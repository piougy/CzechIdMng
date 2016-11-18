package eu.bcvsolutions.idm.icf.virtual.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorInfo;
import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;
import eu.bcvsolutions.idm.icf.api.IcfSchema;
import eu.bcvsolutions.idm.icf.exception.IcfException;
import eu.bcvsolutions.idm.icf.impl.IcfConfigurationPropertiesImpl;
import eu.bcvsolutions.idm.icf.impl.IcfConfigurationPropertyImpl;
import eu.bcvsolutions.idm.icf.impl.IcfConnectorConfigurationImpl;
import eu.bcvsolutions.idm.icf.impl.IcfConnectorInfoImpl;
import eu.bcvsolutions.idm.icf.impl.IcfConnectorKeyImpl;
import eu.bcvsolutions.idm.icf.service.api.IcfConfigurationFacade;
import eu.bcvsolutions.idm.icf.service.api.IcfConfigurationService;

/**
 * Connector framework for virtual account implementation
 * @author svandav
 *
 */
@Service
public class VirtualIcfConfigurationService implements IcfConfigurationService {

	@Autowired
	public VirtualIcfConfigurationService(IcfConfigurationFacade icfConfigurationAggregator) {
		if (icfConfigurationAggregator.getIcfConfigs() == null) {
			throw new IcfException("Map of ICF implementations is not defined!");
		}
		if (icfConfigurationAggregator.getIcfConfigs().containsKey(this.getIcfType())) {
			throw new IcfException("ICF implementation duplicity for key: " + this.getIcfType());
		}
		icfConfigurationAggregator.getIcfConfigs().put(this.getIcfType(), this);
	}

	/**
	 * Return key defined ICF implementation
	 * 
	 * @return
	 */
	@Override
	public String getIcfType() {
		return "virtual";
	}

	/**
	 * Return available local connectors for this ICF implementation
	 * 
	 * @return
	 */
	@Override
	public List<IcfConnectorInfo> getAvailableLocalConnectors() {
		List<IcfConnectorInfo> localConnectorInfos = new ArrayList<>();
		IcfConnectorInfoImpl dto = new IcfConnectorInfoImpl("Testovací konektor", "categori test", new IcfConnectorKeyImpl(getIcfType(), "eu.bcvsolutions.connectors.test", "0.0.1", "Test connector"));
		localConnectorInfos.add(dto);
		return localConnectorInfos;
	}

	/**
	 * Return find connector default configuration by connector key
	 * 
	 * @param key
	 * @return
	 */
	@Override
	public IcfConnectorConfiguration getConnectorConfiguration(IcfConnectorKey key) {
		Assert.notNull(key);
		IcfConnectorConfigurationImpl dto = new IcfConnectorConfigurationImpl();
		IcfConfigurationPropertiesImpl propertiesDto = new IcfConfigurationPropertiesImpl();
		IcfConfigurationPropertyImpl propertyDto = new IcfConfigurationPropertyImpl();
		propertyDto.setConfidential(true);
		propertyDto.setDisplayName("First property");
		propertyDto.setGroup("test");
		propertyDto.setName("first_property");
		propertyDto.setRequired(true);
		propertyDto.setType(String.class.getName());
		propertyDto.setValue("test value");
		propertiesDto.getProperties().add(propertyDto);
		dto.setConfigurationProperties(propertiesDto);
		return dto;

	}

	@Override
	public IcfSchema getSchema(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration) {
		return null;
	}

}
