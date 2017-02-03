package eu.bcvsolutions.idm.scheduler.dto.filter;

import org.joda.time.DateTime;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;

/**
 * Long running task filter
 * 
 * @author Radek Tomiška
 *
 */
public class LongRunningTaskFilter extends QuickFilter {

	private OperationState operationState;
	private String taskType;
	private DateTime from;
	private DateTime till;
	private Boolean running;

	public OperationState getOperationState() {
		return operationState;
	}
	
	public void setOperationState(OperationState operationState) {
		this.operationState = operationState;
	}

	public String getTaskType() {
		return taskType;
	}

	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}

	public DateTime getFrom() {
		return from;
	}

	public void setFrom(DateTime from) {
		this.from = from;
	}

	public DateTime getTill() {
		return till;
	}

	public void setTill(DateTime till) {
		this.till = till;
	}
	
	public Boolean getRunning() {
		return running;
	}
	
	public void setRunning(Boolean running) {
		this.running = running;
	}
}
