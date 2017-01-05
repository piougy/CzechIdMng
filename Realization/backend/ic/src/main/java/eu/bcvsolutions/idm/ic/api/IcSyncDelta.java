package eu.bcvsolutions.idm.ic.api;

import eu.bcvsolutions.idm.ic.impl.IcSyncDeltaTypeEnum;

public interface IcSyncDelta {

	/**
	 * If the change described by this <code>IcSyncDeltaImpl</code> modified the
	 * object's Uid, this method returns the Uid before the change. Not all
	 * resources can determine the previous Uid, so this method can return
	 * <code>null</code>.
	 *
	 * @return the previous Uid or null if it could not be determined or the
	 *         change did not modify the Uid.
	 */
	IcUidAttribute getPreviousUid();

	/**
	 * If the change described by this <code>IcSyncDeltaImpl.DELETE</code> and
	 * the deleted object value is <code>null</code>, this method returns the
	 * ObjectClass of the deleted object. If operation syncs
	 * {@link org.identityconnectors.framework.common.objects.ObjectClass#ALL}
	 * this must be set, otherwise this method can return <code>null</code>.
	 *
	 * @return the ObjectClass of the deleted object.
	 */
	IcObjectClass getObjectClass();

	/**
	 * Returns the Uid of the connector object that changed.
	 *
	 * @return The Uid.
	 */
	IcUidAttribute getUid();

	/**
	 * Returns the connector object that changed. This may be null in the case
	 * of delete.
	 *
	 * @return The object or possibly null if this represents a delete.
	 */
	IcConnectorObject getObject();

	/**
	 * Returns the <code>SyncToken</code> of the object that changed.
	 *
	 * @return the <code>SyncToken</code> of the object that changed.
	 */
	IcSyncToken getToken();

	/**
	 * Returns the type of the change the occured.
	 *
	 * @return The type of change that occured.
	 */
	IcSyncDeltaTypeEnum getDeltaType();

}