package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import org.joda.time.DateTime;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.ModifiedFromFilter;

/**
 * Filter for synchronization item log
 * 
 * @author Svanda
 *
 */
public class SysSyncItemLogFilter implements BaseFilter, ModifiedFromFilter {

	private UUID syncActionLogId;
	private String displayName; //Search with like
	private UUID systemId;
	private DateTime modifiedFrom;

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

	public UUID getSystemId() {
		return systemId;
	}

	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}

	/* (non-Javadoc)
	 * @see eu.bcvsolutions.idm.acc.dto.filter.ModifiedFromFilter#getModifiedFrom()
	 */
	@Override
	public DateTime getModifiedFrom() {
		return modifiedFrom;
	}

	/* (non-Javadoc)
	 * @see eu.bcvsolutions.idm.acc.dto.filter.ModifiedFromFilter#setModifiedFrom(org.joda.time.DateTime)
	 */
	@Override
	public void setModifiedFrom(DateTime modifiedFrom) {
		this.modifiedFrom = modifiedFrom;
	}
}
