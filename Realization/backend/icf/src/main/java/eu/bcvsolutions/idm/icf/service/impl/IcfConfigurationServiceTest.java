package eu.bcvsolutions.idm.icf.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorInfo;
import eu.bcvsolutions.idm.icf.dto.IcfConfigurationPropertiesDto;
import eu.bcvsolutions.idm.icf.dto.IcfConfigurationPropertyDto;
import eu.bcvsolutions.idm.icf.dto.IcfConnectorConfigurationDto;
import eu.bcvsolutions.idm.icf.dto.IcfConnectorInfoDto;
import eu.bcvsolutions.idm.icf.dto.IcfConnectorKeyDto;
import eu.bcvsolutions.idm.icf.exception.IcfException;
import eu.bcvsolutions.idm.icf.service.api.IcfConfigurationService;

@Service
public class IcfConfigurationServiceTest implements IcfConfigurationService {

	@Autowired
	public IcfConfigurationServiceTest(IcfConfigurationAggregatorService icfConfigurationAggregator) {
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
		return "test";
	}

	/**
	 * Return available local connectors for this ICF implementation
	 * 
	 * @return
	 */
	@Override
	public List<IcfConnectorInfo> getAvailableLocalConnectors() {
		List<IcfConnectorInfo> localConnectorInfos = new ArrayList<>();
		IcfConnectorInfoDto dto = new IcfConnectorInfoDto("Testovací konektor", "categori test", new IcfConnectorKeyDto(getIcfType(), "eu.bcvsolutions.connectors.test", "0.0.1", "Test connector"));
		localConnectorInfos.add(dto);
		return localConnectorInfos;
	}

	/**
	 * Return find connector default configuration by connector info
	 * 
	 * @param info
	 * @return
	 */
	@Override
	public IcfConnectorConfiguration getConnectorConfiguration(IcfConnectorInfo info) {
		Assert.notNull(info);
		IcfConnectorConfigurationDto dto = new IcfConnectorConfigurationDto();
		IcfConfigurationPropertiesDto propertiesDto = new IcfConfigurationPropertiesDto();
		IcfConfigurationPropertyDto propertyDto = new IcfConfigurationPropertyDto();
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

}
