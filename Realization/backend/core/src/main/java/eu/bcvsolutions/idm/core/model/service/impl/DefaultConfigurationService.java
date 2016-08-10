package eu.bcvsolutions.idm.core.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration;
import eu.bcvsolutions.idm.core.model.repository.IdmConfigurationRepository;
import eu.bcvsolutions.idm.core.model.service.ConfigurationService;

/**
 * Default implementation finds configuration in database, if configuration for
 * given key is not found, then configuration in property file will be returned
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@Service
public class DefaultConfigurationService implements ConfigurationService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultConfigurationService.class);

	@Autowired
	private Environment env;

	@Autowired
	private IdmConfigurationRepository configurationRepository;

	@Override
	public String getValue(String key) {
		log.debug("Reading configuration for key [{}]", key);
		String value = null;
		IdmConfiguration config = configurationRepository.findOneByName(key);
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
}
