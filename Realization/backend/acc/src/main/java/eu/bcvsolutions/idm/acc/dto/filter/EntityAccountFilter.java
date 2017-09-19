package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Interface for all entity - account filters
 * 
 * @author svandav
 * @author Radek Tomiška
 */
public interface EntityAccountFilter extends BaseFilter {
	
	Boolean isOwnership();

	void setOwnership(Boolean ownership);

	UUID getAccountId();

	void setAccountId(UUID accountId);

	void setEntityId(UUID entityId);

	void setSystemId(UUID systemId);

}