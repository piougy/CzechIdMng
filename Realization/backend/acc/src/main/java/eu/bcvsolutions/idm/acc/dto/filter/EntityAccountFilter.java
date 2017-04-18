package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

/**
 * Interface for all entity - account filters
 * @author svandav
 *
 */
public interface EntityAccountFilter {

	Boolean isOwnership();

	void setOwnership(Boolean ownership);

	UUID getAccountId();

	void setAccountId(UUID accountId);

}