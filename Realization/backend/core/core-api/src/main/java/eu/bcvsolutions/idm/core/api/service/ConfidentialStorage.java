package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

/**
 * Persists abstract entity's confidential informations (passwords, token etc.)
 * 
 * @author Radek Tomiška
 *
 */
public interface ConfidentialStorage {

	/**
	 * Persists (create, update) given value
	 * 
	 * @param owner values owner
	 * @param key identifier will be used to load storage value
	 * @param value
	 */
	<O extends AbstractEntity> void save(O owner, String key, Serializable value);
	
	/**
	 * Deletes values from owner by given key
	 * 
	 * @param owner
	 * @param key
	 */
	<O extends AbstractEntity> void delete(O owner, String key);
	
	/**
	 * Get value by owner and key
	 * 
	 * @param owner values owner
	 * @param key storage value identifier
	 * @return Serializable object - caller has to convert type
	 */
	<O extends AbstractEntity> Serializable get(O owner, String key);
	
	/**
	 * Get value by owner and key casted to valueType, or null if storage value by given key is not found. 
	 *
	 * @param owner values owner
	 * @param key storage value identifier
	 * @param valueType result value type
	 * @return
	 * @throws IllegalArgumentException Throws {@link ClassCastException}, when persisted type is different from given valueType
	 */
	<O extends AbstractEntity, T extends Serializable> T get(O owner, String key, Class<T> valueType);
	
	/**
	 * Get value by owner and key casted to valueType, or default value if storage value by given key is not found. 
	 * Never throws IllegalArgumentException - returns default value when persisted type is different from given valueType.
	 * 
	 * @param owner
	 * @param key
	 * @param valueType
	 * @param defaultValue
	 * @return
	 */
	<O extends AbstractEntity, T extends Serializable> T get(O owner, String key, Class<T> valueType, T defaultValue);
	
	/**
	 * Get value by owner and key as {@GuardedString}. Raw Serializable value is transformed toString internally. If value
	 * 
	 * @param owner
	 * @param key
	 * @return
	 */
	<O extends AbstractEntity> GuardedString getGuardedString(O owner, String key);
	
	/**
	 * Persists (create, update) given guarded string
	 * 
	 * @param owner
	 * @param key
	 * @param value
	 */
	<O extends AbstractEntity> void saveGuardedString(O owner, String key, GuardedString value);
	
}
