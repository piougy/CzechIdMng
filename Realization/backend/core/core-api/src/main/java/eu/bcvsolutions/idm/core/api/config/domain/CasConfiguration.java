package eu.bcvsolutions.idm.core.api.config.domain;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Cas configuration - interface
 *
 * @author Roman Kučera
 */
public interface CasConfiguration extends Configurable, ScriptEnabled {

	String PROPERTY_CAS_SSO_ENABLED = ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX +
			"core.cas.sso.enabled";
	boolean DEFAULT_PROPERTY_CAS_SSO_ENABLED = false;

	String PROPERTY_CAS_URL = ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX +
			"core.cas.url";

	String PROPERTY_IDM_URL = ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX +
			"core.cas.idm-url";

	String PROPERTY_CAS_LOGIN_SUFFIX = ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX +
			"core.cas.login-suffix";

	String PROPERTY_CAS_LOGOUT_SUFFIX = ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX +
			"core.cas.logout-suffix";

	String PROPERTY_HEADER_NAME = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX +
			"core.cas.header-name";

	String PROPERTY_HEADER_PREFIX = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX +
			"core.cas.header-prefix";

	@Override
	default String getConfigurableType() {
		return "configuration";
	}

	@Override
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>(); // we are not using superclass properties - enable and order does not make a sense here
		return properties;
	}

	boolean getPropertyCasSsoEnabled();

	String getPropertyCasUrl();

	String getPropertyIdmUrl();

	String getPropertyCasLoginSuffix();

	String getPropertyCasLogoutSuffix();

	String getPropertyHeaderName();

	String getPropertyHeaderPrefix();
}
