package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Filter for synchronization item log
 * 
 * @author Svanda
 *
 */
public class SysSyncItemLogFilter implements BaseFilter {

	private UUID syncActionLogId;
	private String displayName; //Search with like

	public UUID getSyncActionLogId() {
		return syncActionLogId;
	}

	public void setSyncActionLogId(UUID syncActionLogId) {
		this.syncActionLogId = syncActionLogId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

}
