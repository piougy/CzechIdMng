package eu.bcvsolutions.idm.core.api.domain;

import java.util.UUID;

/**
 * Requestable DTO interface
 * 
 * @author svandav
 *
 */
public interface Requestable {

	UUID getRequestItem();

	void setRequestItem(UUID requestItem);

}