package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RequestState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.exception.RoleRequestException;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmRequestService;
import eu.bcvsolutions.idm.core.api.service.RequestManager;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.event.RequestEvent;
import eu.bcvsolutions.idm.core.model.event.RequestEvent.RequestEventType;
import eu.bcvsolutions.idm.core.model.event.processor.role.RoleRequestApprovalProcessor;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;

/**
 * Default implementation of universal request manager
 * 
 * @author svandav
 *
 */
@Service("requestManager")
public class DefaultRequestManager
		implements RequestManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(DefaultRequestManager.class);

	@Autowired
	private SecurityService securityService;
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private WorkflowProcessInstanceService workflowProcessInstanceService;
	@Autowired
	private EntityEventManager entityEventManager;
	@Autowired
	private IdmRequestService requestService;
	private RequestManager requestManager;



	@Override
	@Transactional
	public IdmRequestDto startRequest(UUID requestId, boolean checkRight) {
		IdmRequestDto request = requestService.get(requestId);
		Assert.notNull(request, "Request is required!");
		return request;

//		// Validation on exist some rule
//		if (RequestType.ATTRIBUTE == request.getRequestType()
//				&& RequestOperationType.REMOVE != request.getOperation()) {
//			IdmAutomaticRoleAttributeRuleRequestFilter ruleFilter = new IdmAutomaticRoleAttributeRuleRequestFilter();
//			ruleFilter.setRoleRequestId(requestId);
//
//			List<IdmAutomaticRoleAttributeRuleRequestDto> ruleConcepts = automaticRoleRuleRequestService
//					.find(ruleFilter, null).getContent();
//			if (ruleConcepts.isEmpty()) {
//				throw new RoleRequestException(CoreResultCode.AUTOMATIC_ROLE_REQUEST_START_WITHOUT_RULE,
//						ImmutableMap.of("request", request.getName()));
//			}
//		}
//
//		try {
//			IdmRequestService service = this.getIdmRequestService();
//			if (!(service instanceof DefaultIdmRequestService)) {
//				throw new CoreException("We expects instace of DefaultIdmRequestService!");
//			}
//			return ((DefaultIdmRequestService) service).startRequestNewTransactional(requestId,
//					checkRight);
//		} catch (Exception ex) {
//			LOG.error(ex.getLocalizedMessage(), ex);
//			request = get(requestId);
//			Throwable exceptionToLog = resolveException(ex);
//
//			// TODO: I set only cause of exception, not code and properties. If are
//			// properties set, then request cannot be save!
//			request.setResult(
//					new OperationResultDto.Builder(OperationState.EXCEPTION).setCause(exceptionToLog).build());
//			request.setState(RequestState.EXCEPTION);
//			return save(request);
//		}
	}

	/**
	 * Internal start request. Start in new transaction
	 * 
	 * @param requestId
	 * @param checkRight
	 *            - If is true, then will be check right for immediately execution
	 *            (if is requires)
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public IdmRequestDto startRequestNewTransactional(UUID requestId, boolean checkRight) {
		return this.getRequestManager().startRequestInternal(requestId, checkRight);
	}

	@Override
	@Transactional
	public IdmRequestDto startRequestInternal(UUID requestId, boolean checkRight) {
		LOG.debug("Start role request [{}]", requestId);
		Assert.notNull(requestId, "Role request ID is required!");
		// Load request ... check right for read
		IdmRequestDto request = requestService.get(requestId);
		Assert.notNull(request, "Role request DTO is required!");
		Assert.isTrue(
				RequestState.CONCEPT == request.getState() || RequestState.DUPLICATED == request.getState()
						|| RequestState.EXCEPTION == request.getState(),
				"Only role request with CONCEPT or EXCEPTION or DUPLICATED state can be started!");

		// Request will be set on in progress state
		request.setState(RequestState.IN_PROGRESS);
		request.setResult(new OperationResultDto.Builder(OperationState.RUNNING).build());
		IdmRequestDto savedRequest = requestService.save(request);

		// Throw event
		Map<String, Serializable> variables = new HashMap<>();
		variables.put(RoleRequestApprovalProcessor.CHECK_RIGHT_PROPERTY, checkRight);
		return entityEventManager
				.process(new RequestEvent(RequestEventType.EXECUTE, savedRequest, variables))
				.getContent();
	}

	@Override
	@Transactional
	public boolean startApprovalProcess(IdmRequestDto request, boolean checkRight,
			EntityEvent<IdmRequestDto> event, String wfDefinition) {
		// If is request marked as executed immediately, then we will check right
		// and do realization immediately (without start approval process)
		if (request.isExecuteImmediately()) {
			boolean haveRightExecuteImmediately = securityService
					.hasAnyAuthority(CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_ADMIN);

			if (checkRight && !haveRightExecuteImmediately) {
				throw new RoleRequestException(CoreResultCode.ROLE_REQUEST_NO_EXECUTE_IMMEDIATELY_RIGHT,
						ImmutableMap.of("new", request));
			}

			// Execute request immediately
			return true;
		} else {

			Map<String, Object> variables = new HashMap<>();
			// Minimize size of DTO persisting to WF
			IdmRequestDto eventRequest = event.getContent();
			eventRequest.setEmbedded(null);
			variables.put(EntityEvent.EVENT_PROPERTY, event);
			variables.put("approvalForAutomaticRole", Boolean.TRUE);

			ProcessInstance processInstance = workflowProcessInstanceService.startProcess(wfDefinition,
					IdmRoleDto.class.getSimpleName(), request.getCreator(), request.getCreatorId().toString(),
					variables);
			// We have to refresh request (maybe was changed in wf process)
			request = requestService.get(request.getId());
			request.setWfProcessId(processInstance.getProcessInstanceId());
			requestService.save(request);
		}

		return false;
	}

	@Override
	@Transactional
	public IdmRequestDto executeRequest(UUID requestId) {
		// We can`t catch and log exception to request, because this transaction will be
		// marked as to rollback.
		// We can`t run this method in new transaction, because changes on request
		// (state modified in WF for example) is in uncommited transaction!
		return this.executeRequestInternal(requestId);
	}

	private IdmRequestDto executeRequestInternal(UUID requestId) {
		Assert.notNull(requestId, "Role request ID is required!");
		IdmRequestDto request = requestService.get(requestId);
		Assert.notNull(request, "Role request is required!");

//		UUID automaticRoleId = request.getAutomaticRole();
//
//		if (RequestType.ATTRIBUTE == request.getRequestType()) {
//			// Automatic role by attributes
//			if (RequestOperationType.REMOVE == request.getOperation()) {
//				// Remove automatic role by attributes
//				Assert.notNull(automaticRoleId, "Id of automatic role in the request (for delete) is required!");
//				automaticRoleAttributeService.delete(automaticRoleAttributeService.get(automaticRoleId));
//				request.setAutomaticRole(null);
//			} else {
//				// Add new or update (rules) for automatic role by attributes
//				IdmAutomaticRoleAttributeDto automaticRole = null;
//				if (automaticRoleId != null) {
//					automaticRole = automaticRoleAttributeService.get(automaticRoleId);
//				} else {
//					automaticRole = new IdmAutomaticRoleAttributeDto();
//					automaticRole = initAttributeAutomaticRole(request, automaticRole);
//					automaticRole = automaticRoleAttributeService.save(automaticRole);
//					request.setAutomaticRole(automaticRole.getId());
//				}
//				UUID roleId = automaticRole.getRole() != null ? automaticRole.getRole() : request.getRole();
//				Assert.notNull(roleId, "Id of role is required in the automatic role request!");
//
//				IdmRoleDto role = roleService.get(request.getRole());
//				Assert.notNull(role, "Role is required in the automatic role request!");
//
//				// Before we do any change, we have to sets the automatic role to concept state
//				automaticRole.setConcept(true);
//				automaticRoleAttributeService.save(automaticRole);
//
//				// Realize changes for rules
//				realizeAttributeRules(request, automaticRole, ruleConcepts);
//
//				// Sets automatic role as no concept -> execute recalculation this role
//				automaticRole.setConcept(false);
//				automaticRoleAttributeService.recalculate(automaticRoleAttributeService.save(automaticRole).getId());
//
//			}
//		} else if (RequestType.TREE == request.getRequestType()) {
//			// Automatic role by node in a tree
//			if (RequestOperationType.REMOVE == request.getOperation()) {
//				// Remove tree automatic role
//				Assert.notNull(automaticRoleId, "Id of automatic role in the request (for delete) is required!");
//				// Recount (remove) assigned roles ensures LRT during delete
//				automaticRoleTreeService.delete(automaticRoleTreeService.get(automaticRoleId));
//				request.setAutomaticRole(null);
//
//			} else if (RequestOperationType.ADD == request.getOperation()) {
//				// Create new tree automatic role
//				IdmRoleTreeNodeDto treeAutomaticRole = new IdmRoleTreeNodeDto();
//				treeAutomaticRole = initTreeAutomaticRole(request, treeAutomaticRole);
//				// Recount of assigned roles ensures LRT after save 
//				treeAutomaticRole = automaticRoleTreeService.save(treeAutomaticRole);
//				request.setAutomaticRole(treeAutomaticRole.getId());
//			} else {
//				// Update is not supported
//				throw new ResultCodeException(CoreResultCode.METHOD_NOT_ALLOWED,
//						"Tree automatic role update is not supported");
//			}
//		}

		request.setState(RequestState.EXECUTED);
		request.setResult(new OperationResultDto.Builder(OperationState.EXECUTED).build());
		return requestService.save(request);

	}



	@Override
	@Transactional
	public void cancel(IdmRequestDto dto) {
		cancelWF(dto);
		dto.setState(RequestState.CANCELED);
		dto.setResult(new OperationResultDto(OperationState.CANCELED));
		requestService.save(dto);
	}


	/**
	 * Fill the audit fields. We want to use original creator from request,
	 * otherwise the creator from the last approver would be used.
	 * 
	 * @param request
	 * @param automaticRole
	 */
	private void fillAuditFields(IdmRequestDto request, AbstractDto automaticRole) {
		automaticRole.setOriginalCreator(request.getOriginalCreator());
		automaticRole.setOriginalModifier(request.getOriginalModifier());
	}


	/**
	 * Cancel unfinished workflow process for this automatic role.
	 * 
	 * @param dto
	 */
	private void cancelWF(IdmRequestDto dto) {
		if (!Strings.isNullOrEmpty(dto.getWfProcessId())) {
			WorkflowFilterDto filter = new WorkflowFilterDto();
			filter.setProcessInstanceId(dto.getWfProcessId());

			Collection<WorkflowProcessInstanceDto> resources = workflowProcessInstanceService.find(filter, null)
					.getContent();
			if (resources.isEmpty()) {
				// Process with this ID not exist ... maybe was ended
				return;
			}

			workflowProcessInstanceService.delete(dto.getWfProcessId(),
					"Role request use this WF, was deleted. This WF was deleted too.");
		}
	}

	private RequestManager getRequestManager() {
		if (this.requestManager == null) {
			this.requestManager = applicationContext.getBean(RequestManager.class);
		}
		return this.requestManager;
	}

	/**
	 * If exception causal chain contains cause instance of ResultCodeException,
	 * then is return primary.
	 * 
	 * TODO: nice util method
	 * 
	 * @param ex
	 * @return
	 */
	private Throwable resolveException(Exception ex) {
		Assert.notNull(ex);
		Throwable exceptionToLog = null;
		List<Throwable> causes = Throwables.getCausalChain(ex);
		// If is some cause instance of ResultCodeException, then we will use only it
		// (for better show on frontend)
		Throwable resultCodeException = causes.stream().filter(cause -> {
			if (cause instanceof ResultCodeException) {
				return true;
			}
			return false;
		}).findFirst().orElse(null);

		exceptionToLog = resultCodeException != null ? resultCodeException : ex;
		return exceptionToLog;
	}

}
