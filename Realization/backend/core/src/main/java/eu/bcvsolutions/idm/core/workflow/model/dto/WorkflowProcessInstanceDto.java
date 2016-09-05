package eu.bcvsolutions.idm.core.workflow.model.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkflowProcessInstanceDto {

	protected String processDefinitionId;
	protected String processDefinitionKey;
	protected String processDefinitionName;
	protected String activityId;
	protected String processInstanceId;
	protected String businessKey;
	protected String id;
	protected String name;
	protected boolean isEnded = false;
	protected Map<String, Object>  processVariables;
	protected String currentActivityName;
	protected String currentActivityDocumentation;
	protected WorkflowTaskDefinitionDto currentTaskDefinition;
	protected List<String> candicateUsers;

	public List<String> getCandicateUsers() {
		return candicateUsers;
	}

	public void setCandicateUsers(List<String> candicateUsers) {
		this.candicateUsers = candicateUsers;
	}

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}

	public void setProcessDefinitionKey(String processDefinitionKey) {
		this.processDefinitionKey = processDefinitionKey;
	}

	public String getProcessDefinitionName() {
		return processDefinitionName;
	}

	public void setProcessDefinitionName(String processDefinitionName) {
		this.processDefinitionName = processDefinitionName;
	}

	public String getActivityId() {
		return activityId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public String getBusinessKey() {
		return businessKey;
	}

	public void setBusinessKey(String businessKey) {
		this.businessKey = businessKey;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isEnded() {
		return isEnded;
	}

	public void setEnded(boolean isEnded) {
		this.isEnded = isEnded;
	}

	public Map<String, Object> getProcessVariables() {
		if (processVariables == null) {
			processVariables = new HashMap<>();
		}
		return processVariables;
	}

	public void setProcessVariables(Map<String, Object> processVariables) {
		this.processVariables = processVariables;
	}

	public WorkflowTaskDefinitionDto getCurrentTaskDefinition() {
		return currentTaskDefinition;
	}

	public void setCurrentTaskDefinition(WorkflowTaskDefinitionDto currentTaskDefinition) {
		this.currentTaskDefinition = currentTaskDefinition;
	}

	public String getCurrentActivityName() {
		return currentActivityName;
	}

	public void setCurrentActivityName(String currentActivityName) {
		this.currentActivityName = currentActivityName;
	}

	public String getCurrentActivityDocumentation() {
		return currentActivityDocumentation;
	}

	public void setCurrentActivityDocumentation(String currentActivityDocumentation) {
		this.currentActivityDocumentation = currentActivityDocumentation;
	}

}
