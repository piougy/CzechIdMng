package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.ConfigurationDto;
import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration;
import eu.bcvsolutions.idm.core.model.repository.IdmConfigurationRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Default implementation finds configuration in database, if configuration for
 * given key is not found, then configuration in property file will be returned. 
 * Public (not secured) configuration could be read without authentication. 
 * Confidential properties are saved to confidential storage.
 * 
 * @author Radek Tomiška 
 *
 */
@Service
public class DefaultConfigurationService extends AbstractReadWriteEntityService<IdmConfiguration, QuickFilter> implements IdmConfigurationService, ConfigurationService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultConfigurationService.class);

	private final IdmConfigurationRepository repository;
	private final ConfidentialStorage confidentialStorage;
	private final ConfigurableEnvironment env; // TODO: optional
	
	@Autowired
	public DefaultConfigurationService(
			IdmConfigurationRepository repository,
			ConfidentialStorage confidentialStorage,
			ConfigurableEnvironment env) {
		super(repository);
		//
		this.repository = repository;
		this.confidentialStorage = confidentialStorage;
		this.env = env; // TODO: optional
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmConfiguration getByName(String name) {
		return repository.findOneByName(name);
	}

	@Override
	@Transactional(readOnly = true)
	public String getValue(String key) {
		return getValue(key, null);
	}
	
	@Override
	@Transactional
	public void setValue(String key, String value) {
		Assert.hasText(key);
		//
		saveConfiguration(new ConfigurationDto(key, value));
	}
	
	@Override
	@Transactional
	public String deleteValue(String key) {
		IdmConfiguration entity = repository.get(key);
		if (entity == null) {
			return null;
		}
		delete(entity);
		return entity.getValue();
	}
	
	@Override
	@Transactional
	public void saveConfiguration(ConfigurationDto configuration) {
		Assert.notNull(configuration);
		Assert.hasText(configuration.getName());
		// only maps dto to entity
		IdmConfiguration configurationEntity = getByName(configuration.getName());
		if (configurationEntity == null) {
			configurationEntity = new IdmConfiguration(configuration.getName(), configuration.getValue(), configuration.isSecured(), configuration.isConfidential());
		} else {
			configurationEntity.setValue(configuration.getValue());
			configurationEntity.setSecured(configuration.isSecured());
			configurationEntity.setConfidential(configuration.isConfidential());
		}
		save(configurationEntity);
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
			String previousValue = entity.getId() == null ? null : confidentialStorage.get(entity, CONFIDENTIAL_PROPERTY_VALUE, String.class);
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
			confidentialStorage.save(entity, CONFIDENTIAL_PROPERTY_VALUE, value);
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
	
	@Override
	@Transactional(readOnly = true)
	public String getValue(String key, String defaultValue) {
		LOG.debug("Reading configuration for key [{}] and default[{}]", key, defaultValue);
		String value = null;
		// idm configuration has higher priority than property file
		IdmConfiguration config = repository.get(key);
		if (config != null) {
			if (config.isConfidential()) {
				value = confidentialStorage.get(config, CONFIDENTIAL_PROPERTY_VALUE, String.class);			
				LOG.debug("Configuration value for key [{}] was found in confidential storage", config.getName());
			} else {			
				value = config.getValue();
				LOG.debug("Configuration value [{}] for key [{}] was found in database.", key, value);
			}			
		} else {
			// try to find value in property configuration
			value = env.getProperty(key);
		}
		// fill default value
		if (value == null) {
			value = defaultValue;
		}	
		LOG.debug("Resolved configuration value for key [{}] and default [{}] is [{}].", key, defaultValue, value);
		return value;
	}
	
	@Override
	@Transactional(readOnly = true)
	public GuardedString getGuardedValue(String key) {
		return getGuardedValue(key, null);
	}
	
	@Override
	@Transactional(readOnly = true)
	public GuardedString getGuardedValue(String key, String defaultValue) {
		String value = getValue(key, defaultValue);
		return value == null ? null : new GuardedString(value);
	}

	@Override
	@Transactional(readOnly = true)
	public Boolean getBooleanValue(String key) {
		String value = getValue(key);
		return value == null ? null : Boolean.valueOf(value);
	}
	
	@Override
	@Transactional(readOnly = true)
	public boolean getBooleanValue(String key, boolean defaultValue) {
		String value = getValue(key);
		return value == null ? defaultValue : Boolean.parseBoolean(value);
	}
	
	@Override
	@Transactional
	public void setBooleanValue(String key, boolean value) {
		setValue(key, Boolean.valueOf(value).toString());
	}
	
	@Override
	@Transactional(readOnly = true)
	public Integer getIntegerValue(String key) {
		String value = getValue(key);
		return value == null ? null : Integer.valueOf(value);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Integer getIntegerValue(String key, Integer defaultValue) {
		String value = getValue(key);
		try {
			return value == null ? defaultValue : Integer.valueOf(value);
		} catch (NumberFormatException ex) {
			LOG.warn("Property [{}] for key [{}] is not integer, returning default value [{}]", value, key, defaultValue, ex);
			return defaultValue; 
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Long getLongValue(String key) {
		String value = getValue(key);
		return value == null ? null : Long.valueOf(value);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Long getLongValue(String key, Long defaultValue) {
		String value = getValue(key);
		try {
			return value == null ? defaultValue : Long.valueOf(value);
		} catch (NumberFormatException ex) {
			LOG.warn("Property [{}] for key [{}] is not integer, returning default value [{}]", value, key, defaultValue, ex);
			return defaultValue; 
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<IdmConfiguration> findByPrefix(String keyPrefix, Pageable pageable) {
		return repository.findByNameStartingWith(keyPrefix, pageable);
	}

	/**
	 * Returns all public configuration properties
	 * 
	 * @return
	 */
	@Override
	@Transactional(readOnly = true)
	public List<ConfigurationDto> getAllPublicConfigurations() {
		Map<String, Object> configurations = new HashMap<>();
		// defaults from property file
		Map<String, Object> map = getAllProperties(env);
		for (Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			if (key.startsWith(IDM_PUBLIC_PROPERTY_PREFIX)) {
				configurations.put(key, entry.getValue());
			}
		}
		// override from database
		repository.findAllBySecuredIsFalse().forEach(idmConfiguration -> {
			configurations.put(idmConfiguration.getName(), idmConfiguration.getValue());
		});
		List<ConfigurationDto> results = new ArrayList<>();
		configurations.forEach((k, v) -> {
			results.add(toConfigurationDto(k, v));
		});
		return results;
	}
	
	/**
	 * Returns all public configuration properties
	 * 
	 * @return
	 */
	@Override
	public List<ConfigurationDto> getAllConfigurationsFromFiles() {
		Map<String, Object> map = getAllProperties(env);
		return map.entrySet().stream()
				.filter(entry -> {
					return entry.getKey().toLowerCase().startsWith(IDM_PROPERTY_PREFIX);
				})
				.map(entry -> {
					return toConfigurationDto(entry.getKey(), entry.getValue());
				})
				.collect(Collectors.toList());
	}
	
	/**
	 * Returns server environment properties
	 * 
	 * @return
	 */
	@Override
	public List<ConfigurationDto> getAllConfigurationsFromEnvironment() {
		Map<String, Object> map = getAllProperties(env);
		return map.entrySet().stream()
				.filter(entry -> {
					return !entry.getKey().toLowerCase().startsWith(IDM_PROPERTY_PREFIX);
				})
				.map(entry -> {
					return toConfigurationDto(entry.getKey(), entry.getValue());
				})
				.collect(Collectors.toList());
	}
	
	@Override
	@Transactional(readOnly = true)
	public String getInstanceId() {
		return getValue(ConfigurationService.PROPERTY_APP_INSTANCE_ID, ConfigurationService.DEFAULT_PROPERTY_APP_INSTANCE_ID);
	}
	
	private static ConfigurationDto toConfigurationDto(String key, Object value) {
		String stringValue = value == null ? null : value.toString();
		ConfigurationDto configuration = new ConfigurationDto(key, stringValue);
		// password etc. has to be guarded - can be used just in BE
		if (shouldBeConfidential(configuration.getName())) {
			LOG.debug("Configuration value for property [{}] is guarded.", configuration.getName());
			configuration.setValue(GuardedString.SECRED_PROXY_STRING);
			configuration.setConfidential(true);
		}
		if (shouldBeSecured(configuration.getName())) {
			configuration.setSecured(true);
		}
		return configuration;
	}

	private static Map<String, Object> getAllProperties(ConfigurableEnvironment aEnv) {
		Map<String, Object> result = new HashMap<>();
		aEnv.getPropertySources().forEach(ps -> addAll(result, getAllProperties(ps)));
		return result;
	}

	private static Map<String, Object> getAllProperties(PropertySource<?> aPropSource) {
		Map<String, Object> result = new HashMap<>();

		if (aPropSource instanceof CompositePropertySource) {
			CompositePropertySource cps = (CompositePropertySource) aPropSource;
			cps.getPropertySources().forEach(ps -> addAll(result, getAllProperties(ps)));
			return result;
		}
		
		if (aPropSource instanceof EnumerablePropertySource<?>) {
			EnumerablePropertySource<?> ps = (EnumerablePropertySource<?>) aPropSource;
			Arrays.asList(ps.getPropertyNames()).forEach(key -> result.put(key, ps.getProperty(key)));
			return result;
		}
		return result;

	}

	private static void addAll(Map<String, Object> aBase, Map<String, Object> aToBeAdded) {
		for (Entry<String, Object> entry : aToBeAdded.entrySet()) {
			if (aBase.containsKey(entry.getKey())) {
				continue;
			}
			aBase.put(entry.getKey(), entry.getValue());
		}
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
