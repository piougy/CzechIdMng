package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

@Service
public class DefaultSysSynchronization {

	private final IcConnectorFacade connectorFacade;
	
	@Autowired
	public DefaultSysSynchronization(IcConnectorFacade connectorFacade) {
		Assert.notNull(connectorFacade);
		this.connectorFacade = connectorFacade;
	}


	public void synchronization(SysSystem system){
		
	}
}
