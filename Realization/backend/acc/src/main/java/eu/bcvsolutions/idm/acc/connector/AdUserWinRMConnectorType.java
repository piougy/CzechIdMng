package eu.bcvsolutions.idm.acc.connector;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * AD+WinRM wizard for users.
 *
 * @author Vít Švanda
 * @since 10.8.0
 */
@Component(AdUserWinRMConnectorType.NAME)
public class AdUserWinRMConnectorType extends AdUserConnectorType {

	public static final String NAME = "ad-winrm-connector-type";

	@Override
	public String getConnectorName() {
		return "net.tirasa.connid.bundles.cmd.CmdConnector";
	}

	@Override
	public String getIconKey() {
		return "ad-connector-icon";
	}

	@Override
	public Map<String, String> getMetadata() {
		// Default values:
		Map<String, String> metadata = super.getMetadata();
		metadata.put(SYSTEM_NAME, this.findUniqueSystemName("MS AD+WinRM - Users", 1));
		return metadata;
	}

	@Override
	protected void initDefaultConnectorSettings(SysSystemDto systemDto, IdmFormDefinitionDto connectorFormDef) {
		super.initDefaultConnectorSettings(systemDto, connectorFormDef);
		// Additional connector default connector settings for WinRM connector.
		this.setValueToConnectorInstance("testViaAd", true, systemDto, connectorFormDef);
		this.setValueToConnectorInstance("searchViaAd", true, systemDto, connectorFormDef);
		this.setValueToConnectorInstance("deleteViaAd", true, systemDto, connectorFormDef);
		this.setValueToConnectorInstance("updateViaAd", true, systemDto, connectorFormDef);
		this.setValueToConnectorInstance("createViaAd", true, systemDto, connectorFormDef);
	}

	@Override
	public int getOrder() {
		return 205;
	}

}
