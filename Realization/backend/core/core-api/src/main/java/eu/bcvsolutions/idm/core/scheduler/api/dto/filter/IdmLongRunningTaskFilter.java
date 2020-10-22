package eu.bcvsolutions.idm.core.scheduler.api.dto.filter;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;

/**
 * Long running task filter.
 * 
 * @author Radek Tomiška
 *
 */
public class IdmLongRunningTaskFilter extends DataFilter {

	public static final String PARAMETER_OPERATION_STATE = "operationState";
	public static final String PARAMETER_TASK_TYPE = "taskType";
	public static final String PARAMETER_FROM = "from"; // => created from
	public static final String PARAMETER_TILL = "till"; // => created till
	public static final String PARAMETER_RUNNING = "running";
	public static final String PARAMETER_STATEFUL = "stateful";
	public static final String PARAMETER_INSTANCE_ID = ConfigurationService.PROPERTY_INSTANCE_ID;
	public static final String PARAMETER_CREATOR_ID = "creatorId";
	public static final String PARAMETER_INCLUDE_ITEM_COUNTS = "includeItemCounts"; // success, failed and warning count will be loaded.
	
	public IdmLongRunningTaskFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmLongRunningTaskFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public IdmLongRunningTaskFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(IdmLongRunningTaskDto.class, data, parameterConverter);
	}

	public OperationState getOperationState() {
		return getParameterConverter().toEnum(getData(), PARAMETER_OPERATION_STATE, OperationState.class);
	}
	
	public void setOperationState(OperationState operationState) {
		set(PARAMETER_OPERATION_STATE, operationState);
	}

	public String getTaskType() {
		return getParameterConverter().toString(getData(), PARAMETER_TASK_TYPE);
	}

	public void setTaskType(String taskType) {
		set(PARAMETER_TASK_TYPE, taskType);
	}

	public ZonedDateTime getFrom() {
		return getParameterConverter().toDateTime(getData(), PARAMETER_FROM);
	}

	public void setFrom(ZonedDateTime from) {
		set(PARAMETER_FROM, from);
	}

	public ZonedDateTime getTill() {
		return getParameterConverter().toDateTime(getData(), PARAMETER_TILL);
	}

	public void setTill(ZonedDateTime till) {
		set(PARAMETER_TILL, till);
	}
	
	public Boolean getRunning() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_RUNNING);
	}
	
	public void setRunning(Boolean running) {
		set(PARAMETER_RUNNING, running);
	}
	
	public void setStateful(Boolean stateful) {
		set(PARAMETER_STATEFUL, stateful);
	}
	
	public Boolean getStateful() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_STATEFUL);
	}
	
	/**
	 * Filter by server instance identifier.
	 *  
	 * @return server instance identifier
	 */
	public String getInstanceId() {
		return getParameterConverter().toString(getData(), PARAMETER_INSTANCE_ID);
	}
	
	/**
	 * Filter by server instance identifier.
	 * 
	 * @param instanceId server instance identifier
	 */
	public void setInstanceId(String instanceId) {
		set(PARAMETER_INSTANCE_ID, instanceId);
	}

	public UUID getCreatorId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_CREATOR_ID);
	}

	public void setCreatorId(UUID creatorId) {
		set(PARAMETER_CREATOR_ID, creatorId);
	}
	
	public boolean isIncludeItemCounts() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_INCLUDE_ITEM_COUNTS, false);
	}
	
	public void setIncludeItemCounts(boolean includeItemCounts) {
		set(PARAMETER_INCLUDE_ITEM_COUNTS, includeItemCounts);
	}
}
