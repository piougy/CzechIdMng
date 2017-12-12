package eu.bcvsolutions.idm.core.scheduler.api.dto.filter;

import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;

/**
 * Search filter for {@link IdmProcessedTaskItemDto}.
 *
 * @author Jan Helbich
 *
 */
public class IdmProcessedTaskItemFilter extends DataFilter {

	private UUID longRunningTaskId;
	private UUID scheduledTaskId;
	private UUID referencedEntityId;
	private String referencedEntityType;
	private OperationState operationState;
	private DateTime from;
	private DateTime till;

	public IdmProcessedTaskItemFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public IdmProcessedTaskItemFilter(MultiValueMap<String, Object> data) {
		super(IdmProcessedTaskItemDto.class, data);
	}

	public OperationState getOperationState(){
		return operationState;
	}

	public void setOperationState(OperationState operationState){
		this.operationState = operationState;
	}

	public UUID getLongRunningTaskId() {
		return longRunningTaskId;
	}

	public void setLongRunningTaskId(UUID longRunningTaskId) {
		this.longRunningTaskId = longRunningTaskId;
	}

	public UUID getScheduledTaskId() {
		return scheduledTaskId;
	}

	public void setScheduledTaskId(UUID scheduledProcessId) {
		this.scheduledTaskId = scheduledProcessId;
	}

	public String getReferencedEntityType() {
		return referencedEntityType;
	}

	public void setReferencedEntityType(String referencedEntityType) {
		this.referencedEntityType = referencedEntityType;
	}

	public UUID getReferencedEntityId() {
		return referencedEntityId;
	}

	public void setReferencedEntityId(UUID referencedEntityId) {
		this.referencedEntityId = referencedEntityId;
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
}
