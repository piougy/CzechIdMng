package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Filter for synchronization action log
 * 
 * @author Svanda
 *
 */
public class SyncActionLogFilter implements BaseFilter {

	private UUID synchronizationLogId;

	public UUID getSynchronizationLogId() {
		return synchronizationLogId;
	}

	public void setSynchronizationLogId(UUID synchronizationLogId) {
		this.synchronizationLogId = synchronizationLogId;
	}
}
