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
	
	String REQUEST_ITEM_FIELD = "requestItem";
	String REQUEST_FIELD = "request";
	
	UUID getRequestItem();

	void setRequestItem(UUID request);

}