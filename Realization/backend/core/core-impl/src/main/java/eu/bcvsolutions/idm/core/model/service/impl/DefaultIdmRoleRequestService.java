package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.RoleRequestException;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.model.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.filter.ConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleRequestFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleRequest;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;

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

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmRoleRequestService.class);
	private final IdmConceptRoleRequestService conceptRoleRequestService;
	private final IdmIdentityRoleService identityRoleService;
	private final IdmIdentityService identityService;

	@Autowired
	public DefaultIdmRoleRequestService(AbstractEntityRepository<IdmRoleRequest, RoleRequestFilter> repository,
			IdmConceptRoleRequestService conceptRoleRequestService, IdmIdentityRoleService identityRoleService,
			IdmIdentityService identityService) {
		super(repository);

		Assert.notNull(conceptRoleRequestService, "Concept role request service is required!");
		Assert.notNull(identityRoleService, "Identity role service is required!");
		Assert.notNull(identityService, "Identity service is required!");

		this.conceptRoleRequestService = conceptRoleRequestService;
		this.identityRoleService = identityRoleService;
		this.identityService = identityService;
	}

	@Override
	public IdmRoleRequestDto saveDto(IdmRoleRequestDto dto) {
		boolean created = false;
		if (dto.getId() == null) {
			created = true;
		}
		IdmRoleRequestDto savedRequest = super.saveDto(dto);
		List<IdmConceptRoleRequestDto> concepts = dto.getConceptRoles();
		concepts.forEach(concept -> {
			concept.setRoleRequest(savedRequest.getId());
		});
		this.conceptRoleRequestService.saveAllDto(concepts);
		if (created) {
			this.startRequest(this.getDto(savedRequest.getId()));
		}
		return this.getDto(savedRequest.getId());
	}

	@Override
	public IdmRoleRequestDto toDto(IdmRoleRequest entity, IdmRoleRequestDto dto) {
		IdmRoleRequestDto requestDto = super.toDto(entity, dto);
		if (requestDto != null) {
			ConceptRoleRequestFilter conceptFilter = new ConceptRoleRequestFilter();
			conceptFilter.setRoleRequest(requestDto.getId());
			requestDto.setConceptRoles(conceptRoleRequestService.findDto(conceptFilter, null).getContent());
		}
		return requestDto;
	}

	public void startRequest(IdmRoleRequestDto request) {
		Assert.notNull(request, "Role request dto is required!");
		Assert.isTrue(RoleRequestState.CREATED == request.getState(),
				"Only role request with CREATED state can be started!");

		List<IdmRoleRequestDto> potentialDuplicatedRequests = new ArrayList<>();

		RoleRequestFilter requestFilter = new RoleRequestFilter();
		requestFilter.setIdentityUUID(request.getIdentity());
		requestFilter.setState(RoleRequestState.IN_PROGRESS);
		potentialDuplicatedRequests.addAll(this.findDto(requestFilter, null).getContent());

		requestFilter.setState(RoleRequestState.APPROVED);
		potentialDuplicatedRequests.addAll(this.findDto(requestFilter, null).getContent());

		Optional<IdmRoleRequestDto> duplicatedRequestOptional = potentialDuplicatedRequests.stream()
				.filter(requestDuplicate -> {
					return isDuplicated(request, requestDuplicate);
				}).findFirst();

		if (duplicatedRequestOptional.isPresent()) {
			throw new RoleRequestException(CoreResultCode.ROLE_REQUEST_DUPLICATE_REQUEST,
					ImmutableMap.of("new", request, "duplicant", duplicatedRequestOptional.get()));
		}
		
		// TODO: check on same identities

		request.setState(RoleRequestState.IN_PROGRESS);
		this.saveDto(request);

		if (request.isExecuteImmediately()) {
			boolean haveRightExecuteImmediately = true;

			// TODO: check right

			if (!haveRightExecuteImmediately) {
				throw new RoleRequestException(CoreResultCode.ROLE_REQUEST_NO_EXECUTE_IMMIDIATELY_RIGHT,
						ImmutableMap.of("new", request));
			}
			
			// Execute request immediately
			executeRequest(request.getId());

		}

	}

	public void executeRequest(UUID requestId) {
		Assert.notNull(requestId, "Role request ID is required!");
		IdmRoleRequestDto request = this.getDto(requestId);
		Assert.notNull(request, "Role request is required!");

		IdmIdentity identity = identityService.get(request.getId());
		
		List<IdmConceptRoleRequestDto> concepts = request.getConceptRoles();
		List<IdmIdentityRole> identityRoles = new ArrayList<>();
		concepts.forEach(concept -> {
			IdmIdentityRole identityRole = new IdmIdentityRole();
			identityRoles.add(convertConceptRoleToIdentityRole(
					conceptRoleRequestService.get(concept.getId()), identityRole));
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
		if (request.getIdentity() == null) {
			if (duplicant.getIdentity() != null) {
				return false;
			}
		} else if (!request.getIdentity().equals(duplicant.getIdentity())) {
			return false;
		}
		return true;
	}
	
	private IdmIdentityRole convertConceptRoleToIdentityRole(IdmConceptRoleRequest conceptRole, IdmIdentityRole identityRole) {
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

}
