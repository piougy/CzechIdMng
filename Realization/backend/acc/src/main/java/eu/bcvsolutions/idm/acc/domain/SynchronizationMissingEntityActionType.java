package eu.bcvsolutions.idm.acc.domain;

/**
 * Type of action for missing entity synchronization situation 
 *  
 * @author Svanda
 *
 */
public enum SynchronizationMissingEntityActionType {

	CREATE_ENTITY, 
	DELETE_ACCOUNT,
	DISABLE_ACCOUNT,
	IGNORE;
}
