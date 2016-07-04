package eu.bcvsolutions.idm.core.workflow.model.dto;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WorkflowHistoricProcessInstanceDto {

	private String processDefinitionId;
	private String id;
	private String name;
	/** The time the process was started. */
	private Date startTime;
	/** The time the process was ended. */
	private Date endTime;
	/**
	 * The difference between {@link #getEndTime()} and {@link #getStartTime()}
	 */
	private Long durationInMillis;
	/**
	 * The authenticated user that started this process instance.
	 * 
	 * @see IdentityService#setAuthenticatedUserId(String)
	 */
	private String startUserId;
	/** The start activity. */
	private String startActivityId;
	/** Obtains the reason for the process instance's deletion. */
	private String deleteReason;
	/**
	 * The process instance id of a potential super process instance or null if
	 * no super process instance exists
	 */
	private String superProcessInstanceId;
	private Map<String, Object> processVariables;

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
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

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Long getDurationInMillis() {
		return durationInMillis;
	}

	public void setDurationInMillis(Long durationInMillis) {
		this.durationInMillis = durationInMillis;
	}

	public String getStartUserId() {
		return startUserId;
	}

	public void setStartUserId(String startUserId) {
		this.startUserId = startUserId;
	}

	public String getStartActivityId() {
		return startActivityId;
	}

	public void setStartActivityId(String startActivityId) {
		this.startActivityId = startActivityId;
	}

	public String getDeleteReason() {
		return deleteReason;
	}

	public void setDeleteReason(String deleteReason) {
		this.deleteReason = deleteReason;
	}

	public String getSuperProcessInstanceId() {
		return superProcessInstanceId;
	}

	public void setSuperProcessInstanceId(String superProcessInstanceId) {
		this.superProcessInstanceId = superProcessInstanceId;
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

}
