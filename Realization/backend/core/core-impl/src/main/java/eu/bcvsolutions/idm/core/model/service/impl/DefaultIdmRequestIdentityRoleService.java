package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.transaction.NotSupportedException;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.google.common.collect.Lists;

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
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRequestIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.InvalidFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmConceptRoleRequestRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Default implementation of service for search and processing changes in assigned identity roles
 *
 * @author Vít Švanda
 */
@Service("requestIdentityRoleService")
public class DefaultIdmRequestIdentityRoleService extends
		AbstractReadWriteDtoService<IdmRequestIdentityRoleDto, IdmConceptRoleRequest, IdmRequestIdentityRoleFilter>
		implements IdmRequestIdentityRoleService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(DefaultIdmRequestIdentityRoleService.class);
	@Autowired
	private IdmConceptRoleRequestService conceptRoleService;
	@Autowired
	private IdmRoleRequestService roleRoleService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	protected ModelMapper modelMapper;
	@Autowired
	private FormService formService;

	@Autowired
	public DefaultIdmRequestIdentityRoleService(IdmConceptRoleRequestRepository repository) {
		super(repository);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		// secured internally by role requests
		return null;
	}

	@Override
	public IdmConceptRoleRequest checkAccess(IdmConceptRoleRequest entity, BasePermission... permission) {
		if (entity == null) {
			// nothing to check
			return null;
		}

		if (ObjectUtils.isEmpty(permission)) {
			return entity;
		}
		// TODO
		throw new ForbiddenEntityException(entity.getId(), permission);
	}
	
	@Override
	public Page<IdmRequestIdentityRoleDto> find(IdmRequestIdentityRoleFilter filter, Pageable pageable,
			BasePermission... permission) {
		
		Assert.notNull(filter);
		// If is true, then we want to return only concepts (not assigned roles)
		boolean returnOnlyChanges = filter.isOnlyChanges();
		
		List<IdmRequestIdentityRoleDto> results = new ArrayList<IdmRequestIdentityRoleDto>();
		
		long total = 0;
		int countConcepts = 0;
		
		if (filter.getRoleRequestId() != null) {
			if (!returnOnlyChanges) {
				// We want to load only new added roles
				filter.setOperation(ConceptRoleRequestOperation.ADD);
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

			// TODO convert filter;
			IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
			identityRoleFilter.setIdentityContractId(filter.getIdentityContractId());
			identityRoleFilter.setIdentityId(filter.getIdentityId());
			identityRoleFilter.setRoleId(filter.getRoleId());
			 
			PageRequest pageableForAssignedRoles = new PageRequest(
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
		
		PageRequest pageableRequest = new PageRequest(pageable.getPageNumber(),
				results.size() > pageable.getPageSize() ? results.size() : pageable.getPageSize(), pageable.getSort());
		return new PageImpl<>(results, pageableRequest, total);
	}
	
	@Override
	public IdmRequestIdentityRoleDto save(IdmRequestIdentityRoleDto dto, BasePermission... permission) {
		Assert.notNull(dto);
	
		// We don`t know if is given DTO identity-role or role-concept.
		if (dto.getId() != null && dto.getId().equals(dto.getIdentityRole())) {
			// Given DTO is identity-role -> create UPDATE concept
			IdmIdentityRoleDto identityRoleDto = identityRoleService.get(dto.getId());
			Assert.notNull(identityRoleDto);

			IdmIdentityContractDto identityContractDto = DtoUtils.getEmbedded(identityRoleDto,
					IdmIdentityRole_.identityContract.getName(), IdmIdentityContractDto.class);
			UUID requestId = dto.getRoleRequest();
			if(requestId == null) {
				IdmRoleRequestDto request = this.createRequest(identityContractDto.getIdentity());
				requestId = request.getId();
			}
			IdmConceptRoleRequestDto conceptRoleRequest = createConcept(identityRoleDto, identityContractDto,
					requestId, identityRoleDto.getRole(), ConceptRoleRequestOperation.UPDATE);
			conceptRoleRequest.setValidFrom(dto.getValidFrom());
			conceptRoleRequest.setValidTill(dto.getValidTill());
			conceptRoleRequest.setEavs(dto.getEavs());
			// Create concept with EAVs
			conceptRoleRequest = conceptRoleService.save(conceptRoleRequest, permission);
			
			return this.conceptToRequestIdentityRole(conceptRoleRequest, null);
		} else if(dto.getId() == null && dto.getIdentityRole() == null) {
			// Given DTO does not have ID neither identity-role ID -> create ADD concept
			Assert.notNull(dto.getIdentityContract());
			Assert.notNull(dto.getRoles());
			
			IdmIdentityContractDto identityContractDto = identityContractService.get(dto.getIdentityContract());
			
			UUID requestId = dto.getRoleRequest();
			if(requestId == null) {
				IdmRoleRequestDto request = this.createRequest(identityContractDto.getIdentity());
				requestId = request.getId();
			}
			List<IdmConceptRoleRequestDto> concepts = Lists.newArrayList();
			
			UUID finalRequestId = requestId;
			dto.getRoles().forEach(role -> {
				IdmConceptRoleRequestDto conceptRoleRequest = createConcept(null, identityContractDto, finalRequestId,
						role, ConceptRoleRequestOperation.ADD);
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
		Assert.notNull(dto);
		Assert.notNull(dto.getId());
	
		// We don`t know if is given DTO identity-role or role-concept.
		if (dto.getId().equals(dto.getIdentityRole())) {
			IdmIdentityRoleDto identityRoleDto = identityRoleService.get(dto.getId());
			// OK given DTO is identity-role
			
			UUID requestId = dto.getRoleRequest();
			if(requestId == null) {
				IdmIdentityContractDto identityContractDto = DtoUtils.getEmbedded(identityRoleDto,
						IdmIdentityRole_.identityContract.getName(), IdmIdentityContractDto.class);
				IdmRoleRequestDto request = this.createRequest(identityContractDto.getIdentity());
				requestId = request.getId();
			}
			IdmRoleRequestDto mockRequest = new IdmRoleRequestDto();
			mockRequest.setId(requestId);
			IdmConceptRoleRequestDto concept = roleRoleService.createConcept(mockRequest, null, identityRoleDto.getId(), identityRoleDto.getRole(),
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
	
	
	
	@Override
	public void delete(IdmRequestIdentityRoleDto dto, BasePermission... permission) {
		new NotSupportedException();
	}
	
	private IdmRoleRequestDto createRequest(UUID identityId) {
		Assert.notNull(identityId, "Identity id must be filled for create role request!");
		IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
		roleRequest.setApplicant(identityId);
		roleRequest.setRequestedByType(RoleRequestedByType.MANUALLY);
		roleRequest.setExecuteImmediately(false);
		roleRequest = roleRoleService.save(roleRequest);
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
			IdmIdentityContractDto identityContractDto, UUID requestId, UUID roleId, ConceptRoleRequestOperation operation) {
		IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
		conceptRoleRequest.setRoleRequest(requestId);
		if (identityContractDto != null) {
			conceptRoleRequest.setIdentityContract(identityContractDto.getId());
			conceptRoleRequest.setValidFrom(identityContractDto.getValidFrom());
			conceptRoleRequest.setValidTill(identityContractDto.getValidTill());
		}
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
					IdmFormInstanceDto formInstanceDto  = null;
					// For updated identity-role replace EAVs from the concept
					if (ConceptRoleRequestOperation.UPDATE == concept.getOperation()) {
						formInstanceDto  = conceptRoleService.getRoleAttributeValues(concept, true);
						this.addEav(requestIdentityRoleWithConcept, formInstanceDto);
					}
				}
		});
	}

	
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

	private List<IdmRequestIdentityRoleDto> conceptsToRequestIdentityRoles(List<IdmConceptRoleRequestDto> concepts,  IdmRequestIdentityRoleFilter filter) {

		List<IdmRequestIdentityRoleDto> results = Lists.newArrayList();
		if (concepts == null) {
			return results;
		}

		concepts.forEach(concept -> {
			IdmRequestIdentityRoleDto requestIdentityRoleDto = conceptToRequestIdentityRole(concept, filter);
			results.add(requestIdentityRoleDto);
		});

		return results;
	}

	private IdmRequestIdentityRoleDto conceptToRequestIdentityRole(IdmConceptRoleRequestDto concept,
			IdmRequestIdentityRoleFilter filter) {
		IdmRequestIdentityRoleDto requestIdentityRoleDto = modelMapper.map(concept, IdmRequestIdentityRoleDto.class);
		if (filter != null && filter.isIncludeEav()) {
			IdmFormInstanceDto formInstanceDto  = null;
			if (ConceptRoleRequestOperation.REMOVE == concept.getOperation()) {
				IdmIdentityRoleDto identityRole = DtoUtils.getEmbedded(concept, IdmConceptRoleRequest_.identityRole.getName(), IdmIdentityRoleDto.class);
				formInstanceDto  = identityRoleService.getRoleAttributeValues(identityRole);
			} else {
				formInstanceDto  = conceptRoleService.getRoleAttributeValues(concept, true);
			}
			addEav(requestIdentityRoleDto, formInstanceDto);
		}
		return requestIdentityRoleDto;
	}

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
