package eu.bcvsolutions.idm.acc.domain;

/**
 * Type of action for unlinked (account on system and entity exists, but link not exist) synchronization situation. 
 *  
 * @author Svanda
 *
 */
public enum SynchronizationUnlinkedActionType {

	LINK(SynchronizationActionType.CREATE_ENTITY),
	LINK_AND_UPDATE_ACCOUNT(SynchronizationActionType.LINK_AND_UPDATE_ACCOUNT), // create link and produce entity save event (call provisioning)
	IGNORE(SynchronizationActionType.IGNORE);
	
	private SynchronizationActionType action;

	private SynchronizationUnlinkedActionType(SynchronizationActionType action) {
		this.action = action;
	}

	public SynchronizationActionType getAction() {
		return this.action;
	}
}
