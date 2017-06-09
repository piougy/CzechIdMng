package eu.bcvsolutions.idm.acc.domain;

/**
 * Types of situations for synchronization
 * @author svandav
 *
 */
public enum SynchronizationSituationType {

	LINKED(SynchronizationActionType.LINKED, SynchronizationLinkedActionType.class),
	MISSING_ENTITY(SynchronizationActionType.MISSING_ENTITY,SynchronizationMissingEntityActionType.class),
	UNLINKED(SynchronizationActionType.UNLINKED,SynchronizationUnlinkedActionType.class),
	MISSING_ACCOUNT(SynchronizationActionType.MISSING_ACCOUNT,SynchronizationMissingEntityActionType.class);
	
	private Class<?> enumeration;
	private SynchronizationActionType action;

	private SynchronizationSituationType(SynchronizationActionType action, Class<?> enumeration) {
		this.enumeration = enumeration;
		this.action = action;
	}

	public Class<?> getSituationEnumClass() {
		return this.enumeration;
	}
	public SynchronizationActionType getAction() {
		return this.action;
	}
}
