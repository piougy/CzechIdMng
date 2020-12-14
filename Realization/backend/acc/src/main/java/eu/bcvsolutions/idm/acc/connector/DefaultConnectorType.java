package eu.bcvsolutions.idm.acc.connector;

import eu.bcvsolutions.idm.acc.dto.ConnectorTypeDto;
import eu.bcvsolutions.idm.acc.dto.SysConnectorKeyDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.service.api.ConnectorManager;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Default connector type represents connector without a wizard.
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
@Component(DefaultConnectorType.NAME)
public class DefaultConnectorType extends AbstractConnectorType {

	public static final String NAME = "default-connector-type";
	private static final String CREATE_SYSTEM_SET_CONNECTOR = "create-system.systemNew";

	@Autowired
	private ConnectorManager connectorManager;

	@Override
	public String getConnectorName() {
		return NAME;
	}

	@Override
	@Transactional
	public ConnectorTypeDto execute(ConnectorTypeDto connectorTypeDto) {
		super.execute(connectorTypeDto);
		// Set connector to the new system.
		if (CREATE_SYSTEM_SET_CONNECTOR.equals(connectorTypeDto.getWizardStepName())) {
			String systemId = connectorTypeDto.getMetadata().get(SYSTEM_DTO_KEY);
			SysSystemDto systemDto = getSystemService().get(UUID.fromString(systemId), IdmBasePermission.READ);
			Assert.notNull(systemDto, "System must exists!");
			SysConnectorKeyDto connectorKey = systemDto.getConnectorKey();
			if (connectorKey == null) {
				// Find connector key and set it to the system.
				IcConnectorKey connectorKeyIc = connectorManager.findConnectorKey(connectorTypeDto.getConnectorName());
				Assert.notNull(connectorKeyIc, "Connector key was not found!");
				systemDto.setConnectorKey(new SysConnectorKeyDto(connectorKeyIc));
				systemDto = getSystemService().save(systemDto, IdmBasePermission.UPDATE);
				connectorTypeDto.getEmbedded().put(SYSTEM_DTO_KEY, systemDto);
			}
		}

		return connectorTypeDto;
	}

	@Override
	public int getOrder() {
		return 1000;
	}

}
