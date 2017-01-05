package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Filter for synchronization config
 * 
 * @author Svanda
 *
 */
public class SynchronizationConfigFilter implements BaseFilter {

	private UUID systemId;

	public UUID getSystemId() {
		return systemId;
	}

	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}

}
