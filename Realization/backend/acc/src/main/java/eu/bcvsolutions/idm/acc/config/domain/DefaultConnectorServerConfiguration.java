package eu.bcvsolutions.idm.acc.config.domain;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.annotations.Beta;

import eu.bcvsolutions.idm.acc.dto.SysConnectorServerDto;
import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;

/**
 * Configuration for connector servers.
 * 
 * @author Radek Tomiška
 * @since 10.8.0
 * @Beta - unused, prepared only
 */
@Beta
@Component("connectorServerConfiguration")
public class DefaultConnectorServerConfiguration extends AbstractConfiguration implements ConnectorServerConfiguration {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultConnectorServerConfiguration.class);
	//
	@Autowired private LookupService lookupService;

	@Override
	public UUID getDefaultRemoteServerId() {
		String remoteServerId = getConfigurationService().getValue(PROPERTY_DEFAULT_REMOTE_SERVER);
		if (StringUtils.isBlank(remoteServerId)) {
			LOG.debug("Default remote server is not configured, returning null. Change configuration [{}]", PROPERTY_DEFAULT_REMOTE_SERVER);
			return null;
		}
		//
		try {
			return DtoUtils.toUuid(remoteServerId);
		} catch (ClassCastException ex) {
			LOG.debug("Default remote server configuration value [{}] is not valid, returning null. Change configuration [{}]",
					remoteServerId, PROPERTY_DEFAULT_REMOTE_SERVER);
			return null;
		}
	}
	
	@Override
	public SysConnectorServerDto getDefaultRemoteServer() {
		UUID remoteServerId = getDefaultRemoteServerId();
		if (remoteServerId == null) {
			return null;
		}
		SysConnectorServerDto remoteServer = lookupService.lookupDto(SysConnectorServerDto.class, remoteServerId);
		if (remoteServer == null) {
			LOG.warn("Default remote server with identifier [{}] not found, returning null. Change configuration [{}]", remoteServerId, PROPERTY_DEFAULT_REMOTE_SERVER);
			return null;
		}
		return remoteServer;
	}

}
