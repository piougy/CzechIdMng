package eu.bcvsolutions.idm.acc.domain;

/**
 * Type of all actions for synchronization 
 *  
 * @author Svanda
 *
 */
public enum SynchronizationActionType {

	CREATE_ENTITY,
	UPDATE_ENTITY, 
	DELETE_ENTITY,
	LINK_AND_UPDATE_ACCOUNT, //create link and produce entity save event (call provisioning)
	LINK,
	UNLINK,
	UNLINK_AND_REMOVE_ROLE,
	CREATE_ACCOUNT, // produce only entity save event (call provisioning)
	UPDATE_ACCOUNT, // produce only entity save event (call provisioning)
	IGNORE;
}
