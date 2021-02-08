package eu.bcvsolutions.idm.acc.config.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.annotations.Beta;

import eu.bcvsolutions.idm.acc.dto.SysConnectorServerDto;
import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Configuration for connector servers.
 * 
 * @author Radek Tomiška
 * @since 10.8.0
 * @Beta - unused, prepared only
 */
@Beta
public interface ConnectorServerConfiguration extends Configurable {

	/**
	 * Default remote connector server (uuid identifier).
	 */
	String PROPERTY_DEFAULT_REMOTE_SERVER = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "acc.connectorServer.defaultRemoteServer";

	@Override
	default String getConfigurableType() {
		return "connectorServer";
	}
	
	@Override
	default boolean isDisableable() {
		return false;
	}

	@Override
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>();
		properties.add(PROPERTY_DEFAULT_REMOTE_SERVER);
		return properties;
	}
	
	/**
	 * Default remote server identifier.
	 * 
	 * @return default remote server or null
	 */
	UUID getDefaultRemoteServerId();
	
	/**
	 * Default remote server.
	 * 
	 * @return default remote server or null
	 */
	SysConnectorServerDto getDefaultRemoteServer();
}
