package eu.bcvsolutions.idm.core.workflow.model.dto;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;

public class WorkflowFilterDto extends QuickFilter {
	
	private Map<String, Object> equalsVariables;
	private String processDefinitionId;
	private String processDefinitionKey;
	private String processInstanceId;
	private String superProcessInstanceId;
	private String name;
	private String category;
	private String candidateOrAssigned;
	private ZonedDateTime createdBefore;
	private ZonedDateTime createdAfter;
	
	public WorkflowFilterDto() {
	}
	
	public Map<String, Object> getEqualsVariables() {
		if (equalsVariables == null) {
			equalsVariables = new HashMap<>();
		}
		return equalsVariables;
	}

	public void setEqualsVariables(Map<String, Object> equalsVariables) {
		this.equalsVariables = equalsVariables;
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

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String id) {
		this.processInstanceId = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSuperProcessInstanceId() {
		return superProcessInstanceId;
	}

	public void setSuperProcessInstanceId(String superProcessInstanceId) {
		this.superProcessInstanceId = superProcessInstanceId;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCandidateOrAssigned() {
		return candidateOrAssigned;
	}

	public void setCandidateOrAssigned(String candidateOrAssigned) {
		this.candidateOrAssigned = candidateOrAssigned;
	}

	public ZonedDateTime getCreatedBefore() {
		return createdBefore;
	}

	public void setCreatedBefore(ZonedDateTime createdBefore) {
		this.createdBefore = createdBefore;
	}

	public ZonedDateTime getCreatedAfter() {
		return createdAfter;
	}

	public void setCreatedAfter(ZonedDateTime createdAfter) {
		this.createdAfter = createdAfter;
	}
}
