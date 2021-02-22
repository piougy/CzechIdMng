package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.UUID;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmConfidentialStorageValueDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.entity.IdmConfidentialStorageValue;
import eu.bcvsolutions.idm.core.model.repository.IdmConfidentialStorageValueRepository;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.service.CryptService;

/**
 * "Naive" confidential storage. Values are persisted in standard database.
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmConfidentialStorage implements ConfidentialStorage {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmConfidentialStorage.class);
	private final IdmConfidentialStorageValueRepository repository;
	private final CryptService cryptService;
	private LookupService lookupService;
	//
	@Autowired private ApplicationContext context;
	
	@Autowired
	public DefaultIdmConfidentialStorage(
			IdmConfidentialStorageValueRepository repository,
			CryptService encryptService) {
		Assert.notNull(repository, "Confidential storage repository is required");
		Assert.notNull(encryptService, "Service is required.");
		//
		this.repository = repository;
		this.cryptService = encryptService;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public void save(UUID ownerId, Class<? extends Identifiable> ownerType, String key, Serializable value) {
		Assert.notNull(ownerId, "Owner identifier is required");
		Assert.notNull(ownerType, "Owner type is required.");
		Assert.hasLength(key,"Key is required.");
		//
		LOG.debug("Saving value for owner [{},{}] and key [{}] to confidential storage", ownerId, ownerType, key);
		IdmConfidentialStorageValue storage = getStorageValue(ownerId, ownerType, key);
		if (storage == null) {
			// create new storage
			storage = new IdmConfidentialStorageValue();
			storage.setOwnerType(getOwnerType(ownerType));
			storage.setOwnerId(ownerId);
			storage.setKey(key);
		}

		byte[] vector = cryptService.generateVector();
		// Set new IV vector
		storage.setIv(vector);
		// set storage value
		storage.setValue(toStorageValue(value, vector));
		// persist
		repository.save(storage);
	}
	
	@Override
	@Transactional
	public void save(Identifiable owner, String key, Serializable value) {
		Assert.notNull(owner, "Owner is required.");
		//
		save(getOwnerId(owner), owner.getClass(), key, value);
	}

	@Override
	@Transactional
	public void changeCryptKey(IdmConfidentialStorageValueDto value, GuardedString oldCryptKey) {
		Assert.notNull(value, "Value is required.");
		//
		IdmConfidentialStorageValue storage = getStorageValue(value.getOwnerId(), value.getOwnerType(), value.getKey());
		Assert.notNull(storage, "Storage is required.");
		//
		// decrypt value with old key
		byte[] decryptedValue = cryptService.decryptWithKey(storage.getValue(), oldCryptKey, value.getIv());
		// create new IV
		byte[] vector = cryptService.generateVector();
		storage.setIv(vector);
		// and crypt value with new key
		storage.setValue(cryptService.encrypt(decryptedValue, vector));
		// persist new value
		repository.save(storage);
	}
	
	@Override
	@Transactional
	public void delete(UUID ownerId, Class<? extends Identifiable> ownerType, String key) {
		Assert.notNull(ownerId, "Owner identifier is required");
		Assert.notNull(ownerType, "Owner type is required.");
		Assert.hasLength(key, "Key is required.");
		//
		LOG.debug("Delete value for owner [{},{}] and key [{}] from confidential storage", ownerId, ownerType, key);
		IdmConfidentialStorageValue storageValue = getStorageValue(ownerId, ownerType, key);
		if (storageValue != null) {
			repository.delete(storageValue);
		}
	}
	
	@Override
	@Transactional
	public void delete(Identifiable owner, String key) {
		Assert.notNull(owner, "Owner is required.");
		//
		delete(getOwnerId(owner), owner.getClass(), key);
	}
	
	@Override
	@Transactional
	public void deleteAll(UUID ownerId, Class<? extends Identifiable> ownerType) {
		Assert.notNull(ownerId, "Owner identifier is required");
		Assert.notNull(ownerType, "Owner type is required.");
		//
		LOG.debug("Delete all values for owner [{},{}] from confidential storage", ownerId, ownerType);
		this.repository.deleteByOwnerIdAndOwnerType(ownerId, getOwnerType(ownerType));
	}
	
	@Override
	@Transactional
	public void deleteAll(Identifiable owner) {
		Assert.notNull(owner, "Owner is required.");
		//
		deleteAll(getOwnerId(owner), owner.getClass());
	}
	
	@Override
	public boolean exists(UUID ownerId, Class<? extends Identifiable> ownerType, String key) {
		Assert.notNull(ownerId, "Owner identifier is required");
		Assert.notNull(ownerType, "Owner type is required.");
		Assert.hasLength(key, "Key is required.");
		//
		return getStorageValue(ownerId, ownerType, key) != null;
	}

	@Override
	public Serializable get(UUID ownerId, Class<? extends Identifiable> ownerType, String key) {
		Assert.notNull(ownerId, "Owner identifier is required");
		Assert.notNull(ownerType, "Owner type is required.");
		Assert.hasLength(key, "Key is required.");
		//
		IdmConfidentialStorageValue storageValue = getStorageValue(ownerId, ownerType, key);
		return storageValue == null ? null : fromStorageValue(storageValue.getValue(), storageValue.getIv());
	}
	
	@Override
	public Serializable get(Identifiable owner, String key) {
		Assert.notNull(owner, "Owner is required.");
		//
		return get(getOwnerId(owner), owner.getClass(), key);
	}
	
	@Override
	public <T extends Serializable> T get(UUID ownerId, Class<? extends Identifiable> ownerType, String key, Class<T> valueType) {
		Serializable storageValue = get(ownerId, ownerType, key);
		if (storageValue == null) {
			return null;
		}
		try {
			return valueType.cast(storageValue);
		} catch(ClassCastException ex) {
			throw new IllegalArgumentException(
					MessageFormat.format("Storage value [{0}] with type [{1}] could not be cast to type [{2}]",
							key, storageValue.getClass(), valueType), ex);
		}
	}
	
	@Override
	public <T extends Serializable> T get(Identifiable owner, String key, Class<T> valueType) {
		Assert.notNull(owner, "Owner is required.");
		//
		return get(getOwnerId(owner), owner.getClass(), key, valueType);
	}

	@Override
	public <T extends Serializable> T get(UUID ownerId, Class<? extends Identifiable> ownerType, String key, Class<T> valueType, T defaultValue) {
		try {
			T value = get(ownerId, ownerType, key, valueType);
			return value != null ? value : defaultValue;
		} catch(IllegalArgumentException ex) {
			LOG.debug(MessageFormat.format("Storage value [{0}] could not be cast to type [{1}], returning default value", key, valueType), ex);
			return defaultValue;
		}
	} 
	
	@Override
	public <T extends Serializable> T get(Identifiable owner, String key, Class<T> valueType, T defaultValue) {
		Assert.notNull(owner, "Owner is required.");
		//
		return get(getOwnerId(owner), owner.getClass(), key, valueType, defaultValue);
	}
	
	@Override
	public GuardedString getGuardedString(UUID ownerId, Class<? extends Identifiable> ownerType, String key) {
		Serializable storageValue = get(ownerId, ownerType, key);
		if (storageValue == null) {
			return new GuardedString();
		}
		return new GuardedString(storageValue.toString());
	}
	
	@Override
	public GuardedString getGuardedString(Identifiable owner, String key) {
		Assert.notNull(owner, "Owner is required.");
		//
		return getGuardedString(getOwnerId(owner), owner.getClass(), key);
	}
	
	@Override
	@Transactional
	public void saveGuardedString(UUID ownerId, Class<? extends Identifiable> ownerType, String key, GuardedString value) {
		save(ownerId, ownerType, key, value == null ? null : value.asString());
	}
	
	@Override
	@Transactional
	public void saveGuardedString(Identifiable owner, String key, GuardedString value) {
		Assert.notNull(owner, "Owner is required.");
		//
		saveGuardedString(getOwnerId(owner), owner.getClass(), key, value);
	}
	
	@Override
	public String getOwnerType(Identifiable owner) {
		Assert.notNull(owner, "Owner is required.");
		//
		return getOwnerType(owner.getClass());
	}
	
	@Override
	public String getOwnerType(Class<? extends Identifiable> ownerType) {
		Assert.notNull(ownerType, "Owner type is required.");
		//
		Class<? extends BaseEntity> ownerEntityType = getLookupService().getEntityClass(ownerType);
		if (ownerEntityType == null) {
			throw new IllegalArgumentException(String.format("Owner type [%s] has to generatize [AbstractEntity]", ownerType));
		}
		return ownerEntityType.getCanonicalName();
	}
	
	@Override
	@Transactional
	public void renewVector(IdmConfidentialStorageValueDto value) {
		Assert.notNull(value, "Value is required.");
		//
		IdmConfidentialStorageValue storageValue = getStorageValue(value.getOwnerId(), value.getOwnerType(), value.getKey());
		Assert.notNull(storageValue, String.format("Value was not found in the confidential storage. Owner id [%s], owner type [%s], key [%s].",
				value.getOwnerId(), value.getOwnerType(), value.getKey()));
		//
		byte[] decryptedValue = cryptService.decrypt(storageValue.getValue(), storageValue.getIv());
		// Renew the vector
		byte[] newInitVector = cryptService.generateVector();
		storageValue.setIv(newInitVector);
		// Save the confidential value with the new vector
		storageValue.setValue(cryptService.encrypt(decryptedValue, newInitVector));
		repository.save(storageValue);
	}
	
	/**
	 * Get persisted storage by owner and key
	 * 
	 * @param owner
	 * @param key
	 * @return
	 */
	private IdmConfidentialStorageValue getStorageValue(UUID ownerId, Class<? extends Identifiable> ownerType, String key) {
		return getStorageValue(ownerId, getOwnerType(ownerType), key);
	}
	
	private IdmConfidentialStorageValue getStorageValue(UUID ownerId, String ownerType, String key) {
		//
		LOG.debug("Get value for owner [{},{}] and key [{}] from confidential storage", ownerId, ownerType, key);
		//
		Assert.notNull(ownerId, "Owner identifier is required");
		Assert.notNull(ownerType, "Owner type is required.");
		Assert.notNull(key, "Key is required.");
		//
		return repository.findOneByOwnerIdAndOwnerTypeAndKey(ownerId, ownerType, key);
	}

	/**
	 * Converts storage byte value to Serializable
	 * 
	 * @param value
	 * @param iv
	 * @return
	 */
	private Serializable fromStorageValue(byte[] value, byte[] iv) {
		if (value == null) {
			return null;
		}
		byte [] decryptValue = cryptService.decrypt(value, iv);
	    return SerializationUtils.deserialize(decryptValue);
	}

	/**
	 * Converts serializable to storage byte value.
	 * 
	 * @param value
	 * @param iv
	 * @return
	 */
	private byte[] toStorageValue(Serializable value, byte[] iv) {
		byte [] serializedValue = SerializationUtils.serialize(value);
		//
		return cryptService.encrypt(serializedValue, iv);
	}
	
	/**
	 * UUID identifier from given owner.
	 * 
	 * @param owner
	 * @return
	 */
	private UUID getOwnerId(Identifiable owner) {
		Assert.notNull(owner, "Owner is required.");
		if (owner.getId() == null) {
			return null;
		}		
		Assert.isInstanceOf(UUID.class, owner.getId(), "Entity with UUID identifier is supported as owner for confidential storage.");
		//
		return (UUID) owner.getId();
	}
	
	/**
	 * Abstract form value - confidential storage - circular dependency
	 * 
	 * @return
	 */
	private LookupService getLookupService() {
		if (lookupService == null) {
			lookupService = context.getBean(LookupService.class);
		}
		return lookupService;
	}
}
