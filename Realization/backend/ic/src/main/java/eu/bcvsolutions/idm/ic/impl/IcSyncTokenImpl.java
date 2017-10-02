package eu.bcvsolutions.idm.ic.impl;

import eu.bcvsolutions.idm.ic.api.IcSyncToken;

/**
 * Abstract "place-holder" for synchronization.
 */
public final class IcSyncTokenImpl implements IcSyncToken {

	private Object value;

	public IcSyncTokenImpl(Object value) {
		this.value = value;
	}

	/**
	 * Returns the value for the token.
	 *
	 * @return The value for the token.
	 */
	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "SyncToken: " + value;
	}

}
