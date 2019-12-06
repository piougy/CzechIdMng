package eu.bcvsolutions.idm.core.model.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import java.time.LocalDate;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRequestIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.InvalidFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.rest.AbstractBaseDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;

/**
 * Default implementation of service for search and processing changes in assigned identity roles
 *
 * @author Vít Švanda
 */
@Service("requestIdentityRoleService")
public class DefaultIdmRequestIdentityRoleService extends
			AbstractBaseDtoService<IdmRequestIdentityRoleDto, IdmRequestIdentityRoleFilter>
		implements IdmRequestIdentityRoleService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(DefaultIdmRequestIdentityRoleService.class);
	@Autowired
	private IdmConceptRoleRequestService conceptRoleService;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private ModelMapper modelMapper;
	@Autowired
	private FormService formService;
	@Autowired
	private WorkflowProcessInstanceService workflowProcessInstanceService;

	@Override
	public Page<IdmRequestIdentityRoleDto> find(IdmRequestIdentityRoleFilter filter, Pageable pageable,
			BasePermission... permission) {
		LOG.debug(MessageFormat.format("Find idm-request-identity-roles by filter [{0}] ", filter));
		Assert.notNull(filter, "Filter is required.");
		
		if (pageable == null) {
			// Page is null, so we set page to max value
			pageable = PageRequest.of(0, Integer.MAX_VALUE);
		}
		
		// If is true, then we want to return only concepts (not assigned roles)
		boolean returnOnlyChanges = filter.isOnlyChanges();
		
		List<IdmRequestIdentityRoleDto> results = new ArrayList<IdmRequestIdentityRoleDto>();
		
		long total = 0;
		int countConcepts = 0;
		
		if (filter.getRoleRequestId() != null) {
			if (!returnOnlyChanges) {
				// We want to load only new added roles
				filter.setOperation(ConceptRoleRequestOperation.ADD);
				// We don`t want load ADD concepts with filled identityRoleId (such concepts were already executed )
				filter.setIdentityRoleIsNull(true);
			}
			
			Page<IdmConceptRoleRequestDto> conceptsPage = conceptRoleService.find(filter, pageable, permission);
			results.addAll(this.conceptsToRequestIdentityRoles(conceptsPage.getContent(), filter));
			total = conceptsPage.getTotalElements();
			countConcepts = results.size();
		}
		
		int pageSizeForAssignedRoles = pageable.getPageSize() - countConcepts;
		long numberOfPagesWithConcepts = total / pageable.getPageSize();
		int pageNumberForAssignedRoles = pageable.getPageNumber() - ((int) numberOfPagesWithConcepts);
		
		if (!returnOnlyChanges && filter.getIdentityId() != null && pageSizeForAssignedRoles > 0
				&& pageNumberForAssignedRoles >= 0) {

			IdmIdentityRoleFilter identityRoleFilter = toIdentityRoleFilter(filter);
			 
			PageRequest pageableForAssignedRoles = PageRequest.of(
					pageNumberForAssignedRoles, pageable.getPageSize(),
					pageable.getSort());

			Page<IdmIdentityRoleDto> identityRolesPage = identityRoleService.find(identityRoleFilter,
					pageableForAssignedRoles, permission);
			List<IdmIdentityRoleDto> identityRoles = identityRolesPage.getContent();
			
			// Transform identity-roles to request-identity-roles
			results.addAll(this.identityRolesToRequestIdentityRoles(identityRoles, filter));
			total = total + identityRolesPage.getTotalElements();
			
			if (filter.getRoleRequestId() != null && !identityRoles.isEmpty()) {
				compileIdentityRolesWithConcepts(results, identityRoles, filter, permission);
			}
			
		}
		
		PageRequest pageableRequest = PageRequest.of(pageable.getPageNumber(),
				results.size() > pageable.getPageSize() ? results.size() : pageable.getPageSize(), pageable.getSort());
		return new PageImpl<>(results, pageableRequest, total);
	}
	
	@Override
	@Transactional
	public IdmRequestIdentityRoleDto save(IdmRequestIdentityRoleDto dto, BasePermission... permission) {
		LOG.debug(MessageFormat.format("Save idm-request-identity-role [{0}] ", dto));
		Assert.notNull(dto, "DTO is required.");
	
		// We don`t know if is given DTO identity-role or role-concept.
		if (dto.getId() != null && dto.getId().equals(dto.getIdentityRole())) {
			// Given DTO is identity-role -> create UPDATE concept
			IdmIdentityRoleDto identityRole = identityRoleService.get(dto.getId());
			Assert.notNull(identityRole, "Identity role is required.");

			IdmIdentityContractDto identityContractDto = DtoUtils.getEmbedded(identityRole,
					IdmIdentityRole_.identityContract.getName(), IdmIdentityContractDto.class);
			UUID requestId = dto.getRoleRequest();
			if(requestId == null) {
				IdmRoleRequestDto request = this.createRequest(identityContractDto.getIdentity());
				requestId = request.getId();
			}
			IdmConceptRoleRequestDto conceptRoleRequest = createConcept(identityRole, identityContractDto, requestId,
					identityRole.getRole(), identityContractDto.getValidFrom(), identityContractDto.getValidTill(),
					ConceptRoleRequestOperation.UPDATE);
			conceptRoleRequest.setValidFrom(dto.getValidFrom());
			conceptRoleRequest.setValidTill(dto.getValidTill());
			conceptRoleRequest.setEavs(dto.getEavs());
			// Create concept with EAVs
			conceptRoleRequest = conceptRoleService.save(conceptRoleRequest, permission);
			
			return this.conceptToRequestIdentityRole(conceptRoleRequest, null);
		} else if(dto.getId() == null && dto.getIdentityRole() == null) {
			// Given DTO does not have ID neither identity-role ID -> create ADD concept
			Assert.notNull(dto.getIdentityContract(), "Contract is required.");
			
			Set<UUID> roles = Sets.newHashSet();
			if (dto.getRole() != null) {
				roles.add(dto.getRole());
			}
			if (dto.getRoles() != null) {
				roles.addAll(dto.getRoles());
			}
			
			Assert.notEmpty(roles, "Roles cannot be empty!");
			
			IdmIdentityContractDto identityContractDto = identityContractService.get(dto.getIdentityContract());
			
			UUID requestId = dto.getRoleRequest();
			if(requestId == null) {
				IdmRoleRequestDto request = this.createRequest(identityContractDto.getIdentity());
				requestId = request.getId();
			}
			List<IdmConceptRoleRequestDto> concepts = Lists.newArrayList();
			
			UUID finalRequestId = requestId;
			roles.forEach(role -> {
				IdmConceptRoleRequestDto conceptRoleRequest = createConcept(null, identityContractDto, finalRequestId,
						role, dto.getValidFrom(), dto.getValidTill(), ConceptRoleRequestOperation.ADD);
				conceptRoleRequest.setEavs(dto.getEavs());
				// Create concept with EAVs
				conceptRoleRequest = conceptRoleService.save(conceptRoleRequest);
				concepts.add(conceptRoleRequest);
			});
			// Beware more then one concepts could be created, but only first will be returned!
			return this.conceptToRequestIdentityRole(concepts.get(0), null);
		} else {
			// Try to find role-concept
			IdmConceptRoleRequestDto roleConceptDto = conceptRoleService.get(dto.getId());
			if (roleConceptDto != null) {
				dto.setState(roleConceptDto.getState());
				if (ConceptRoleRequestOperation.UPDATE == roleConceptDto.getOperation()) {
					// Given DTO is concept -> update exists UPDATE concept
					return this.conceptToRequestIdentityRole(conceptRoleService.save(dto, permission), null);
				}
				if (ConceptRoleRequestOperation.ADD == roleConceptDto.getOperation()) {
					// Given DTO is concept -> update exists ADD concept
					return this.conceptToRequestIdentityRole(conceptRoleService.save(dto, permission), null);
				}
			}
		}
		
		return null;
	}
	
	@Override
	@Transactional
	public IdmRequestIdentityRoleDto deleteRequestIdentityRole(IdmRequestIdentityRoleDto dto, BasePermission... permission) {
		LOG.debug(MessageFormat.format("Delete idm-request-identity-role [{0}] ", dto));
		Assert.notNull(dto, "DTO cannot be null!");
		Assert.notNull(dto.getId(), "ID of request-identity-role DTO cannot be null!");
	
		// We don`t know if is given DTO identity-role or role-concept.
		if (dto.getId().equals(dto.getIdentityRole())) {
			IdmIdentityRoleDto identityRoleDto = identityRoleService.get(dto.getId());
			// OK given DTO is identity-role
			
			UUID requestId = dto.getRoleRequest();
			IdmIdentityContractDto identityContractDto = DtoUtils.getEmbedded(identityRoleDto,
					IdmIdentityRole_.identityContract.getName(), IdmIdentityContractDto.class);
			if(requestId == null) {
				IdmRoleRequestDto request = this.createRequest(identityContractDto.getIdentity());
				requestId = request.getId();
			}
			IdmRoleRequestDto mockRequest = new IdmRoleRequestDto();
			mockRequest.setId(requestId);
			IdmConceptRoleRequestDto concept = roleRequestService.createConcept(mockRequest, identityContractDto, identityRoleDto.getId(), identityRoleDto.getRole(),
					ConceptRoleRequestOperation.REMOVE);
			
			return this.conceptToRequestIdentityRole(concept, null);
			
		} else {
			// Try to find role-concept
			IdmConceptRoleRequestDto roleConceptDto = conceptRoleService.get(dto.getId());
			if (roleConceptDto != null) {
				// OK given DTO is concept
				conceptRoleService.delete(roleConceptDto, permission);
				return dto;
			}
		}
		return null;
	}
	
	/**
	 * Not supported, use deleteRequestIdentityRole!
	 */
	@Override
	@Deprecated
	public void delete(IdmRequestIdentityRoleDto dto, BasePermission... permission) {
		throw new UnsupportedOperationException();
	}
	
    /**
     * Convert request-identity-role-filter to identity-role-filter
     * 
     * @param filter
     * @return
     */
	private IdmIdentityRoleFilter toIdentityRoleFilter(IdmRequestIdentityRoleFilter filter) {
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		
		identityRoleFilter.setIdentityContractId(filter.getIdentityContractId());
		identityRoleFilter.setIdentityId(filter.getIdentityId());
		identityRoleFilter.setRoleId(filter.getRoleId());
		identityRoleFilter.setRoleEnvironments(filter.getRoleEnvironments());
		
		return identityRoleFilter;
	}
	
	/**
	 * Creates new manual request for given identity
	 * 
	 * @param identityId
	 * @return
	 */
	private IdmRoleRequestDto createRequest(UUID identityId) {
		Assert.notNull(identityId, "Identity id must be filled for create role request!");
		IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
		roleRequest.setApplicant(identityId);
		roleRequest.setRequestedByType(RoleRequestedByType.MANUALLY);
		roleRequest.setExecuteImmediately(false);
		roleRequest = roleRequestService.save(roleRequest);
		LOG.debug(MessageFormat.format("New manual role-request [{1}] was created.", roleRequest));
		
		return roleRequest;
	}
	
	/**
	 * Create new instance of concept without save
	 * 
	 * @param identityRoleDto
	 * @param identityContractDto
	 * @param requestId
	 * @param operation
	 * @param roleId
	 * 
	 * @return
	 */
	private IdmConceptRoleRequestDto createConcept(IdmIdentityRoleDto identityRoleDto,
			IdmIdentityContractDto identityContractDto, UUID requestId, UUID roleId, LocalDate validFrom, LocalDate validTill, ConceptRoleRequestOperation operation) {
		IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
		conceptRoleRequest.setRoleRequest(requestId);
		if (identityContractDto != null) {
			conceptRoleRequest.setIdentityContract(identityContractDto.getId());
		}
		conceptRoleRequest.setValidFrom(validFrom);
		conceptRoleRequest.setValidTill(validTill);
		if (identityRoleDto != null) {
			conceptRoleRequest.setIdentityRole(identityRoleDto.getId());
		}
		conceptRoleRequest.setRole(roleId);
		conceptRoleRequest.setOperation(operation);
		return conceptRoleRequest;
	}

	/**
	 * Find concepts for given identity-roles. If some exists (in given request),
	 * then will be altered for concept metadata (operation, EAVs)
	 * 
	 * @param requestIdentityRoles
	 * @param identityRoles
	 * @param filter
	 * @param permission
	 */
	private void compileIdentityRolesWithConcepts(List<IdmRequestIdentityRoleDto> requestIdentityRoles,
			List<IdmIdentityRoleDto> identityRoles, IdmRequestIdentityRoleFilter filter, BasePermission... permission) {
		// Convert identity-roles to Set of IDs.
		Set<UUID> identityRoleIds = identityRoles.stream().map(IdmIdentityRoleDto::getId)
				.collect(Collectors.toSet());
		// Find concepts by identity-roles IDs.
		IdmConceptRoleRequestFilter conceptFilter = new IdmConceptRoleRequestFilter();
		conceptFilter.setIdentityRoleIds(identityRoleIds);
		conceptFilter.setRoleRequestId(filter.getRoleRequestId());
		
		List<IdmConceptRoleRequestDto> conceptsForThisPage = conceptRoleService
				.find(conceptFilter, null, permission).getContent();
		conceptsForThisPage.stream() //
			.filter(concept -> ConceptRoleRequestOperation.ADD != concept.getOperation()) //
			.forEach(concept -> { //
			
				IdmRequestIdentityRoleDto requestIdentityRoleWithConcept= requestIdentityRoles.stream() //
						.filter(requestIdentityRole -> 
						requestIdentityRole.getIdentityRole() != null
							&& 
						requestIdentityRole.getIdentityRole().equals(concept.getIdentityRole())
							&& requestIdentityRole.getId().equals(requestIdentityRole.getIdentityRole()))
						.findFirst() //
						.orElse(null); //
				if(requestIdentityRoleWithConcept != null) {
					requestIdentityRoleWithConcept.setOperation(concept.getOperation());
					requestIdentityRoleWithConcept.setId(concept.getId());
					requestIdentityRoleWithConcept.setValidFrom(concept.getValidFrom());
					requestIdentityRoleWithConcept.setValidTill(concept.getValidTill());
					requestIdentityRoleWithConcept.setRoleRequest(concept.getRoleRequest());
					IdmFormInstanceDto formInstanceDto  = null;
					// For updated identity-role replace EAVs from the concept
					if (ConceptRoleRequestOperation.UPDATE == concept.getOperation()) {
						formInstanceDto  = conceptRoleService.getRoleAttributeValues(concept, true);
						this.addEav(requestIdentityRoleWithConcept, formInstanceDto);
					}
				}
		});
	}

	/**
	 * Adds given EAVs attributes to the request-identity-role
	 * 
	 * @param concept
	 * @param formInstanceDto
	 */
	private void addEav(IdmRequestIdentityRoleDto concept, IdmFormInstanceDto formInstanceDto) {
		if (formInstanceDto != null) {
			concept.getEavs().clear();
			concept.getEavs().add(formInstanceDto);
			// Validate the concept
			List<InvalidFormAttributeDto> validationResults = formService.validate(formInstanceDto);
			formInstanceDto.setValidationErrors(formService.validate(formInstanceDto));
			if (!validationResults.isEmpty()) {
				// Concept is not valid (no other metadata for validation problem is not
				// necessary now).
				concept.setValid(false);
			}
		}
	}

	/**
	 * Converts concepts to request-identity-roles
	 * 
	 * @param concepts
	 * @param filter
	 * @return
	 */
	private List<IdmRequestIdentityRoleDto> conceptsToRequestIdentityRoles(List<IdmConceptRoleRequestDto> concepts,  IdmRequestIdentityRoleFilter filter) {

		List<IdmRequestIdentityRoleDto> results = Lists.newArrayList();
		if (concepts == null) {
			return results;
		}
		
		// Mark duplicates
		// TODO: Rewrite to query, this is very ineffective!!
		UUID identityId = filter.getIdentityId();
		LOG.debug(MessageFormat.format("Start searching duplicates for identity [{1}].", identityId));
		Assert.notNull(identityId, "Identity identifier is required.");
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findValidRoles(identityId, null).getContent();
		// Add to all identity roles form instance. For identity role can exists only
		// one form instance.
		identityRoles.forEach(identityRole -> {
			IdmFormInstanceDto formInstance = identityRoleService.getRoleAttributeValues(identityRole);
			if (formInstance != null) {
				identityRole.setEavs(Lists.newArrayList(formInstance));
			}
		});
		// Find potential duplicated concepts (only ADD and not in terminated state)
		List<IdmConceptRoleRequestDto> conceptsForMarkDuplicates = concepts.stream() //
				.filter(concept -> ConceptRoleRequestOperation.ADD == concept.getOperation()) //
				.filter(concept -> !concept.getState().isTerminatedState()) //
				.collect(Collectors.toList()); //
		roleRequestService.markDuplicates(conceptsForMarkDuplicates, identityRoles);
		// End mark duplicates
		LOG.debug(MessageFormat.format("End searching duplicates for identity [{1}].", identityId));

		concepts.forEach(concept -> {
			IdmRequestIdentityRoleDto requestIdentityRoleDto = conceptToRequestIdentityRole(concept, filter);
			results.add(requestIdentityRoleDto);
		});

		return results;
	}

	/**
	 * Converts concept to the request-identity-roles
	 * 
	 * @param concept
	 * @param filter
	 * @return
	 */
	private IdmRequestIdentityRoleDto conceptToRequestIdentityRole(IdmConceptRoleRequestDto concept,
			IdmRequestIdentityRoleFilter filter) {
		IdmRequestIdentityRoleDto requestIdentityRoleDto = modelMapper.map(concept, IdmRequestIdentityRoleDto.class);
		
		if (filter != null && filter.isIncludeEav()) {
			IdmFormInstanceDto formInstanceDto  = null;
			if (ConceptRoleRequestOperation.REMOVE == concept.getOperation()) {
				IdmIdentityRoleDto identityRole = DtoUtils.getEmbedded(concept,
						IdmConceptRoleRequest_.identityRole.getName(), IdmIdentityRoleDto.class,
						(IdmIdentityRoleDto) null);
				if (identityRole == null) { 
					// Identity-role was not found, remove concept was executed (identity-role was removed).
					return addCandidates(requestIdentityRoleDto, concept, filter);
				}
				formInstanceDto  = identityRoleService.getRoleAttributeValues(identityRole);
			} else {
				formInstanceDto  = conceptRoleService.getRoleAttributeValues(concept, true);
			}
			addEav(requestIdentityRoleDto, formInstanceDto);
		}

		return addCandidates(requestIdentityRoleDto, concept, filter);
	}

	/**
	 * Add candidates to given {@link IdmRequestIdentityRoleDto}. Candidates will be added only if filter has includesCandidates = true
	 *
	 * @param requestIdentityRoleDto
	 * @param concept
	 * @param filter
	 * @return
	 */
	private IdmRequestIdentityRoleDto addCandidates(IdmRequestIdentityRoleDto requestIdentityRoleDto, IdmConceptRoleRequestDto concept, IdmRequestIdentityRoleFilter filter) {
		if (filter != null && filter.isIncludeCandidates() && concept.getWfProcessId() != null) {
			// Concept has own process (subprocess), method getApproversForProcess also include approvers for subprocess
			requestIdentityRoleDto.setCandidates(workflowProcessInstanceService.getApproversForProcess(concept.getWfProcessId()));
		}

		return requestIdentityRoleDto;
	}

	/**
	 * Converts identity-roles to request-identity-roles
	 * 
	 * @param identityRoles
	 * @param filter
	 * @return
	 */
	private List<IdmRequestIdentityRoleDto> identityRolesToRequestIdentityRoles(List<IdmIdentityRoleDto> identityRoles, IdmRequestIdentityRoleFilter filter) {
		List<IdmRequestIdentityRoleDto> concepts = Lists.newArrayList();
		
		if (identityRoles  == null) {
			return concepts;
		}
		
		identityRoles.forEach(identityRole -> {
			IdmRequestIdentityRoleDto request = new IdmRequestIdentityRoleDto();
			request.setId(identityRole.getId());
			request.setRole(identityRole.getRole());
			request.setIdentityRole(identityRole.getId());
			request.setDirectRole(identityRole.getDirectRole());
			request.setRoleComposition(identityRole.getRoleComposition());
			request.setIdentityContract(identityRole.getIdentityContract());
			request.setValidFrom(identityRole.getValidFrom());
			request.setValidTill(identityRole.getValidTill());
			request.setAutomaticRole(identityRole.getAutomaticRole());
			request.setTrimmed(true);
			request.getEmbedded().put(IdmIdentityRole_.role.getName(),
					identityRole.getEmbedded().get(IdmIdentityRole_.role.getName()));
			request.getEmbedded().put(IdmIdentityRole_.identityContract.getName(),
					identityRole.getEmbedded().get(IdmIdentityRole_.identityContract.getName()));
			
			if (filter.isIncludeEav()) {
				IdmFormInstanceDto formInstanceDto  = identityRoleService.getRoleAttributeValues(identityRole);
				addEav(request, formInstanceDto);
			}

			concepts.add(request);
		});
		
		return concepts;
	} 

}
