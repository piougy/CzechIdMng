package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.activiti.engine.runtime.ProcessInstance;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.Loggable;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.exception.RoleRequestException;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
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
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;
import eu.bcvsolutions.idm.core.model.event.processor.role.RoleRequestApprovalProcessor;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;

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
	private final ObjectMapper objectMapper;
	private final SecurityService securityService;
	private final ApplicationContext applicationContext;
	private final WorkflowProcessInstanceService workflowProcessInstanceService;
	private final EntityEventManager entityEventManager;
	private IdmRoleRequestService roleRequestService;

	@Autowired
	public DefaultIdmRoleRequestService(AbstractEntityRepository<IdmRoleRequest, RoleRequestFilter> repository,
			IdmConceptRoleRequestService conceptRoleRequestService,
			IdmIdentityRoleService identityRoleService,
			IdmIdentityService identityService,
			ObjectMapper objectMapper,
			SecurityService securityService,
			ApplicationContext applicationContext,
			WorkflowProcessInstanceService workflowProcessInstanceService,
			EntityEventManager entityEventManager) {
		super(repository);

		Assert.notNull(conceptRoleRequestService, "Concept role request service is required!");
		Assert.notNull(identityRoleService, "Identity role service is required!");
		Assert.notNull(identityService, "Identity service is required!");
		Assert.notNull(objectMapper, "Object mapper is required!");
		Assert.notNull(securityService, "Security service is required!");
		Assert.notNull(applicationContext, "Application context is required!");
		Assert.notNull(workflowProcessInstanceService, "Workflow process instance service is required!");
		Assert.notNull(entityEventManager, "Entity event manager is required!");

		this.conceptRoleRequestService = conceptRoleRequestService;
		this.identityRoleService = identityRoleService;
		this.identityService = identityService;
		this.objectMapper = objectMapper;
		this.securityService = securityService;
		this.applicationContext = applicationContext;
		this.workflowProcessInstanceService = workflowProcessInstanceService;
		this.entityEventManager = entityEventManager;
	}

	@Override
	@Transactional
	public void startRequest(UUID requestId) {

		try {
			this.getIdmRoleRequestService().startRequestNewTransactional(requestId, true);
		} catch (Exception ex) {
			LOG.error(ex.getLocalizedMessage(), ex);
			IdmRoleRequestDto request = getDto(requestId);
			this.addToLog(request, Throwables.getStackTraceAsString(ex));
			request.setState(RoleRequestState.EXCEPTION);
			save(request);
		}
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void startRequestNewTransactional(UUID requestId, boolean checkRight) {
		this.getIdmRoleRequestService().startRequestInternal(requestId, true);
	}
	
	@Override
	@Transactional
	public void startRequestInternal(UUID requestId, boolean checkRight) {
		LOG.debug("Start role request [{}]", requestId);
		Assert.notNull(requestId, "Role request ID is required!");
		// Load request ... check right for read
		IdmRoleRequestDto request = getDto(requestId);
		Assert.notNull(request, "Role request DTO is required!");
		Assert.isTrue(
				RoleRequestState.CONCEPT == request.getState() 
				|| RoleRequestState.DUPLICATED == request.getState() 
				|| RoleRequestState.EXCEPTION == request.getState(),
				"Only role request with CONCEPT or EXCEPTION or DUPLICATED state can be started!");

		IdmRoleRequestDto duplicant = validateOnDuplicity(request);

		if (duplicant != null) {
			request.setState(RoleRequestState.DUPLICATED);
			request.setDuplicatedToRequest(duplicant.getId());
			this.addToLog(request, MessageFormat.format("This request [{0}] is duplicated to another change permissions request [{1}]", request.getId(), duplicant.getId()));
			this.save(request);
			return;
		}
		
		// Duplicant is fill, but request is not duplicated (maybe in past)
		if (request.getDuplicatedToRequest() != null){
			request.setDuplicatedToRequest(null);
		}
		
		// Check on same applicants in all role concepts
		boolean identityNotSame = this.getDto(request.getId()).getConceptRoles().stream().filter(concept -> {
			// get contract dto from embedded map
			IdmIdentityContractDto contract = (IdmIdentityContractDto) concept.getEmbedded()
					.get(IdmConceptRoleRequestService.IDENTITY_CONTRACT_FIELD);
			return !request.getApplicant().equals(contract.getIdentity());
		}).findFirst().isPresent();

		if (identityNotSame) {
			throw new RoleRequestException(CoreResultCode.ROLE_REQUEST_APPLICANTS_NOT_SAME,
					ImmutableMap.of("request", request, "applicant", request.getApplicant()));
		}
	
		// Convert whole request to JSON and persist (without logs and embedded data)
		try {
			IdmRoleRequestDto requestOriginal = getDto(requestId);
			trimRequest(requestOriginal);
			request.setOriginalRequest(objectMapper.writeValueAsString(requestOriginal));
		} catch (JsonProcessingException e) {
			throw new RoleRequestException(CoreResultCode.BAD_REQUEST, e);
		}

		// Request will be set on in progress state
		request.setState(RoleRequestState.IN_PROGRESS);
		IdmRoleRequestDto savedRequest = this.save(request);
		
		// Throw event
		Map<String, Serializable> variables = new HashMap<>();
		variables.put(RoleRequestApprovalProcessor.CHECK_RIGHT_PROPERTY, checkRight);
		entityEventManager.process(new RoleRequestEvent(RoleRequestEventType.EXCECUTE, savedRequest, variables));
	}
	

	@Override
	@Transactional
	public boolean startApprovalProcess(IdmRoleRequestDto request, boolean checkRight, EntityEvent<IdmRoleRequestDto> event,  String wfDefinition){
		// If is request marked as executed immediately, then we will check right
		// and do realization immediately (without start approval process) 
		if (request.isExecuteImmediately()) {
			boolean haveRightExecuteImmediately = securityService
					.hasAnyAuthority(CoreGroupPermission.ROLE_REQUEST_EXECUTE);

			if (checkRight && !haveRightExecuteImmediately) {
				throw new RoleRequestException(CoreResultCode.ROLE_REQUEST_NO_EXECUTE_IMMEDIATELY_RIGHT,
						ImmutableMap.of("new", request));
			}

			// All concepts in progress state will be set on approved (we can
			// execute it immediately)
			request.getConceptRoles().stream().filter(concept -> {
				return RoleRequestState.IN_PROGRESS == concept.getState();
			}).forEach(concept -> {
				concept.setState(RoleRequestState.APPROVED);
				conceptRoleRequestService.save(concept);
			});

			// Execute request immediately
			return true;
		}else {
			IdmIdentity applicant = identityService.get(request.getApplicant());
			
			Map<String, Object> variables = new HashMap<>();
			// Minimize size of DTO persisting to WF
			IdmRoleRequestDto eventRequest = event.getContent();
			trimRequest(eventRequest);
			eventRequest.setConceptRoles(null);
			eventRequest.setOriginalRequest(null);
			variables.put(EntityEvent.EVENT_PROPERTY, event);
			
			ProcessInstance processInstance = workflowProcessInstanceService.startProcess(wfDefinition,
					IdmIdentity.class.getSimpleName(), applicant.getUsername(), applicant.getId().toString(), variables);
			// We have to refresh request (maybe was changed in wf process)
			request = this.getDto(request.getId());
			request.setWfProcessId(processInstance.getProcessInstanceId());
			this.save(request);
		}
		
		return false;
	}
	
	
	@Override
	@Transactional
	public IdmRoleRequestDto executeRequest(UUID requestId) {
		try {
			return this.executeRequestInternal(requestId);
		} catch (Exception ex) {
			LOG.error(ex.getLocalizedMessage(), ex);
			IdmRoleRequestDto request = getDto(requestId);
			this.addToLog(request, Throwables.getStackTraceAsString(ex));
			request.setState(RoleRequestState.EXCEPTION);
			return save(request);
		}

	}
	
	protected IdmRoleRequestDto executeRequestInternal(UUID requestId) {
		Assert.notNull(requestId, "Role request ID is required!");
		IdmRoleRequestDto request = this.getDto(requestId);
		Assert.notNull(request, "Role request is required!");
//		if(RoleRequestState.APPROVED != request.getState() && RoleRequestState.CONCEPT != request.getState()){
//			throw new RoleRequestException(CoreResultCode.ROLE_REQUEST_EXECUTE_WRONG_STATE,
//					ImmutableMap.of("state", request.getState()));
//		}

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

		List<IdmIdentityRole> identityRolesToSave = new ArrayList<>();
		List<IdmConceptRoleRequestDto> conceptsToSave = new ArrayList<>();

		// Create new identity role
		concepts.stream().filter(concept -> {
			return ConceptRoleRequestOperation.ADD == concept.getOperation();
		}).filter(concept -> {
			// Only approved concepts can be executed
			// Concepts in concept state will be executed too (for situation, when will be approval event disabled)
			return RoleRequestState.APPROVED == concept.getState() || RoleRequestState.CONCEPT == concept.getState();
		}).forEach(concept -> {
			IdmIdentityRole identityRole = new IdmIdentityRole();
			identityRolesToSave.add(
					convertConceptRoleToIdentityRole(conceptRoleRequestService.get(concept.getId()), identityRole));
			concept.setState(RoleRequestState.EXECUTED);
			String message = MessageFormat.format("IdentityRole [{0}] was added to applicant. Requested in concept [{1}].",
					identityRole.getRole().getName(), concept.getId());
			conceptRoleRequestService.addToLog(concept, message);
			conceptRoleRequestService.addToLog(request, message);
			conceptsToSave.add(concept);
		});

		// Update identity role
		concepts.stream().filter(concept -> {
			return ConceptRoleRequestOperation.UPDATE == concept.getOperation();
		}).filter(concept -> {
			// Only approved concepts can be executed
			// Concepts in concept state will be executed too (for situation, when will be approval event disabled)
			return RoleRequestState.APPROVED == concept.getState() || RoleRequestState.CONCEPT == concept.getState();
		}).forEach(concept -> {
			IdmIdentityRole identityRole = identityRoleService.get(concept.getIdentityRole());
			identityRolesToSave.add(
					convertConceptRoleToIdentityRole(conceptRoleRequestService.get(concept.getId()), identityRole));
			concept.setState(RoleRequestState.EXECUTED);
			String message = MessageFormat.format("IdentityRole [{0}] was changed. Requested in concept [{1}].",
					identityRole.getRole().getName(), concept.getId());
			conceptRoleRequestService.addToLog(concept, message);
			conceptRoleRequestService.addToLog(request, message);
			conceptsToSave.add(concept);
		});

		// Delete identity role
		concepts.stream().filter(concept -> {
			return ConceptRoleRequestOperation.REMOVE == concept.getOperation();
		}).filter(concept -> {
			// Only approved concepts can be executed
			// Concepts in concept state will be executed too (for situation, when will be approval event disabled)
			return RoleRequestState.APPROVED == concept.getState() || RoleRequestState.CONCEPT == concept.getState();
		}).forEach(concept -> {
			IdmIdentityRole identityRole = identityRoleService.get(concept.getIdentityRole());
			if (identityRole != null) {
				concept.setState(RoleRequestState.EXECUTED);
				concept.setIdentityRole(null); // we have to remove relation on
												// deleted identityRole
				String message = MessageFormat.format(
						"IdentityRole [{0}] (reqested in concept [{1}]) was deleted (from this role request).",
						identityRole.getId(), concept.getId());
				conceptRoleRequestService.addToLog(concept, message);
				conceptRoleRequestService.addToLog(request, message);
				conceptRoleRequestService.save(concept);
				identityRoleService.delete(identityRole);
			}
		});

		identityRoleService.saveAll(identityRolesToSave);
		conceptRoleRequestService.saveAll(conceptsToSave);
		request.setState(RoleRequestState.EXECUTED);
		return this.save(request);

	}
	
	@Override
	public IdmRoleRequestDto toDto(IdmRoleRequest entity, IdmRoleRequestDto dto) {
		IdmRoleRequestDto requestDto = super.toDto(entity, dto);
		// Set concepts to request DTO
		if (requestDto != null) {
			ConceptRoleRequestFilter conceptFilter = new ConceptRoleRequestFilter();
			conceptFilter.setRoleRequestId(requestDto.getId());
			requestDto.setConceptRoles(conceptRoleRequestService.findDto(conceptFilter, null).getContent());
		}
		
		if(requestDto != null && requestDto.getWfProcessId() != null){
			WorkflowProcessInstanceDto processDto = workflowProcessInstanceService.get(requestDto.getWfProcessId());
			// TODO: create trimmed variant in workflow process instance service
			if(processDto != null) {
				processDto.setProcessVariables(null);
			}
			requestDto.getEmbedded().put(IdmRoleRequestDto.WF_PROCESS_FIELD, processDto);
		}
		
		return requestDto;
	}

	@Override
	public IdmRoleRequest toEntity(IdmRoleRequestDto dto, IdmRoleRequest entity) {
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
			if (dto.getConceptRoles() == null) {
				dto.setConceptRoles(dtoPersisited.getConceptRoles());
			}
			if (dto.getWfProcessId() == null) {
				dto.setWfProcessId(dtoPersisited.getWfProcessId());
			}
			if (dto.getOriginalRequest() == null) {
				dto.setOriginalRequest(dtoPersisited.getOriginalRequest());
			}
		} else {
			dto.setState(RoleRequestState.CONCEPT);
		}

		return super.toEntity(dto, entity);

	}

	private boolean isDuplicated(IdmRoleRequestDto request, IdmRoleRequestDto duplicant) {

		if (request == duplicant) {
			return true;
		}
		if (request.getDescription() == null) {
			if (duplicant.getDescription() != null) {
				return false;
			}
		} else if (!request.getDescription().equals(duplicant.getDescription())) {
			return false;
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

	@Override
	public void addToLog(Loggable logItem, String text) {
		StringBuilder sb = new StringBuilder();
		sb.append(DateTime.now());
		sb.append(": ");
		sb.append(text);
		text = sb.toString();
		logItem.addToLog(text);
		LOG.info(text);

	}
	
	@Override
	public void delete(IdmRoleRequestDto dto, BasePermission... permission) {
		
		// Find all request where is this request duplicated and remove relation
		RoleRequestFilter conceptRequestFilter = new RoleRequestFilter();
		conceptRequestFilter.setDuplicatedToRequestId(dto.getId());
		this.findDto(conceptRequestFilter, null).getContent().forEach(duplicant -> {
			duplicant.setDuplicatedToRequest(null);
			if(RoleRequestState.DUPLICATED == duplicant.getState()){
				duplicant.setState(RoleRequestState.CONCEPT);
				duplicant.setDuplicatedToRequest(null);
			}
			String message = MessageFormat.format("Duplicated request [{0}] was deleted!", dto.getId());
			this.addToLog(duplicant, message);
			this.save(duplicant);
		});
		
		// Stop connected WF process
		cancelWF(dto);

		// First we have to delete all concepts for this request
		dto.getConceptRoles().forEach(concept -> {
			conceptRoleRequestService.delete(concept);
		});
		super.delete(dto);
	}
	
	@Override
	public void cancel(IdmRoleRequestDto dto) {
		cancelWF(dto);
		dto.setState(RoleRequestState.CANCELED);
		this.save(dto);
	}

	private void cancelWF(IdmRoleRequestDto dto) {
		if (!Strings.isNullOrEmpty(dto.getWfProcessId())) {
			WorkflowFilterDto filter = new WorkflowFilterDto();
			filter.setProcessInstanceId(dto.getWfProcessId());
			
			Collection<WorkflowProcessInstanceDto> resources = workflowProcessInstanceService.searchInternal(filter, false).getResources();
			if(resources.isEmpty()){
				// Process with this ID not exist ... maybe was ended 
				this.addToLog(dto,
						MessageFormat.format(
								"Workflow process with ID [{0}] was not deleted, because was not found. Maybe was ended before.",
								dto.getWfProcessId()));
				return;
			}
			
			workflowProcessInstanceService.delete(dto.getWfProcessId(),
					"Role request use this WF, was deleted. This WF was deleted too.");
			this.addToLog(dto,
					MessageFormat.format(
							"Workflow process with ID [{0}] was deleted, because this request is deleted/canceled",
							dto.getWfProcessId()));
		}
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
		identityRole.setRoleTreeNode(conceptRole.getRoleTreeNode());
		//
		// if exists role tree node, set automatic role
		if (conceptRole.getRoleTreeNode() != null) {
			identityRole.setAutomaticRole(true);
		}
		return identityRole;
	}

	private IdmRoleRequestService getIdmRoleRequestService() {
		if (this.roleRequestService == null) {
			this.roleRequestService = applicationContext.getBean(IdmRoleRequestService.class);
		}
		return this.roleRequestService;
	}
	
	private IdmRoleRequestDto validateOnDuplicity(IdmRoleRequestDto request) {
		List<IdmRoleRequestDto> potentialDuplicatedRequests = new ArrayList<>();

		RoleRequestFilter requestFilter = new RoleRequestFilter();
		requestFilter.setApplicantId(request.getApplicant());
		requestFilter.setState(RoleRequestState.IN_PROGRESS);
		potentialDuplicatedRequests.addAll(this.findDto(requestFilter, null).getContent());

		requestFilter.setState(RoleRequestState.APPROVED);
		potentialDuplicatedRequests.addAll(this.findDto(requestFilter, null).getContent());

		Optional<IdmRoleRequestDto> duplicatedRequestOptional = potentialDuplicatedRequests.stream()
				.filter(requestDuplicate -> {
					return isDuplicated(request, requestDuplicate) && !(request.getId() != null
							&& requestDuplicate.getId() != null && request.getId().equals(requestDuplicate.getId()));
				}).findFirst();

		if (duplicatedRequestOptional.isPresent()) {
			return duplicatedRequestOptional.get();
		}
		return null;
	}

	
	/**
	 * Trim request and his role concepts. Remove embedded objects.
	 * It is important for minimize size of dto persisted for example in WF process. 
	 * @param requestOriginal
	 */
	private void trimRequest(IdmRoleRequestDto requestOriginal) {
		requestOriginal.setLog(null);
		requestOriginal.setEmbedded(null);
		requestOriginal.getConceptRoles().forEach(concept -> {
			concept.setEmbedded(null);
			concept.setLog(null);
		});
	}
}
