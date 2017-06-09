package eu.bcvsolutions.idm.core.scheduler.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmProcessedTaskItem;

/**
 * Search filter for {@link IdmProcessedTaskItem}.
 * 
 * @author Jan Helbich
 *
 */
public class IdmProcessedTaskItemFilter extends DataFilter {

	private UUID longRunningTaskId;
	private UUID scheduledTaskId;
	private UUID referencedEntityId;
	private String referencedEntityType;
	
	public IdmProcessedTaskItemFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmProcessedTaskItemFilter(MultiValueMap<String, Object> data) {
		super(IdmProcessedTaskItemDto.class, data);
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

}
