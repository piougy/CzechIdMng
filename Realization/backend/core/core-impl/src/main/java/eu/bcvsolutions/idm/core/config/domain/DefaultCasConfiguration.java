package eu.bcvsolutions.idm.core.config.domain;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.CasConfiguration;

/**
 * Cas configuration - implementation
 *
 * @author Roman Kučera
 *
 */
@Component("casConfiguration")
public class DefaultCasConfiguration
		extends AbstractConfiguration
		implements CasConfiguration {

	@Override
	public boolean getPropertyCasSsoEnabled() {
		return getConfigurationService().getBooleanValue(CasConfiguration.PROPERTY_CAS_SSO_ENABLED, DEFAULT_PROPERTY_CAS_SSO_ENABLED);
	}

	@Override
	public String getPropertyCasUrl() {
		String value = getConfigurationService().getValue(CasConfiguration.PROPERTY_CAS_URL);
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return value;
	}

	@Override
	public String getPropertyIdmUrl() {
		String value = getConfigurationService().getValue(CasConfiguration.PROPERTY_IDM_URL);
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return value;
	}

	@Override
	public String getPropertyCasLoginSuffix() {
		String value = getConfigurationService().getValue(CasConfiguration.PROPERTY_CAS_LOGIN_SUFFIX);
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return value;
	}

	@Override
	public String getPropertyCasLogoutSuffix() {
		String value = getConfigurationService().getValue(CasConfiguration.PROPERTY_CAS_LOGOUT_SUFFIX);
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return value;
	}

	@Override
	public String getPropertyHeaderName() {
		String value = getConfigurationService().getValue(CasConfiguration.PROPERTY_HEADER_NAME);
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return value;
	}

	@Override
	public String getPropertyHeaderPrefix() {
		String value = getConfigurationService().getValue(CasConfiguration.PROPERTY_HEADER_PREFIX);
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return value;
	}
}
