package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.model.dto.ConfigurationDto;
import eu.bcvsolutions.idm.core.model.dto.QuickFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.BaseRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmConfigurationRepository;
import eu.bcvsolutions.idm.core.model.service.IdmConfigurationService;
import eu.bcvsolutions.idm.security.domain.GuardedString;

/**
 * Default implementation finds configuration in database, if configuration for
 * given key is not found, then configuration in property file will be returned
 * 
 * TODO: cache
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@Service
public class DefaultIdmConfigurationService extends AbstractReadWriteEntityService<IdmConfiguration, QuickFilter> implements IdmConfigurationService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultIdmConfigurationService.class);

	@Autowired
	private ConfigurableEnvironment env;

	@Autowired
	private IdmConfigurationRepository configurationRepository;
	
	@Override
	protected BaseRepository<IdmConfiguration> getRepository() {
		return configurationRepository;
	}
	
	@Override
	public Page<IdmConfiguration> find(QuickFilter filter, Pageable pageable) {
		if (filter == null) {
			return find(pageable);
		}
		return configurationRepository.findQuick(filter.getText(), pageable);
	}

	@Override
	public String getValue(String key) {
		return getValue(key, null);
	}
	
	@Override
	public void setValue(String key, String value) {
		Assert.hasText(key);
		//
		IdmConfiguration configuration = configurationRepository.get(key);
		if (configuration == null) {
			configuration = new IdmConfiguration(key, value);
		} else {
			configuration.setValue(value);
		}		
		setConfiguration(configuration);
	}
	
	@Override
	public void setConfiguration(IdmConfiguration configuration) {
		Assert.notNull(configuration);
		Assert.hasText(configuration.getName());
		//
		if (shouldBeSecured(configuration.getName())) {
			configuration.setSecured(true);
		}
		configurationRepository.save(configuration);
	}
	
	@Override
	public String getValue(String key, String defaultValue) {
		log.debug("Reading configuration for key [{}] and default[{}]", key, defaultValue);
		String value = null;
		// idm configuration has higher priority
		IdmConfiguration config = configurationRepository.get(key);
		if (config != null) {
			value = config.getValue();
			log.debug("Configuration value [{}] for key [{}] were found in database.", key, value);
		} else {
			// try to find value in property configuration
			value = env.getProperty(key);
		}
		// fill default value
		if (value == null) {
			value = defaultValue;
		}	
		log.debug("Resolved configuration value for key [{}] and default [{}] is [{}].", key, defaultValue, value);
		return value;
	}

	@Override
	public Boolean getBooleanValue(String key) {
		String value = getValue(key);
		return value == null ? null : Boolean.valueOf(value);
	}
	
	@Override
	public boolean getBooleanValue(String key, boolean defaultValue) {
		String value = getValue(key);
		return value == null ? defaultValue : Boolean.valueOf(value);
	}
	
	@Override
	public void setBooleanValue(String key, boolean value) {
		setValue(key, Boolean.valueOf(value).toString());
	}
	
	@Override
	public Integer getIntegerValue(String key) {
		String value = getValue(key);
		return value == null ? null : Integer.valueOf(value);
	}
	
	@Override
	public Integer getIntegerValue(String key, Integer defaultValue) {
		String value = getValue(key);
		try {
			return value == null ? defaultValue : Integer.valueOf(value);
		} catch (NumberFormatException ex) {
			log.warn("Property [{}] for key [{}] is not integer, returning default value [{}]", value, key, defaultValue, ex);
			return defaultValue; 
		}
	}

	/**
	 * Returns all public configuration properties
	 * 
	 * @return
	 */
	@Override
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
		configurationRepository.findAllBySecuredIsFalse().forEach(idmConfiguration -> {
			if (idmConfiguration.getName().startsWith(IDM_PUBLIC_PROPERTY_PREFIX)) {
				configurations.put(idmConfiguration.getName(), idmConfiguration.getValue());
			}
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
	
	private static ConfigurationDto toConfigurationDto(String key, Object value) {
		String stringValue = value == null ? null : value.toString();
		// password etc. has to be guarded - can be used just in BE
		if(GuardedString.shouldBeGuarded(key)) {
			log.debug("Configuration value for property [{}] is guarded.", key);
			stringValue = GuardedString.SECRED_PROXY_STRING;
		}
		return new ConfigurationDto(key, stringValue, key.startsWith(IDM_PRIVATE_PROPERTY_PREFIX));
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
	
	private static boolean shouldBeSecured(String key) {
		return key.startsWith(IdmConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX);
	}
}
