package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Filter for synchronization log
 * 
 * @author Svanda
 *
 */
public class SynchronizationLogFilter implements BaseFilter {

	private UUID synchronizationConfigId;
	private Boolean running;
	
	public UUID getSynchronizationConfigId() {
		return synchronizationConfigId;
	}

	public void setSynchronizationConfigId(UUID synchronizationConfigId) {
		this.synchronizationConfigId = synchronizationConfigId;
	}

	public Boolean getRunning() {
		return running;
	}

	public void setRunning(Boolean running) {
		this.running = running;
	}
}
