package eu.bcvsolutions.idm.icf.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.icf.api.IcfAttribute;
import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;
import eu.bcvsolutions.idm.icf.api.IcfUidAttribute;
import eu.bcvsolutions.idm.icf.domain.IcfResultCode;
import eu.bcvsolutions.idm.icf.service.api.IcfConnectorService;

@Service
public class IcfConnectorAggregatorService {
	
	private Map<String, IcfConnectorService> icfConnectors = new HashMap<>();
	
	/**
	 * @return Connector services for all ICFs
	 */
	public Map<String, IcfConnectorService> getIcfConnectors() {
		return icfConnectors;
	}
	
	public IcfUidAttribute createObject(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration,
			List<IcfAttribute> attributes) {
		Assert.notNull(key);
		if(!icfConnectors.containsKey(key.getIcfType())){
			throw new ResultCodeException(IcfResultCode.ICF_IMPLEMENTATTION_NOT_FOUND,  ImmutableMap.of("icf", key.getIcfType()));
		}
		return icfConnectors.get(key.getIcfType()).createObject(key, connectorConfiguration, attributes);
		
	}
	
}
