package eu.bcvsolutions.idm.ic.api;

/**
 * Abstract "place-holder" for synchronization. The application must not make
 * any attempt to interpret the value of the token. From the standpoint of the
 * application the token is merely a black-box. The application may only persist
 * the value of the token for use on subsequent synchronization requests.
 * <p>
 * What this token represents is entirely connector-specific. On some connectors
 * this might be a last-modified value. On others, it might be a unique ID of a
 * log table entry. On others such as JMS, this might be a dummy value since JMS
 * itself keeps track of the state of the sync.
 */
public interface IcSyncToken {

	/**
	 * Returns the value for the token.
	 *
	 * @return The value for the token.
	 */
	Object getValue();

}