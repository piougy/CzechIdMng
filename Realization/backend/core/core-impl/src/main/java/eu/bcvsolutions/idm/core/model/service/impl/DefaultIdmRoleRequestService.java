package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.RoleRequestException;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.model.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.filter.ConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleRequestFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleRequest;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Default implementation of role request service
 * 
 * @author svandav
 *
 */
@Service("roleRequestService")
public class DefaultIdmRoleRequestService
		extends AbstractReadWriteDtoService<IdmRoleRequestDto, IdmRoleRequest, RoleRequestFilter>
		implements IdmRoleRequestService {

	// private static final org.slf4j.Logger LOG =
	// org.slf4j.LoggerFactory.getLogger(DefaultIdmRoleRequestService.class);
	private final IdmConceptRoleRequestService conceptRoleRequestService;
	private final IdmIdentityRoleService identityRoleService;
	private final IdmIdentityService identityService;
	private final ObjectMapper objectMapper;
	private final SecurityService securityService;
	private IdmRoleRequestService roleRequestService;
	private final ApplicationContext applicationContext;

	@Autowired
	public DefaultIdmRoleRequestService(AbstractEntityRepository<IdmRoleRequest, RoleRequestFilter> repository,
			IdmConceptRoleRequestService conceptRoleRequestService, IdmIdentityRoleService identityRoleService,
			IdmIdentityService identityService, ObjectMapper objectMapper, SecurityService securityService,
			ApplicationContext applicationContext) {
		super(repository);

		Assert.notNull(conceptRoleRequestService, "Concept role request service is required!");
		Assert.notNull(identityRoleService, "Identity role service is required!");
		Assert.notNull(identityService, "Identity service is required!");
		Assert.notNull(objectMapper, "Object mapper is required!");
		Assert.notNull(securityService, "Security service is required!");
		Assert.notNull(applicationContext, "Application context is required!");

		this.conceptRoleRequestService = conceptRoleRequestService;
		this.identityRoleService = identityRoleService;
		this.identityService = identityService;
		this.objectMapper = objectMapper;
		this.securityService = securityService;
		this.applicationContext = applicationContext;
	}

	@Override
	public IdmRoleRequestDto saveDto(IdmRoleRequestDto dto) {
		boolean created = false;
		if (dto.getId() == null) {
			created = true;
		}
		// Load applicant (check read right)
		IdmIdentity applicant = identityService.get(dto.getApplicant());
		List<IdmConceptRoleRequestDto> concepts = dto.getConceptRoles();
		
		validateOnDuplicity(dto);
		IdmRoleRequestDto savedRequest = super.saveDto(dto);

		// Concepts will be save only on create request
		if (created && concepts != null) {
			concepts.forEach(concept -> {
				concept.setRoleRequest(savedRequest.getId());
			});
			this.conceptRoleRequestService.saveAllDto(concepts);
		}

		// Check on same applicants in all role concepts
		boolean identityNotSame = this.getDto(savedRequest.getId()).getConceptRoles().stream().filter(concept -> {
			// get contract dto from embedded map
			IdmIdentityContractDto contract = (IdmIdentityContractDto) concept.getEmbedded()
					.get(IdmConceptRoleRequestService.IDENTITY_CONTRACT_FIELD);
			return !dto.getApplicant().equals(contract.getIdentity());
		}).findFirst().isPresent();

		if (identityNotSame) {
			throw new RoleRequestException(CoreResultCode.ROLE_REQUEST_APPLICANTS_NOT_SAME,
					ImmutableMap.of("request", dto, "applicant", applicant.getUsername()));
		}

		if (created) {
			// TODO: Separate start request to own schedule task
			this.startRequest(savedRequest.getId());
		}
		return this.getDto(savedRequest.getId());
	}

	@Override
	public IdmRoleRequestDto toDto(IdmRoleRequest entity, IdmRoleRequestDto dto) {
		IdmRoleRequestDto requestDto = super.toDto(entity, dto);
		if (requestDto != null) {
			ConceptRoleRequestFilter conceptFilter = new ConceptRoleRequestFilter();
			conceptFilter.setRoleRequestId(requestDto.getId());
			requestDto.setConceptRoles(conceptRoleRequestService.findDto(conceptFilter, null).getContent());
		}
		return requestDto;
	}

	@Override
	public IdmRoleRequest toEntity(IdmRoleRequestDto dto, IdmRoleRequest entity) {
		if (entity == null || entity.getId() == null) {
			try {
				dto.setOriginalRequest(objectMapper.writeValueAsString(dto));
			} catch (JsonProcessingException e) {
				throw new RoleRequestException(CoreResultCode.BAD_REQUEST, e);
			}
		}
		// Set persisted value to read only properties
		// TODO: Create converter for skip fields mark as read only
		if (dto.getId() != null) {
			IdmRoleRequestDto dtoPersisited = this.getDto(dto.getId());
			if (dto.getState() == null) {
				dto.setState(dtoPersisited.getState());
			}
			if (dto.getLog() == null) {
				dto.setLog(dtoPersisited.getLog());
			}
			if (dto.getDuplicatedToRequest() == null) {
				dto.setDuplicatedToRequest(dtoPersisited.getDuplicatedToRequest());
			}
			if (dto.getWfProcessId() == null) {
				dto.setWfProcessId(dtoPersisited.getWfProcessId());
			}
			if (dto.getOriginalRequest() == null) {
				dto.setOriginalRequest(dtoPersisited.getOriginalRequest());
			}
		} else {
			dto.setState(RoleRequestState.CREATED);
		}

		return super.toEntity(dto, entity);

	}

	@Override
	public void startRequest(UUID requestId) {

		try {
			// Request will be started in new transaction
			this.getIdmRoleRequestService().startRequestInternal(requestId, true);
		} catch (Exception ex) {
			IdmRoleRequestDto request = getDto(requestId);
			request.setLog(Throwables.getStackTraceAsString(ex));
			request.setState(RoleRequestState.EXCEPTION);
			saveDto(request);
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void startRequestInternal(UUID requestId, boolean checkRight) {
		Assert.notNull(requestId, "Role request ID is required!");
		// Load request ... check right for read
		IdmRoleRequestDto request = getDto(requestId);
		Assert.notNull(request, "Role request DTO is required!");
		Assert.isTrue(RoleRequestState.CREATED == request.getState(),
				"Only role request with CREATED state can be started!");

		validateOnDuplicity(request);

		// TODO: check on same identities

		request.setState(RoleRequestState.IN_PROGRESS);
		this.saveDto(request);

		if (request.isExecuteImmediately()) {
			boolean haveRightExecuteImmediately = securityService
					.hasAnyAuthority(IdmGroupPermission.ROLE_REQUEST_IMMEDIATELY_WRITE);

			if (checkRight && !haveRightExecuteImmediately) {
				throw new RoleRequestException(CoreResultCode.ROLE_REQUEST_NO_EXECUTE_IMMIDIATELY_RIGHT,
						ImmutableMap.of("new", request));
			}

			// Execute request immediately
			executeRequest(request.getId());

		}
	}

	private void validateOnDuplicity(IdmRoleRequestDto request) {
		List<IdmRoleRequestDto> potentialDuplicatedRequests = new ArrayList<>();

		RoleRequestFilter requestFilter = new RoleRequestFilter();
		requestFilter.setApplicantId(request.getApplicant());
		requestFilter.setState(RoleRequestState.IN_PROGRESS);
		potentialDuplicatedRequests.addAll(this.findDto(requestFilter, null).getContent());

		requestFilter.setState(RoleRequestState.APPROVED);
		potentialDuplicatedRequests.addAll(this.findDto(requestFilter, null).getContent());
		
		requestFilter.setState(RoleRequestState.CREATED);
		potentialDuplicatedRequests.addAll(this.findDto(requestFilter, null).getContent());

		Optional<IdmRoleRequestDto> duplicatedRequestOptional = potentialDuplicatedRequests.stream()
				.filter(requestDuplicate -> {
					return isDuplicated(request, requestDuplicate) && !(request.getId() != null && requestDuplicate.getId() != null && request.getId().equals(requestDuplicate.getId()));
				}).findFirst();

		if (duplicatedRequestOptional.isPresent()) {
			throw new RoleRequestException(CoreResultCode.ROLE_REQUEST_DUPLICATE_REQUEST,
					ImmutableMap.of("new", request, "duplicant", duplicatedRequestOptional.get()));
		}
	}

	public void executeRequest(UUID requestId) {
		Assert.notNull(requestId, "Role request ID is required!");
		IdmRoleRequestDto request = this.getDto(requestId);
		Assert.notNull(request, "Role request is required!");

		List<IdmConceptRoleRequestDto> concepts = request.getConceptRoles();
		IdmIdentity identity = identityService.get(request.getApplicant());

		boolean identityNotSame = concepts.stream().filter(concept -> {
			// get contract dto from embedded map
			IdmIdentityContractDto contract = (IdmIdentityContractDto) concept.getEmbedded()
					.get(IdmConceptRoleRequestService.IDENTITY_CONTRACT_FIELD);
			return !identity.getId().equals(contract.getIdentity());
		}).findFirst().isPresent();

		if (identityNotSame) {
			throw new RoleRequestException(CoreResultCode.ROLE_REQUEST_APPLICANTS_NOT_SAME,
					ImmutableMap.of("request", request, "applicant", identity.getUsername()));
		}

		List<IdmIdentityRole> identityRoles = new ArrayList<>();
		concepts.forEach(concept -> {
			IdmIdentityRole identityRole = new IdmIdentityRole();
			identityRoles.add(
					convertConceptRoleToIdentityRole(conceptRoleRequestService.get(concept.getId()), identityRole));
		});

		identityRoleService.saveAll(identityRoles);
		request.setState(RoleRequestState.EXECUTED);
		this.saveDto(request);

	}

	private boolean isDuplicated(IdmRoleRequestDto request, IdmRoleRequestDto duplicant) {

		if (request == duplicant) {
			return true;
		}

		if (request.getConceptRoles() == null) {
			if (duplicant.getConceptRoles() != null) {
				return false;
			}
		} else if (!request.getConceptRoles().equals(duplicant.getConceptRoles())) {
			return false;
		}
		if (request.getApplicant() == null) {
			if (duplicant.getApplicant() != null) {
				return false;
			}
		} else if (!request.getApplicant().equals(duplicant.getApplicant())) {
			return false;
		}
		return true;
	}

	private IdmIdentityRole convertConceptRoleToIdentityRole(IdmConceptRoleRequest conceptRole,
			IdmIdentityRole identityRole) {
		if (conceptRole == null || identityRole == null) {
			return null;
		}
		identityRole.setRole(conceptRole.getRole());
		identityRole.setIdentityContract(conceptRole.getIdentityContract());
		identityRole.setValidFrom(conceptRole.getValidFrom());
		identityRole.setValidTill(conceptRole.getValidTill());
		identityRole.setOriginalCreator(conceptRole.getOriginalCreator());
		identityRole.setOriginalModifier(conceptRole.getOriginalModifier());
		return identityRole;
	}

	private IdmRoleRequestService getIdmRoleRequestService() {
		if (this.roleRequestService == null) {
			this.roleRequestService = applicationContext.getBean(IdmRoleRequestService.class);
		}
		return this.roleRequestService;
	}

}
