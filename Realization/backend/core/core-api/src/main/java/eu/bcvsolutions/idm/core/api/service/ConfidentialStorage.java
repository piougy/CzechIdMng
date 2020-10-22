package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConfidentialStorageValueDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Persists identifiable entity's confidential informations (passwords, token etc.)
 * 
 * {@link AbstractEntity} type or {@link AbstractDto} can be used as owner type. 
 * Underlying {@link AbstractEntity} has to extend {@link FormableEntity}. 
 * If {@link AbstractDto} is given as owner type, then {@link FormableEntity} owner will be found by 
 * {@link LookupService} => transformation to {@link FormableEntity}. 
 * 
 * @see LookupService
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
	 * Persists (create, update) given value
	 * 
	 * @param owner
	 * @param key
	 * @param value
	 * @since 8.2.0
	 */
	void save(Identifiable owner, String key, Serializable value);
	
	/**
	 * Deletes values from owner by given key
	 * 
	 * @param ownerId values owner identifier
	 * @param ownerType owner type
	 * @param key
	 */
	void delete(UUID ownerId, Class<? extends Identifiable> ownerType, String key);
	
	/**
	 * Deletes values from owner by given key
	 * 
	 * @param owner
	 * @param key
	 * @since 8.2.0
	 */
	void delete(Identifiable owner, String key);
	
	/**
	 * Deletes all values by owner id
	 * 
	 * @param ownerId
	 * @since 7.6.0
	 */
	void deleteAll(UUID ownerId, Class<? extends Identifiable> ownerType);
	
	/**
	 * Deletes all values by owner
	 * 
	 * @param owner
	 * @since 8.2.0
	 */
	void deleteAll(Identifiable owner);
	
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
	 * Get value by owner and key
	 * @param owner
	 * @param key storage value identifier
	 * @return Serializable object - caller has to convert type
	 * @since 8.2.0
	 */
	Serializable get(Identifiable owner, String key);
	
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
	 * Get value by owner and key casted to valueType, or null if storage value by given key is not found. 
	 *
	 * @param owner
	 * @param key storage value identifier
	 * @param valueType result value type
	 * @return
	 * @throws IllegalArgumentException Throws {@link ClassCastException}, when persisted type is different from given valueType
	 * @since 8.2.0
	 */
	<T extends Serializable> T get(Identifiable owner, String key, Class<T> valueType);
	
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
	 * Get value by owner and key casted to valueType, or default value if storage value by given key is not found. 
	 * Never throws IllegalArgumentException - returns default value when persisted type is different from given valueType.
	 * 
	 * @param owner
	 * @param key
	 * @param valueType
	 * @param defaultValue
	 * @return
	 * @since 8.2.0
	 */
	<T extends Serializable> T get(Identifiable owner, String key, Class<T> valueType, T defaultValue);
	
	/**
	 * Get value by owner and key as {@GuardedString}. Raw Serializable value is transformed toString internally. If value
	 * 
	 * @param ownerId values owner identifier
	 * @param ownerType
	 * @param key
	 * @return
	 */
	GuardedString getGuardedString(UUID ownerId, Class<? extends Identifiable> ownerType, String key);
	
	/**
	 * Get value by owner and key as {@GuardedString}. Raw Serializable value is transformed toString internally. If value
	 * 
	 * @param owner
	 * @param key
	 * @return
	 * @since 8.2.0
	 */
	GuardedString getGuardedString(Identifiable owner, String key);
	
	/**
	 * Persists (create, update) given guarded string
	 * 
	 * @param ownerType owner type
	 * @param ownerId values owner identifier
	 * @param key
	 * @param value
	 */
	void saveGuardedString(UUID ownerId, Class<? extends Identifiable> ownerType, String key, GuardedString value);
	
	/**
	 * Persists (create, update) given guarded string
	 * 
	 * @param owner
	 * @param key
	 * @param value
	 * @since 8.2.0
	 */
	void saveGuardedString(Identifiable owner, String key, GuardedString value);

	/**
	 * Method read value from confidential storage with old crypt key and resave value with new one (configured).
	 *
	 * @param ownerId
	 * @param ownerType
	 * @param key
	 * @param oldCryptKey
	 * @since 8.2.0
	 */	
	void changeCryptKey(IdmConfidentialStorageValueDto value, GuardedString oldCryptKey);
	
	/**
	 * Returns owner type - owner type has to be entity class - dto class can be given.
	 * Its used as default definition type for given owner type.
	 * 
	 * @param ownerType
	 * @return
	 * @since 8.2.0
	 */
	String getOwnerType(Identifiable owner);
	
	/**
	 * Returns owner type - owner type has to be entity class - dto class can be given.
	 * Its used as default definition type for given owner type.
	 * 
	 * @param ownerType
	 * @return
	 * @since 8.2.0
	 */
	String getOwnerType(Class<? extends Identifiable> ownerType);
	
}
