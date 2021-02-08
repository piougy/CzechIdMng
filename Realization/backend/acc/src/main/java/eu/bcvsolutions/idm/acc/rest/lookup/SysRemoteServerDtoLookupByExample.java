package eu.bcvsolutions.idm.acc.rest.lookup;

import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.config.domain.ConnectorServerConfiguration;
import eu.bcvsolutions.idm.acc.dto.SysConnectorServerDto;
import eu.bcvsolutions.idm.acc.service.api.SysRemoteServerService;
import eu.bcvsolutions.idm.core.api.rest.lookup.AbstractDtoLookupByExample;

/**
 * Remote server lookup by example.
 * 
 * @author Radek Tomiška
 * @since 10.8.0
 */
@Component
public class SysRemoteServerDtoLookupByExample extends AbstractDtoLookupByExample<SysConnectorServerDto>{

	@Autowired @Lazy private ConnectorServerConfiguration connectorServerConfiguration;
	@Autowired @Lazy private SysRemoteServerService remoteServerService;

	@Override
	public SysConnectorServerDto lookup(SysConnectorServerDto example) {
		List<SysConnectorServerDto> remoteServers = remoteServerService.find(null).getContent();
		//
		// try to find remote system by all fields - first win
		return remoteServers
				.stream()
				.filter(r -> {
					return StringUtils.equals(r.getHost(), example.getHost())
							&& Integer.compare(r.getPort(), example.getPort()) == 0
							&& BooleanUtils.compare(r.isUseSsl(), example.isUseSsl()) == 0
							&& Integer.compare(r.getTimeout(), example.getTimeout()) == 0;
				})
				.findFirst()
				.orElse(null);
	}
}
