package eu.bcvsolutions.idm.acc.domain;

/**
 * Type of action for unlinked (account on system and entity exists, but link not exist) synchronization situation. 
 *  
 * @author Svanda
 *
 */
public enum SynchronizationUnlinkedActionType {

	LINK,
	LINK_AND_UPDATE_ACCOUNT, // create link and produce entity save event (call provisioning)
	IGNORE;
}
