package eu.bcvsolutions.idm.ic.virtual.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcConnectorServer;
import eu.bcvsolutions.idm.ic.api.IcSchema;
import eu.bcvsolutions.idm.ic.exception.IcException;
import eu.bcvsolutions.idm.ic.impl.IcConfigurationPropertiesImpl;
import eu.bcvsolutions.idm.ic.impl.IcConfigurationPropertyImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorConfigurationImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorInfoImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorKeyImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationFacade;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationService;

/**
 * Connector framework for virtual account implementation
 * @author svandav
 *
 */
@Service
public class VirtualIcConfigurationService implements IcConfigurationService {

	@Autowired
	public VirtualIcConfigurationService(IcConfigurationFacade icConfigurationAggregator) {
		if (icConfigurationAggregator.getIcConfigs() == null) {
			throw new IcException("Map of IC implementations is not defined!");
		}
		if (icConfigurationAggregator.getIcConfigs().containsKey(IMPLEMENTATION_TYPE)) {
			throw new IcException(MessageFormat.format("IC implementation duplicity for key: {0}", IMPLEMENTATION_TYPE));
		}
	    // Disable for now
		// icConfigurationAggregator.getIcConfigs().put(this.getIcType(), this);
	}
	
	final private static String IMPLEMENTATION_TYPE = "virtual";

	/**
	 * Return key defined IC implementation
	 * 
	 * @return
	 */
	@Override
	public String getImplementationType() {
		return IMPLEMENTATION_TYPE;
	}

	/**
	 * Return available local connectors for this IC implementation
	 * 
	 * @return
	 */
	@Override
	public List<IcConnectorInfo> getAvailableLocalConnectors() {
		List<IcConnectorInfo> localConnectorInfos = new ArrayList<>();
		IcConnectorInfoImpl dto = new IcConnectorInfoImpl("Testovací konektor", "categori test", new IcConnectorKeyImpl(getImplementationType(), "eu.bcvsolutions.connectors.test", "0.0.1", "Test connector"));
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
	public IcConnectorConfiguration getConnectorConfiguration(IcConnectorKey key) {
		Assert.notNull(key);
		IcConnectorConfigurationImpl dto = new IcConnectorConfigurationImpl();
		IcConfigurationPropertiesImpl propertiesDto = new IcConfigurationPropertiesImpl();
		IcConfigurationPropertyImpl propertyDto = new IcConfigurationPropertyImpl();
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
	public IcSchema getSchema(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration) {
		return null;
	}

	@Override
	public IcConnectorConfiguration getRemoteConnectorConfiguration(IcConnectorServer server, IcConnectorKey key) {
		return null;
	}

	@Override
	public List<IcConnectorInfo> getAvailableRemoteConnectors(IcConnectorServer server) {
		return null;
	}

	public void validate(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void test(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration) {
		// TODO Auto-generated method stub
	}

}
