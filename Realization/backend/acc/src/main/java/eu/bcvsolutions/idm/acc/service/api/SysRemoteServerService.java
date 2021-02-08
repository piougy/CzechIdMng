package eu.bcvsolutions.idm.acc.service.api;

import java.util.UUID;

import eu.bcvsolutions.idm.acc.dto.SysConnectorServerDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRemoteServerFilter;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Remote server with connectors.
 * 
 * @author Radek Tomiška
 * @since 10.8.0
 */
public interface SysRemoteServerService extends 
		EventableDtoService<SysConnectorServerDto, SysRemoteServerFilter>,
		AuthorizableService<SysConnectorServerDto> {

	/**
	 * Returns configured remote server password.
	 * 
	 * @param remoteServerId remote server identifier
	 * @return password in guarded string
	 */
	GuardedString getPassword(UUID remoteServerId);
	
}
