package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.entity.SysConnectorServer;
import eu.bcvsolutions.idm.acc.repository.SysConnectorServerRepository;
import eu.bcvsolutions.idm.acc.service.api.SysConnectorServerService;
import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Default service for remote server with connectors
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
public class DefaultSysConnectorServerService  extends AbstractReadWriteEntityService<SysConnectorServer, QuickFilter> implements SysConnectorServerService {
	
	private final SysConnectorServerRepository connectorServerRepository;
	
	@Autowired
	public DefaultSysConnectorServerService(
			AbstractEntityRepository<SysConnectorServer, QuickFilter> repository,
			SysConnectorServerRepository connectorServerRepository) {
		super(repository);
		//
		Assert.notNull(connectorServerRepository);
		//
		this.connectorServerRepository = connectorServerRepository;
	}

	@Override
	public SysConnectorServer getByName(String name) {
		return connectorServerRepository.findOneByName(name);
	}	
}
