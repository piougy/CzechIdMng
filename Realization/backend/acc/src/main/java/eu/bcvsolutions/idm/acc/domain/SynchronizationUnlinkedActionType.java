package eu.bcvsolutions.idm.acc.domain;

/**
 * Type of action for unlinked (account on system and entity exists, but link not exist) synchronization situation. 
 *  
 * @author Svanda
 *
 */
public enum SynchronizationUnlinkedActionType {

	LINK_AND_UPDATE_ENTITY,
	IGNORE;
}
