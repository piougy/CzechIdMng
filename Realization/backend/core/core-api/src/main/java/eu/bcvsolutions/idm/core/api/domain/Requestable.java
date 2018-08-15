package eu.bcvsolutions.idm.core.api.domain;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;

/**
 * Requestable DTO interface
 * 
 * @author svandav
 *
 */
public interface Requestable extends BaseDto {
	
	public final String REQUEST_ITEM_FIELD = "requestItem";
	public final String REQUEST_FIELD = "request";
	
	UUID getRequest();

	void setRequest(UUID requestItem);
	
	UUID getRequestItem();

	void setRequestItem(UUID request);

}