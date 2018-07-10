package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Persists identifiable entity's confidential informations (passwords, token etc.)
 * 
 * @author Radek Tomiška
 */
public interface ConfidentialStorage {

	/**
	 * Persists (create, update) given value
	 * 
	 * @param ownerId values owner identifier
	 * @param ownerType owner type
	 * @param key identifier will be used to load storage value
	 * @param value
	 */
	void save(UUID ownerId, Class<? extends Identifiable> ownerType, String key, Serializable value);
	
	/**
	 * Deletes values from owner by given key
	 * 
	 * @param ownerId values owner identifier
	 * @param ownerType owner type
	 * @param key
	 */
	void delete(UUID ownerId, Class<? extends Identifiable> ownerType, String key);
	
	/**
	 * Deletes all values by owner id
	 * 
	 * @param ownerId
	 * @since 7.6.0
	 */
	void deleteAll(UUID ownerId, Class<? extends Identifiable> ownerType);
	
	/**
	 * Get value by owner and key
	 * 
	 * @param ownerId values owner identifier
	 * @param ownerType owner type
	 * @param key storage value identifier
	 * @return Serializable object - caller has to convert type
	 */
	Serializable get(UUID ownerId, Class<? extends Identifiable> ownerType, String key);
	
	/**
	 * Get value by owner and key casted to valueType, or null if storage value by given key is not found. 
	 *
	 * @param ownerId values owner identifier
	 * @param ownerType owner type
	 * @param key storage value identifier
	 * @param valueType result value type
	 * @return
	 * @throws IllegalArgumentException Throws {@link ClassCastException}, when persisted type is different from given valueType
	 */
	<T extends Serializable> T get(UUID ownerId, Class<? extends Identifiable> ownerType, String key, Class<T> valueType);
	
	/**
	 * Get value by owner and key casted to valueType, or default value if storage value by given key is not found. 
	 * Never throws IllegalArgumentException - returns default value when persisted type is different from given valueType.
	 * 
	 * @param ownerId values owner identifier
	 * @param ownerType owner type
	 * @param key
	 * @param valueType
	 * @param defaultValue
	 * @return
	 */
	<T extends Serializable> T get(UUID ownerId, Class<? extends Identifiable> ownerType, String key, Class<T> valueType, T defaultValue);
	
	/**
	 * Get value by owner and key as {@GuardedString}. Raw Serializable value is transformed toString internally. If value
	 * 
	 * @param ownerId values owner identifier
	 * @param key
	 * @return
	 */
	GuardedString getGuardedString(UUID ownerId, Class<? extends Identifiable> ownerType, String key);
	
	/**
	 * Persists (create, update) given guarded string
	 * @param ownerType owner type
	 * 
	 * @param ownerId values owner identifier
	 * @param key
	 * @param value
	 */
	void saveGuardedString(UUID ownerId, Class<? extends Identifiable> ownerType, String key, GuardedString value);

	/**
	 * Method read value from confidential storage with old key and resave value with new one.q
	 *
	 * @param ownerId
	 * @param ownerType
	 * @param key
	 * @param oldKey
	 */
	void changeCryptKey(UUID ownerId, Class<? extends Identifiable> ownerType, String key, GuardedString oldKey);
	
}
