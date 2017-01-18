package eu.bcvsolutions.idm.acc.domain;

/**
 * Type of action for linked synchronization situation 
 *  
 * @author Svanda
 *
 */
public enum SynchronizationLinkedActionType {

	UPDATE_ENTITY,
	UPDATE_ACCOUNT, // produce only entity save event (call provisioning)
	UNLINK,
	UNLINK_AND_REMOVE_ROLE,
	IGNORE;
}
