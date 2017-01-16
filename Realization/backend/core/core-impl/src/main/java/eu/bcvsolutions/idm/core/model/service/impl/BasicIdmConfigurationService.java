package eu.bcvsolutions.idm.core.model.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration;
import eu.bcvsolutions.idm.core.model.repository.IdmConfigurationRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

/**
 * Basic configuration service
 * 
 * TODO: cache (except confidential properties)
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class BasicIdmConfigurationService extends AbstractReadWriteEntityService<IdmConfiguration, QuickFilter> implements IdmConfigurationService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultConfigurationService.class);

	private final IdmConfigurationRepository configurationRepository;
	private final ConfidentialStorage confidentialStorage;
	
	@Autowired
	public BasicIdmConfigurationService(
			IdmConfigurationRepository configurationRepository,
			ConfidentialStorage confidentialStorage, 
			ConfigurableEnvironment env) {
		super(configurationRepository);
		//
		this.configurationRepository = configurationRepository;
		this.confidentialStorage = confidentialStorage;
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmConfiguration getByName(String name) {
		return configurationRepository.findOneByName(name);
	}
	
	@Transactional(readOnly = true)
	// @Cacheable(cacheNames = "core-configurations", key="#key")
	public String getValue(String key) {
		LOG.debug("Reading configuration for key [{}]", key);
		String value = null;
		// idm configuration has higher priority than property file
		IdmConfiguration config = configurationRepository.get(key);
		if (config != null) {
			if (config.isConfidential()) {
				value = confidentialStorage.get(config, IdmConfigurationService.CONFIDENTIAL_PROPERTY_VALUE, String.class);			
				LOG.debug("Configuration value for key [{}] was found in confidential storage", config.getName());
			} else {			
				value = config.getValue();
				LOG.debug("Configuration value [{}] for key [{}] was found in database.", key, value);
			}			
		}	
		LOG.debug("Resolved configuration value for key [{}] is [{}].", key, value);
		return value;
	}
	
	@Override
	@Transactional
	public IdmConfiguration save(IdmConfiguration entity) {
		Assert.notNull(entity);
		// check confidential option
		if (shouldBeConfidential(entity.getName())) {
			entity.setConfidential(true);
		}
		// check secured option
		if (shouldBeSecured(entity.getName())) {
			entity.setSecured(true);
		}
		// save confidential properties to confidential storage
		String value = entity.getValue();
		if (entity.isConfidential()) {
			String previousValue = entity.getId() == null ? null : confidentialStorage.get(entity, IdmConfigurationService.CONFIDENTIAL_PROPERTY_VALUE, String.class);
			if (StringUtils.isNotEmpty(value) || (value == null && previousValue != null)) {
				// we need only to know, if value was filled
				entity.setValue(GuardedString.SECRED_PROXY_STRING);
			} else {
				entity.setValue(null);
			}
		}
		entity = super.save(entity);
		//
		// save new value to confidential storage - empty string should be given for saving empty value. We are leaving previous value otherwise
		if (entity.isConfidential() && value != null) {
			confidentialStorage.save(entity, IdmConfigurationService.CONFIDENTIAL_PROPERTY_VALUE, value);
			LOG.debug("Configuration value [{}] is persisted in confidential storage", entity.getName());
		}
		return entity;
	}
	
	@Override
	@Transactional
	public void delete(IdmConfiguration entity) {
		Assert.notNull(entity);
		//
		if (entity.isConfidential()) {
			confidentialStorage.delete(entity, CONFIDENTIAL_PROPERTY_VALUE);
			LOG.debug("Configuration value [{}] was removed from confidential storage", entity.getName());
		}
		super.delete(entity);
	}
	
	/**
	 * Returns true, if key should be confidential by naming convention
	 * 
	 * @param key
	 * @return
	 */
	private static boolean shouldBeConfidential(String key) {
		return GuardedString.shouldBeGuarded(key);
	}
	
	/**
	 * Returns true, if key should be secured by naming convention. Confidential properties are always secured.
	 * 
	 * @param key
	 * @return
	 */
	private static boolean shouldBeSecured(String key) {
		return key.startsWith(ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX) || shouldBeConfidential(key);
	}
}
