package eu.bcvsolutions.idm.core.workflow.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.rest.AbstractBaseDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;

/**
 * Default implementation of workflow process instance service
 * 
 * @author svandav
 * @author Radek Tomiška
 */
@Service
public class DefaultWorkflowProcessInstanceService 
		extends AbstractBaseDtoService<WorkflowProcessInstanceDto, WorkflowFilterDto> 
		implements WorkflowProcessInstanceService {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultWorkflowProcessInstanceService.class);

	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private SecurityService securityService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private TaskService taskService;

	@Override
	@Transactional
	public ProcessInstance startProcess(String definitionKey, String objectType, String applicant,
			String objectIdentifier, Map<String, Object> variables) {
		Assert.hasText(definitionKey, "Definition key cannot be null!");
		UUID implementerId = securityService.getCurrentId();
		UUID originalImplementerId = securityService.getOriginalId();
 
		IdmIdentityDto applicantIdentity = null;
		if (applicant != null) {
			applicantIdentity = identityService.getByUsername(applicant);
		}
		ProcessInstanceBuilder builder = runtimeService.createProcessInstanceBuilder()
				.processDefinitionKey(definitionKey)//
				.variable(WorkflowProcessInstanceService.OBJECT_TYPE, objectType)
				.variable(WorkflowProcessInstanceService.ACTIVITI_SKIP_EXPRESSION_ENABLED, Boolean.TRUE) // Allow skip expression on user task
				.variable(WorkflowProcessInstanceService.OBJECT_IDENTIFIER, objectIdentifier)
				.variable(WorkflowProcessInstanceService.IMPLEMENTER_IDENTIFIER, implementerId == null ? null : implementerId.toString())
				.variable(WorkflowProcessInstanceService.ORIGINAL_IMPLEMENTER_IDENTIFIER, originalImplementerId == null ? null : originalImplementerId.toString())
				.variable(WorkflowProcessInstanceService.APPLICANT_USERNAME, applicant)
				.variable(WorkflowProcessInstanceService.APPLICANT_IDENTIFIER,
						applicantIdentity != null ? applicantIdentity.getId() : null);
		if (variables != null) {
			for (Entry<String, Object> entry : variables.entrySet()) {
				builder.variable(entry.getKey(), entry.getValue());
			}
		}

		ProcessInstance instance = builder.start();
		if (!instance.isEnded()) {
			// must explicit check null, else throw org.activiti.engine.ActivitiIllegalArgumentException: userId and groupId cannot both be null
			if (applicantIdentity != null) {
				// Set applicant as owner of process.
				runtimeService.addUserIdentityLink(instance.getId(), applicantIdentity.getId().toString(), IdentityLinkType.OWNER);
			}
			if (implementerId != null) {
				// Set current logged user (implementer) as starter of process.
				runtimeService.addUserIdentityLink(instance.getId(), implementerId.toString(), IdentityLinkType.STARTER);
			}
			if (originalImplementerId != null && !originalImplementerId.equals(implementerId)) {
				// Set original logged user (original implementer) as participant
				runtimeService.addUserIdentityLink(instance.getId(), originalImplementerId.toString(), IdentityLinkType.PARTICIPANT);
			}
			// Search subprocesses and add create links to add access for applicant and implementer.
			runtimeService
				.createProcessInstanceQuery()
				.active()
				.includeProcessVariables()
				.superProcessInstanceId(instance.getId())
				.list()
				.forEach(subProcess-> {
					subProcess.getProcessVariables().forEach((k, v) -> {
						if (WorkflowProcessInstanceService.APPLICANT_IDENTIFIER.equals(k)) {
							String value = v == null ? null : v.toString();
							// Set applicant as owner of process.
							runtimeService.addUserIdentityLink(subProcess.getProcessInstanceId(), value, IdentityLinkType.OWNER);
							LOG.debug("StartProcesEventListener - set process [{}]-[{}] owner [{}]",
									subProcess.getName(), subProcess.getProcessInstanceId(), value);
						} else if (WorkflowProcessInstanceService.IMPLEMENTER_IDENTIFIER.equals(k)) {
							String value = v == null ? null : v.toString();
							// Set implementer as starter of process.
							runtimeService.addUserIdentityLink(subProcess.getProcessInstanceId(), value, IdentityLinkType.STARTER);
							LOG.debug("StartProcesEventListener - set process [{}]-[{}] starter [{}]",
									subProcess.getName(), subProcess.getProcessInstanceId(), value);
						} else if (WorkflowProcessInstanceService.ORIGINAL_IMPLEMENTER_IDENTIFIER.equals(k)) {
							String value = v == null ? null : v.toString();
							// Set original implementer as participant of process.
							runtimeService.addUserIdentityLink(subProcess.getProcessInstanceId(), value, IdentityLinkType.PARTICIPANT);
							LOG.debug("StartProcesEventListener - set process [{}]-[{}] participant [{}]",
									subProcess.getName(), subProcess.getProcessInstanceId(), value);
						}
					});
				});
		}
		return instance;
	}
	
	@Override
	public WorkflowProcessInstanceDto get(Serializable id, BasePermission... permission) {
		Assert.notNull(id, "Identifier is required.");
		return this.get(String.valueOf(id));
	}
	
	@Override
	public Page<WorkflowProcessInstanceDto> find(WorkflowFilterDto filter, Pageable pageable, BasePermission... permission) {
		if (pageable == null) {
			// pageable is required now
			pageable = PageRequest.of(0, Integer.MAX_VALUE);
		}
		
		String processDefinitionId = filter.getProcessDefinitionId();
		Map<String, Object> equalsVariables = filter.getEqualsVariables();
		ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

		query.active();
		query.includeProcessVariables();

		if (processDefinitionId != null) {
			query.processDefinitionId(processDefinitionId);
		}
		if (filter.getProcessDefinitionKey() != null) {
			query.processDefinitionKey(filter.getProcessDefinitionKey());
		}
		if (filter.getProcessInstanceId() != null) {
			query.processInstanceId(filter.getProcessInstanceId());
		}
		if (filter.getCategory() != null) {
			// Find definitions with this category (use double sided like)
			// We have to find definitions first, because process instance can't
			// be find by category.
			ProcessDefinitionQuery queryDefinition = repositoryService.createProcessDefinitionQuery();
			queryDefinition.active();
			queryDefinition.latestVersion();
			queryDefinition.processDefinitionCategoryLike(filter.getCategory() + "%");
			List<ProcessDefinition> processDefinitions = queryDefinition.list();
			Set<String> processDefinitionKeys = new HashSet<>();
			processDefinitions.forEach(p -> processDefinitionKeys.add(p.getKey()));
			if (processDefinitionKeys.isEmpty()) {
				// We don`t have any definitions ... nothing must be returned
				processDefinitionKeys.add("-1");
			}
			query.processDefinitionKeys(processDefinitionKeys);
		}
		if (equalsVariables != null) {
			for (Entry<String, Object> entry : equalsVariables.entrySet()) {
				query.variableValueEquals(entry.getKey(), entry.getValue());
			}
		}
		// check security ... only involved user or applicant can work with process instance
		// Applicant and Implementer is added to involved user after process
		// (subprocess) started. This modification allow not use OR clause.
		boolean checkRight = !ObjectUtils.isEmpty(PermissionUtils.trimNull(permission));
		if (checkRight && !securityService.isAdmin()){
			UUID currentId = securityService.getCurrentId();
			if (currentId == null) {
				currentId = UUID.randomUUID();
			}
			query.involvedUser(currentId.toString());
		}

		if (pageable.getSort() != null) {
			LOG.warn("Sort is not supported, will be ignored.");
		}
		
		query.orderByProcessDefinitionId();
		query.desc();
		long count = query.count();
		List<ProcessInstance> processInstances = query.listPage((pageable.getPageNumber()) * pageable.getPageSize(), pageable.getPageSize());
		List<WorkflowProcessInstanceDto> dtos = new ArrayList<>();

		if (processInstances != null) {
			for (ProcessInstance instance : processInstances) {
				dtos.add(toResource(instance));
			}
		}

		return new PageImpl<WorkflowProcessInstanceDto>(dtos, pageable, count);
	}
	
	@Override
	public Page<WorkflowProcessInstanceDto> find(Pageable pageable, BasePermission... permission) {
		return this.find(new WorkflowFilterDto(), pageable, permission);
	}
	
	@Override
	public WorkflowProcessInstanceDto get(String processInstanceId) {
		return get(processInstanceId, false);
	}
	
	@Override
	public WorkflowProcessInstanceDto get(String processInstanceId, boolean checkRight) {
		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setProcessInstanceId(processInstanceId);
		List<WorkflowProcessInstanceDto> resources = this
				.find(filter, PageRequest.of(0, 1), checkRight ? IdmBasePermission.READ : null)
				.getContent();
		return !resources.isEmpty() ? resources.get(0) : null;
	}
	
	@Override
	public void delete(WorkflowProcessInstanceDto dto, BasePermission... permission) {
		this.delete(dto.getId(), null);
	}
	
	@Override
	public void deleteById(Serializable id, BasePermission... permission) {
		this.delete(String.valueOf(id), null);
	}
	
	@Override
	public void deleteInternalById(Serializable id) {
		this.delete(String.valueOf(id), null);
	}
	
	@Override
	public void deleteInternal(WorkflowProcessInstanceDto dto) {
		this.delete(dto.getId(), null);
	}

	@Override
	public Set<IdmIdentityDto> getApproversForSubprocess(String processInstaceId) {
		if (processInstaceId == null) {
			return Sets.newHashSet();
		}

		Set<IdmIdentityDto> identities = new HashSet<IdmIdentityDto>();
		
		// All subprocess
		List<ProcessInstance> list = runtimeService
				.createProcessInstanceQuery()
				.superProcessInstanceId(processInstaceId)
				.list();

		// Iterate over subprocess and get approvers for each subprocess
		for (ProcessInstance instance : list) {
			identities.addAll(getApproversForProcess(instance.getId()));
		}

		return identities;
	}

	@Override
	public WorkflowProcessInstanceDto delete(String processInstanceId, String deleteReason) {
		if (processInstanceId == null) {
			return null;
		}
		if (deleteReason == null) {
			StringBuilder message = new StringBuilder("Deleted by [");
			String username = securityService.getUsername();
			message.append(username);
			// resolve switched user
			String originalUsername = securityService.getOriginalUsername();
			if (StringUtils.isNotEmpty(originalUsername) && !StringUtils.equals(username, originalUsername)) {
				message.append("], original user [");
				message.append(originalUsername);
			}
			message.append("].");
			//
			deleteReason = message.toString();
		}
		
		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setProcessInstanceId(processInstanceId);
		
		List<WorkflowProcessInstanceDto> resources = this.find(filter, null).getContent();
		WorkflowProcessInstanceDto processInstanceToDelete = null;
		if(!resources.isEmpty()){
			processInstanceToDelete = resources.get(0);
		}

		if (processInstanceToDelete == null) {
			throw new ResultCodeException(CoreResultCode.FORBIDDEN,
					"You do not have permission for delete process instance with ID: %s !",
					ImmutableMap.of("processInstanceId", processInstanceId));
		}
		runtimeService.deleteProcessInstance(processInstanceToDelete.getProcessInstanceId(), deleteReason);

		return processInstanceToDelete;
	}

	@Override
	public Set<IdmIdentityDto> getApproversForProcess(String processInstaceId) {
		Task task = taskService.createTaskQuery().active().processInstanceId(processInstaceId).singleResult();
		Set<IdmIdentityDto> approvers = new HashSet<>();
		
		if (task != null) {
			// Get all identity links for task id
			List<HistoricIdentityLink> identityLinks = historyService.getHistoricIdentityLinksForTask(task.getId());
			if (identityLinks != null && !identityLinks.isEmpty()) {
				for	(HistoricIdentityLink identity : identityLinks) {
					if (IdentityLinkType.CANDIDATE.equals(identity.getType())) {
						IdmIdentityDto identityDto = identityService.get(identity.getUserId());
						if (identityDto != null) {
							approvers.add(identityDto);
						}
					}
				}
			}
		}
		// Include approvers by subprocess
		approvers.addAll(this.getApproversForSubprocess(processInstaceId));

		return approvers;
	}

	private WorkflowProcessInstanceDto toResource(ProcessInstance instance) {
		if (instance == null) {
			return null;
		}

		String instanceName = instance.getName();
		// If we don't have process name, then we try variable with key
		// processInstanceName
		if (instanceName == null && instance.getProcessVariables() != null && instance.getProcessVariables()
				.containsKey(WorkflowHistoricProcessInstanceService.PROCESS_INSTANCE_NAME)) {
			instanceName = (String) instance.getProcessVariables()
					.get(WorkflowHistoricProcessInstanceService.PROCESS_INSTANCE_NAME);
		}
		if (instanceName == null || instanceName.isEmpty()) {
			instanceName = instance.getProcessDefinitionName();
		}

		WorkflowProcessInstanceDto dto = new WorkflowProcessInstanceDto();
		dto.setId(instance.getId());
		dto.setActivityId(instance.getActivityId());
		dto.setBusinessKey(instance.getBusinessKey());
		dto.setName(instanceName);
		dto.setProcessDefinitionId(instance.getProcessDefinitionId());
		dto.setProcessDefinitionKey(instance.getProcessDefinitionKey());
		dto.setProcessDefinitionName(instance.getProcessDefinitionName());
		dto.setProcessVariables(instance.getProcessVariables());
		dto.setEnded(instance.isEnded());
		dto.setProcessInstanceId(instance.getProcessInstanceId());
		// Add current activity name and documentation
		BpmnModel model = repositoryService.getBpmnModel(instance.getProcessDefinitionId());
		
		for (FlowElement element : model.getMainProcess().getFlowElements()) {
			if (element.getId().equals(instance.getActivityId())) {
				dto.setCurrentActivityName(element.getName());
				dto.setCurrentActivityDocumentation(element.getDocumentation());
			}
		}
		
		Task task = taskService.createTaskQuery().processInstanceId(instance.getProcessInstanceId()).singleResult();
		
		if (task != null) {
			List<HistoricIdentityLink> identityLinks = historyService.getHistoricIdentityLinksForTask(task.getId());
			if (identityLinks != null && !identityLinks.isEmpty()) {
				List<String> candicateUsers = new ArrayList<>();
				for	(HistoricIdentityLink identity : identityLinks) {
					if (IdentityLinkType.CANDIDATE.equals(identity.getType())) {
						candicateUsers.add(identity.getUserId());
					}
				}
				dto.setCandicateUsers(candicateUsers);
			}
		}
		
		return dto;
	}
}
