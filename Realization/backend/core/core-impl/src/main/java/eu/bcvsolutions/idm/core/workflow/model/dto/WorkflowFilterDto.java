package eu.bcvsolutions.idm.core.workflow.model.dto;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;

/**
 * Universal workflow filter for process and tasks.
 *
 * @author Vít Švanda
 *
 * FIXME: filter is used in bulk action and cannot be saved and persisted into DB (DataFilter is required to asynchronous bulk action processing).
 *
 */
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
	// If true, then returns only objects where logged user is involved.
	// Cannot be set via REST (security reasons)!
	private Boolean onlyInvolved = Boolean.TRUE;
	
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

	/**
	 * Returns only objects where logged user is involved.
	 * If false or null, then check on involved is skip.
	 *
	 * Super admin, skip this always.
	 */
	public Boolean getOnlyInvolved() {
		return onlyInvolved;
	}

	/**
	 * Returns only objects where logged user is involved.
	 * If false or null, then check on involved is skip.
	 *
	 * Super admin, skip this always.
	 */
	public void setOnlyInvolved(Boolean onlyInvolved) {
		this.onlyInvolved = onlyInvolved;
	}
}
