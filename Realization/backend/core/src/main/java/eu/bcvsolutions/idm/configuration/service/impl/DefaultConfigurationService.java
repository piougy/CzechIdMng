package eu.bcvsolutions.idm.configuration.service.impl;

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
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.configuration.dto.ConfigurationDto;
import eu.bcvsolutions.idm.configuration.entity.IdmConfiguration;
import eu.bcvsolutions.idm.configuration.repository.IdmConfigurationRepository;
import eu.bcvsolutions.idm.configuration.service.ConfigurationService;

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
public class DefaultConfigurationService implements ConfigurationService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultConfigurationService.class);

	@Autowired
	private ConfigurableEnvironment env;

	@Autowired
	private IdmConfigurationRepository configurationRepository;

	@Override
	public String getValue(String key) {
		log.debug("Reading configuration for key [{}]", key);
		String value = null;
		IdmConfiguration config = configurationRepository.get(key);
		if (config != null) {
			value = config.getValue();
			log.debug("Configuration value [{}] for key [{}] were found in database.", key, value);
		} else {
			// try to find value in property configuration
			value = env.getProperty(key);
		}
		log.debug("Resolved configuration value for key [{}] is [{}].", key, value);
		return value;
	}

	@Override
	public boolean getBoolean(String key) {
		return Boolean.valueOf(getValue(key));
	}

	/**
	 * Returns all public configuration properties
	 * 
	 * @return
	 */
	@Override
	public List<ConfigurationDto> getAllPublicConfigurations() {
		Map<String, String> configurations = new HashMap<>();
		// defaults from property file
		Map<String, Object> map = getAllProperties(env);
		for (Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			if (key.startsWith(PUBLIC_PROPERTY_PREFIX)) {
				configurations.put(key, entry.getValue() == null ? null : entry.getValue().toString());
			}
		}
		// override from database
		configurationRepository.findAllBySecuredIsFalse().forEach(idmConfiguration -> {
			configurations.put(idmConfiguration.getName(), idmConfiguration.getValue());
		});
		List<ConfigurationDto> results = new ArrayList<>();
		configurations.forEach((k, v) -> {
			results.add(new ConfigurationDto(k, v));
		});
		return results;
	}
	
	/**
	 * Returns all public configuration properties
	 * 
	 * @return
	 */
	public List<ConfigurationDto> getAllFileConfigurations() {
		Map<String, Object> map = getAllProperties(env);
		return map.entrySet().stream()
			.map(entry -> { 
				return new ConfigurationDto(entry.getKey(), entry.getValue().toString()); 
				})
			.collect(Collectors.toList());
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
}
