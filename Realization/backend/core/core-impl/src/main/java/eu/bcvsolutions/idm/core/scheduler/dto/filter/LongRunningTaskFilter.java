package eu.bcvsolutions.idm.core.scheduler.dto.filter;

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
public class LongRunningTaskFilter extends DataFilter {

	private OperationState operationState;
	private String taskType;
	private DateTime from;
	private DateTime till;
	private Boolean running;
	
	public LongRunningTaskFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public LongRunningTaskFilter(MultiValueMap<String, Object> data) {
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
}
