package eu.bcvsolutions.idm.core.scheduler.api.dto.filter;

import org.joda.time.DateTime;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;

/**
 * Long running task filter
 * 
 * @author Radek Tomiška
 *
 */
public class IdmLongRunningTaskFilter extends DataFilter {

	private OperationState operationState;
	private String taskType;
	private DateTime from;
	private DateTime till;
	private Boolean running;
	private Boolean stateful;
	
	public IdmLongRunningTaskFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmLongRunningTaskFilter(MultiValueMap<String, Object> data) {
		super(IdmLongRunningTaskDto.class, data);
	}

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
	
	public void setStateful(Boolean stateful) {
		this.stateful = stateful;
	}
	
	public Boolean getStateful() {
		return stateful;
	}
}
