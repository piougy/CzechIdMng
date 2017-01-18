package eu.bcvsolutions.idm.acc.domain;

/**
 * Type of action for account missing (entity exists, but account on system missing) synchronization situation. 
 *  
 * @author Svanda
 *
 */
public enum ReconciliationMissingAccountActionType {

	CREATE_ACCOUNT, // produce only entity save event (call provisioning)
	DELETE_ENTITY,
	UNLINK,
	UNLINK_AND_REMOVE_ROLE,
	IGNORE;
}
