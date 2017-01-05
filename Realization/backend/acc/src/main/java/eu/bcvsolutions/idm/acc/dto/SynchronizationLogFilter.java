package eu.bcvsolutions.idm.acc.dto;

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

	public UUID getSynchronizationConfigId() {
		return synchronizationConfigId;
	}

	public void setSynchronizationConfigId(UUID synchronizationConfigId) {
		this.synchronizationConfigId = synchronizationConfigId;
	}

}
