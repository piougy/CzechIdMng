package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.Serializable;
import java.text.MessageFormat;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.model.entity.IdmConfidentialStorageValue;
import eu.bcvsolutions.idm.core.model.repository.IdmConfidentialStorageValueRepository;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

/**
 * "Naive" confidential storage. Values are persisted in standard database.
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmConfidentialStorage implements ConfidentialStorage {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmConfidentialStorage.class);
	private final IdmConfidentialStorageValueRepository repository;
	
	@Autowired
	public DefaultIdmConfidentialStorage(IdmConfidentialStorageValueRepository repository) {
		Assert.notNull(repository, "Confidential storage repository is required");
		//
		this.repository = repository;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public <O extends AbstractEntity> void save(O owner, String key, Serializable values) {
		Assert.notNull(owner);
		Assert.hasLength(key);
		//
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
		Assert.hasLength(key);
		//
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
	public <O extends AbstractEntity, T extends Serializable> T get(O owner, String key, Class<T> valueType) throws IllegalArgumentException {
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
	 * Get persisted storage by owner and key
	 * 
	 * @param owner
	 * @param key
	 * @return
	 */
	private IdmConfidentialStorageValue getStorageValue(AbstractEntity owner, String key) {
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
	    return SerializationUtils.deserialize(value);
	}

	/**
	 * Converts serializable to storage byte value
	 * 
	 * @param value
	 * @return
	 */
	private byte[] toStorageValue(Serializable value) {
	    return SerializationUtils.serialize(value);
	}
}
