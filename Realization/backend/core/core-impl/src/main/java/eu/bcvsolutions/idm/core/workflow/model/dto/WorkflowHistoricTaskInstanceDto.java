package eu.bcvsolutions.idm.core.workflow.model.dto;

import java.util.Date;

import org.activiti.engine.IdentityService;
import org.springframework.hateoas.core.Relation;

/**
 * DTO for historic workflow task
 * 
 * @author svandav
 *
 */
//TODO: Use in the since version 8.x.x @Relation(collectionRelation = "workflowHistoricTaskInstances")
@Relation(collectionRelation = "resources")
public class WorkflowHistoricTaskInstanceDto extends WorkflowTaskInstanceDto {

	private static final long serialVersionUID = 1L;
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
	/** Obtains the reason for the process instance's deletion. */
	private String deleteReason;
	private Date createTime;
	private Date dueDate;
	private String completeTaskDecision;
	private String completeTaskMessage;

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

	public String getDeleteReason() {
		return deleteReason;
	}

	public void setDeleteReason(String deleteReason) {
		this.deleteReason = deleteReason;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public String getCompleteTaskDecision() {
		return completeTaskDecision;
	}

	public void setCompleteTaskDecision(String completeTaskDecision) {
		this.completeTaskDecision = completeTaskDecision;
	}

	public String getCompleteTaskMessage() {
		return completeTaskMessage;
	}

	public void setCompleteTaskMessage(String completeTaskMessage) {
		this.completeTaskMessage = completeTaskMessage;
	}

}
