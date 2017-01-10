package eu.bcvsolutions.idm.acc.domain;

/**
 * Type of action for account missing (entity exists, but account on system missing) synchronization situation. 
 *  
 * @author Svanda
 *
 */
public enum ReconciliationMissingAccountActionType {

	CREATE_ACCOUNT, 
	DELETE_ENTITY,
	IGNORE;
}
