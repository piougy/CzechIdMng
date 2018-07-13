package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.UUID;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
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
	
	public DefaultIdmConfidentialStorage(IdmConfidentialStorageValueRepository repository,
			CryptService encryptService) {
		Assert.notNull(repository, "Confidential storage repository is required");
		Assert.notNull(encryptService);
		//
		this.repository = repository;
		this.cryptService = encryptService;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public void save(UUID ownerId, Class<? extends Identifiable> ownerType, String key, Serializable values) {
		Assert.notNull(ownerId);
		Assert.notNull(ownerType);
		Assert.hasLength(key);
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
		// set storage value
		storage.setValue(toStorageValue(values));
		// persist
		repository.save(storage);
	}

	@Override
	@Transactional
	public void changeCryptKey(UUID ownerId, Class<? extends Identifiable> ownerType, String key, GuardedString oldKey) {
		Assert.notNull(ownerId);
		Assert.notNull(ownerType);
		Assert.notNull(oldKey);
		Assert.hasLength(key);
		//
		IdmConfidentialStorageValue storage = getStorageValue(ownerId, ownerType, key);
		//
		// decrypt value with old key
		byte[] decryptedValue = cryptService.decryptWithKey(storage.getValue(), oldKey);
		//
		// and crypt valuewith new key
		storage.setValue(cryptService.encrypt(decryptedValue));
		// persist new value
		repository.save(storage);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public void delete(UUID ownerId, Class<? extends Identifiable> ownerType, String key) {
		Assert.notNull(ownerId);
		Assert.notNull(ownerType);
		Assert.hasLength(key);
		//
		LOG.debug("Delete value for owner [{},{}] and key [{}] from confidential storage", ownerId, ownerType, key);
		IdmConfidentialStorageValue storageValue = getStorageValue(ownerId, ownerType, key);
		if (storageValue != null) {
			repository.delete(storageValue);
		}
	}
	
	@Override
	@Transactional
	public void deleteAll(UUID ownerId, Class<? extends Identifiable> ownerType) {
		Assert.notNull(ownerId);
		Assert.notNull(ownerType);
		//
		LOG.debug("Delete all values for owner [{},{}] from confidential storage", ownerId, ownerType);
		this.repository.deleteByOwnerIdAndOwnerType(ownerId, getOwnerType(ownerType));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public Serializable get(UUID ownerId, Class<? extends Identifiable> ownerType, String key) {
		Assert.notNull(ownerId);
		Assert.notNull(ownerType);
		Assert.hasLength(key);
		//
		IdmConfidentialStorageValue storageValue = getStorageValue(ownerId, ownerType, key);
		return storageValue == null ? null : fromStorageValue(storageValue.getValue());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public <T extends Serializable> T get(UUID ownerId, Class<? extends Identifiable> ownerType, String key, Class<T> valueType, T defaultValue) {
		try {
			T value = get(ownerId, ownerType, key, valueType);
			return value != null ? value : defaultValue;
		} catch(IllegalArgumentException ex) {
			LOG.debug(MessageFormat.format("Storage value [{0}] could not be cast to type [{1}], returning default value", key, valueType), ex);
			return defaultValue;
		}
	} 
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public GuardedString getGuardedString(UUID ownerId, Class<? extends Identifiable> ownerType, String key) {
		Serializable storageValue = get(ownerId, ownerType, key);
		if (storageValue == null) {
			return new GuardedString();
		}
		return new GuardedString(storageValue.toString());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public void saveGuardedString(UUID ownerId, Class<? extends Identifiable> ownerType, String key, GuardedString value) {
		save(ownerId, ownerType, key, value == null ? null : value.asString());
	}
	
	
	/**
	 * Get persisted storage by owner and key
	 * 
	 * @param owner
	 * @param key
	 * @return
	 */
	private IdmConfidentialStorageValue getStorageValue(UUID ownerId, Class<? extends Identifiable> ownerType, String key) {
		LOG.debug("Get value for owner [{},{}] and key [{}] from confidential storage", ownerId, ownerType, key);
		return repository.findOneByOwnerIdAndOwnerTypeAndKey(ownerId, getOwnerType(ownerType), key);
	}
	
	/**
	 * Returns owner type
	 * 
	 * @param owner
	 * @return
	 */
	private String getOwnerType(Class<? extends Identifiable> ownerType) {
		return ownerType.getCanonicalName();
	}

	/**
	 * Converts storage byte value to Serializable
	 * 
	 * @param type
	 * @param value
	 * @return
	 */
	private Serializable fromStorageValue(byte[] value) {
		if (value == null) {
			return null;
		}
		byte [] decryptValue = cryptService.decrypt(value);
	    return SerializationUtils.deserialize(decryptValue);
	}

	/**
	 * Converts serializable to storage byte value
	 * 
	 * @param value
	 * @return
	 */
	private byte[] toStorageValue(Serializable value) {
		byte [] serializedValue = SerializationUtils.serialize(value);
		return cryptService.encrypt(serializedValue);
	}
}
