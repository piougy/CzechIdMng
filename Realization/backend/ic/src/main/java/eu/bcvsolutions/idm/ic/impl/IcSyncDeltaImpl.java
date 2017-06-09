
package eu.bcvsolutions.idm.ic.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcSyncDelta;
import eu.bcvsolutions.idm.ic.api.IcSyncToken;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;

/**
 * Represents a change to an object in a resource.
 *
 * @see SyncApiOp
 * @see SyncOp
 */
public final class IcSyncDeltaImpl implements IcSyncDelta {
	private final IcSyncToken token;
	private final IcSyncDeltaTypeEnum deltaType;
	private final IcUidAttribute previousUid;
	private final IcObjectClass objectClass;
	private final IcUidAttribute uid;
	private final IcConnectorObject connectorObject;

	/**
	 * Creates a IcSyncDeltaImpl.
	 *
	 * @param token
	 *            The token. Must not be null.
	 * @param deltaType
	 *            The delta. Must not be null.
	 * @param uid
	 *            The uid. Must not be null.
	 * @param object
	 *            The object that has changed. May be null for delete.
	 */
	public IcSyncDeltaImpl(IcSyncToken token, IcSyncDeltaTypeEnum deltaType, IcUidAttribute previousUid,
			IcObjectClass objectClass, IcUidAttribute uid, IcConnectorObject object) {
		Assert.notNull(token);
		Assert.notNull(deltaType);
		Assert.notNull(uid);

		// do not allow previous Uid for anything else than create or update
		if (previousUid != null
				&& (deltaType == IcSyncDeltaTypeEnum.DELETE || deltaType == IcSyncDeltaTypeEnum.CREATE)) {
			throw new IllegalArgumentException(
					"The previous Uid can only be specified for create_or_update or update.");
		}

		// only allow null object for delete
		if (object == null && deltaType != IcSyncDeltaTypeEnum.DELETE) {
			throw new IllegalArgumentException("ConnectorObject must be specified for anything other than delete.");
		}

		// if object not null, make sure its Uid matches
		// if (object != null && !uid.equals(object.getUid())) {
		// throw new IllegalArgumentException("Uid does not match that of the
		// object.");
		// }
		if (object != null && !objectClass.equals(object.getObjectClass())) {
			throw new IllegalArgumentException("ObjectClass does not match that of the object.");
		}

		this.token = token;
		this.deltaType = deltaType;
		this.previousUid = previousUid;
		this.objectClass = objectClass;
		this.uid = uid;
		this.connectorObject = object;
	}

	/**
	 * If the change described by this <code>IcSyncDeltaImpl</code> modified the
	 * object's Uid, this method returns the Uid before the change. Not all
	 * resources can determine the previous Uid, so this method can return
	 * <code>null</code>.
	 *
	 * @return the previous Uid or null if it could not be determined or the
	 *         change did not modify the Uid.
	 */
	public IcUidAttribute getPreviousUid() {
		return previousUid;
	}

	/**
	 * If the change described by this <code>IcSyncDeltaImpl.DELETE</code> and
	 * the deleted object value is <code>null</code>, this method returns the
	 * ObjectClass of the deleted object. If operation syncs
	 * {@link org.identityconnectors.framework.common.objects.ObjectClass#ALL}
	 * this must be set, otherwise this method can return <code>null</code>.
	 *
	 * @return the ObjectClass of the deleted object.
	 */
	public IcObjectClass getObjectClass() {
		return objectClass;
	}

	/**
	 * Returns the Uid of the connector object that changed.
	 *
	 * @return The Uid.
	 */
	public IcUidAttribute getUid() {
		return uid;
	}

	/**
	 * Returns the connector object that changed. This may be null in the case
	 * of delete.
	 *
	 * @return The object or possibly null if this represents a delete.
	 */
	public IcConnectorObject getObject() {
		return connectorObject;
	}

	/**
	 * Returns the <code>SyncToken</code> of the object that changed.
	 *
	 * @return the <code>SyncToken</code> of the object that changed.
	 */
	public IcSyncToken getToken() {
		return token;
	}

	/**
	 * Returns the type of the change the occured.
	 *
	 * @return The type of change that occured.
	 */
	public IcSyncDeltaTypeEnum getDeltaType() {
		return deltaType;
	}

	@Override
	public String toString() {
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("Token", token);
		values.put("DeltaType", deltaType);
		values.put("PreviousUid", previousUid);
		values.put("ObjectClass", objectClass);
		values.put("Uid", uid);
		values.put("Object", connectorObject);
		return values.toString();
	}

	@Override
	public int hashCode() {
		int result = token.hashCode();
		result = 31 * result + deltaType.hashCode();
		result = 31 * result + (previousUid != null ? previousUid.hashCode() : 0);
		result = 31 * result + (objectClass != null ? objectClass.hashCode() : 0);
		result = 31 * result + uid.hashCode();
		result = 31 * result + (connectorObject != null ? connectorObject.hashCode() : 0);
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof IcSyncDeltaImpl) {
			IcSyncDeltaImpl other = (IcSyncDeltaImpl) o;
			if (!token.equals(other.token)) {
				return false;
			}
			if (!deltaType.equals(other.deltaType)) {
				return false;
			}
			if (previousUid == null) {
				if (other.previousUid != null) {
					return false;
				}
			} else if (!previousUid.equals(other.previousUid)) {
				return false;
			}
			if (objectClass == null) {
				if (other.objectClass != null) {
					return false;
				}
			} else if (!objectClass.equals(other.objectClass)) {
				return false;
			}
			if (!uid.equals(other.uid)) {
				return false;
			}
			if (connectorObject == null) {
				if (other.connectorObject != null) {
					return false;
				}
			} else if (!connectorObject.equals(other.connectorObject)) {
				return false;
			}
			return true;
		}
		return false;
	}
}
