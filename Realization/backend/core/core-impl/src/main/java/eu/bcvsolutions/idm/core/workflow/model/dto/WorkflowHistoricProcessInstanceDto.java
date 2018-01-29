package eu.bcvsolutions.idm.core.workflow.model.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.IdentityService;
import org.springframework.hateoas.core.Relation;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;

/**
 * 
 * @author svandav
 *
 */
@Relation(collectionRelation = "workflowHistoricProcessInstances")
public class WorkflowHistoricProcessInstanceDto implements BaseDto {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
	@JsonProperty(value = "_trimmed", access=Access.READ_ONLY)
	private boolean trimmed = false;
	
	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	public String getId() {
		return id;
	}
	
	@Override
	public void setId(Serializable id) {
		if (id != null) {
			Assert.isInstanceOf(String.class, id, "WorkflowHistoricTaskInstanceDto supports only String identifier.");
		}
		this.id = (String) id;
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

	public boolean isTrimmed() {
		return trimmed;
	}

	public void setTrimmed(boolean trimmed) {
		this.trimmed = trimmed;
	}

}
