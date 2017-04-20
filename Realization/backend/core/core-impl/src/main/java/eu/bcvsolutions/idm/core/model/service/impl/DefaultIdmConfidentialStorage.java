package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.Serializable;
import java.text.MessageFormat;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
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
	public <O extends AbstractEntity> void save(O owner, String key, Serializable values) {
		Assert.notNull(owner);
		Assert.notNull(owner.getId());
		Assert.hasLength(key);
		//
		LOG.debug("Saving value for owner [{}] and key [{}] to confidential storage", owner, key);
		IdmConfidentialStorageValue storage = getStorageValue(owner, key);
		if (storage == null) {
			// create new storage
			storage = new IdmConfidentialStorageValue();
			storage.setOwnerType(getOwnerType(owner));
			storage.setOwnerId(owner.getId());
			storage.setKey(key);
		}
		// set storage value
		storage.setValue(toStorageValue(values));
		// persist
		repository.save(storage);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public <O extends AbstractEntity> void delete(O owner, String key) {
		Assert.notNull(owner);
		Assert.notNull(owner.getId());
		Assert.hasLength(key);
		//
		LOG.debug("Delete value for owner [{}] and key [{}] from confidential storage", owner, key);
		IdmConfidentialStorageValue storageValue = getStorageValue(owner, key);
		if (storageValue != null) {
			repository.delete(storageValue);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public <O extends AbstractEntity> Serializable get(O owner, String key) {
		Assert.notNull(owner);
		Assert.notNull(owner.getId());
		Assert.hasLength(key);
		//
		IdmConfidentialStorageValue storageValue = getStorageValue(owner, key);
		return storageValue == null ? null : fromStorageValue(storageValue.getValue());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public <O extends AbstractEntity, T extends Serializable> T get(O owner, String key, Class<T> valueType) {
		Serializable storageValue = get(owner, key);
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
	public <O extends AbstractEntity, T extends Serializable> T get(O owner, String key, Class<T> valueType, T defaultValue) {
		try {
			T value = get(owner, key, valueType);
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
	public <O extends AbstractEntity> GuardedString getGuardedString(O owner, String key) {
		Serializable storageValue = get(owner, key);
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
	public <O extends AbstractEntity> void saveGuardedString(O owner, String key, GuardedString value) {
		save(owner, key, value == null ? null : value.asString());
	}
	
	
	/**
	 * Get persisted storage by owner and key
	 * 
	 * @param owner
	 * @param key
	 * @return
	 */
	private IdmConfidentialStorageValue getStorageValue(AbstractEntity owner, String key) {
		LOG.debug("Get value for owner [{}] and key [{}] from confidential storage", owner, key);
		return repository.findOneByOwnerTypeAndOwnerIdAndKey(getOwnerType(owner), owner.getId(), key);
	}
	
	/**
	 * Returns owner type
	 * @param owner
	 * @return
	 */
	private String getOwnerType(BaseEntity owner) {
		return owner.getClass().getCanonicalName();
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
