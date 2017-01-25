package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Filter for synchronization item log
 * 
 * @author Svanda
 *
 */
public class SyncItemLogFilter implements BaseFilter {

	private UUID syncActionLogId;

	public UUID getSyncActionLogId() {
		return syncActionLogId;
	}

	public void setSyncActionLogId(UUID syncActionLogId) {
		this.syncActionLogId = syncActionLogId;
	}

}
