package eu.bcvsolutions.idm.core.model.domain;

import eu.bcvsolutions.idm.core.model.entity.IdmRequestItem;

/**
 * Requestable entity interface
 * 
 * @author svandav
 *
 */
public interface Requestable {

	IdmRequestItem getRequestItem();

	void setRequestItem(IdmRequestItem requestItem);

}