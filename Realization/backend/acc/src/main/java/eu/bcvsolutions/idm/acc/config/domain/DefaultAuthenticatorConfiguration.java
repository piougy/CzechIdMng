package eu.bcvsolutions.idm.acc.config.domain;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.LookupService;

/**
 * Implementation of configuration for authenticator
 *
 * @author Ondrej Kopr
 *
 */
@Component("authenticatorConfiguration")
public class DefaultAuthenticatorConfiguration extends AbstractConfiguration implements AuthenticatorConfiguration {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(DefaultAuthenticatorConfiguration.class);

	@Autowired
	protected LookupService lookupService;

	@Override
	public String getConfigurableType() {
		return "security.auth";
	}

	@Override
	public List<SysSystemDto> getSystems() {
		List<SysSystemDto> result = Lists.newArrayList();

		// Maximum count for system is configurable and it will start from 1 = defined maximum + 1
		for (int index = 1; index < getMaximumSystemCount() + 1; index++) {
			StringBuilder property = new StringBuilder();
			property.append(PROPERTY_AUTH_PREFIX);
			property.append(index);
			property.append(ConfigurationService.PROPERTY_SEPARATOR);
			property.append(AUTH_SYSTEM_SEPARATOR);
			
			String key = property.toString();
			LOG.debug("Try find configuration property with key [{}]", key);
			String systemCodeable = getConfigurationService().getValue(key, null);
			
			if (StringUtils.isBlank(systemCodeable)) {
				continue;
			}
			
			SysSystemDto systemDto = getSystemByCodeable(systemCodeable);
			// Prevent add null values
			if (systemDto != null) {
				result.add(systemDto);
			}
		}

		return result;
	}

	@Override
	@SuppressWarnings("deprecation")
	public SysSystemDto getSystem() {
		return getSystemByCodeable(this.getConfigurationService().getValue(PROPERTY_AUTH_SYSTEM_ID));
	}

	@Override
	public long getMaximumSystemCount() {
		return this.getConfigurationService().getLongValue(PROPERTY_AUTH_MAX_SYSTEM_COUNT, DEFAULT_AUTH_MAX_SYSTEM_COUNT);

	}

	/**
	 * Return system by given code/id ({@link LookupService} is used).
	 * If system isn't founded, return null and log warn message.
	 *
	 * @param systemCodeable
	 * @return
	 */
	private SysSystemDto getSystemByCodeable(String systemCodeable) {
		if (systemCodeable == null) {
			return null;
		}

		SysSystemDto system = (SysSystemDto) lookupService.lookupDto(SysSystemDto.class, systemCodeable);
		
		if (system == null) {
			LOG.warn("System by codeable identifier [{}] not found. Check configuration property/properties", systemCodeable);
		}

		return system;
	}

}
