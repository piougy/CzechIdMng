package eu.bcvsolutions.idm.acc.domain;

/**
 * Type of action for account missing (entity exists, but account on system missing) synchronization situation. 
 *  
 * @author Svanda
 *
 */
public enum ReconciliationMissingAccountActionType {

	CREATE_ACCOUNT(SynchronizationActionType.CREATE_ACCOUNT), // produce only entity save event (call provisioning)
	DELETE_ENTITY(SynchronizationActionType.DELETE_ENTITY),
	UNLINK(SynchronizationActionType.UNLINK),
	UNLINK_AND_REMOVE_ROLE(SynchronizationActionType.UNLINK_AND_REMOVE_ROLE),
	IGNORE(SynchronizationActionType.IGNORE);
	
	private SynchronizationActionType action;

	private ReconciliationMissingAccountActionType(SynchronizationActionType action) {
		this.action = action;
	}

	public SynchronizationActionType getAction() {
		return this.action;
	}
}
