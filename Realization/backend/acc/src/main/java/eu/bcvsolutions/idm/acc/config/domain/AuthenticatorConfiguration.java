package eu.bcvsolutions.idm.acc.config.domain;

import java.util.List;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.security.authentication.impl.DefaultAccAuthenticator;
import eu.bcvsolutions.idm.acc.security.authentication.impl.DefaultAccMultipleSystemAuthenticator;
import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;

/**
 * Configuration for authenticator against multiple system
 *
 * @author Ondrej Kopr
 *
 */
@SuppressWarnings({ "deprecation", "unused" })
public interface AuthenticatorConfiguration extends Configurable  {

	/**
	 * Property for authentication <b>idm.sec.acc.security.auth.</b> for compose auth properties eq:<br />
	 * - idm.sec.acc.security.auth.order0.system=<br />
	 * - idm.sec.acc.security.auth.order1.system=<br />
	 * - idm.sec.acc.security.auth.order2.system=<br />
	 * - ...
	 */
	String PROPERTY_AUTH_PREFIX = IdmConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + AccModuleDescriptor.MODULE_ID + ".security.auth.order";
	String AUTH_SYSTEM_SEPARATOR = "system";

	String PROPERTY_AUTH_MAX_SYSTEM_COUNT = IdmConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + AccModuleDescriptor.MODULE_ID + ".security.auth.maximumSystemCount";
	long DEFAULT_AUTH_MAX_SYSTEM_COUNT = 50;
	/**
	 * Old configuration property for authentication against system. Please use property {@link #PROPERTY_AUTH_PREFIX}
	 * 
	 * @deprecated @since 10.4.0 use {@link #getSystems()}
	 */
	@Deprecated
	String PROPERTY_AUTH_SYSTEM_ID = IdmConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "security.auth" + ConfigurationService.PROPERTY_SEPARATOR + AUTH_SYSTEM_SEPARATOR;

	/**
	 * Return maximum system count for authentication against multiples systems.
	 *
	 * @return
	 */
	long getMaximumSystemCount();
	
	/**
	 * Return {@link SysSystemDto} that is available for authenticate. This property is used for example in {@link DefaultAccMultipleSystemAuthenticator}.
	 *
	 * @return
	 */
	List<SysSystemDto> getSystems();

	/**
	 * Return system that is used for authentication.
	 *
	 * @return
	 * @deprecated @since 10.4.0 use {@link #getSystems()}
	 */
	@Deprecated
	SysSystemDto getSystem();
}
