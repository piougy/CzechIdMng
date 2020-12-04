package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;

import eu.bcvsolutions.idm.acc.dto.ConnectorTypeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;

/**
 * Connector manager controls connector types, which extends standard IC connectors for more metadata (image, wizard, ...).
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
public interface ConnectorManager {

	/**
	 * Returns all registered connector types.
	 *
	 * @return
	 */
	List<ConnectorType> getSupportedTypes();

	/**
	 * Get connector type by ID.
	 */
	ConnectorType getConnectorType(String id);

	/**
	 * Converts connectorType to DTO version.
	 */
	ConnectorTypeDto convertTypeToDto(ConnectorType connectorType);

	/**
	 * Converts InfoConnectorInfo to the ConnectorTypeDto.
	 */
	ConnectorTypeDto convertIcConnectorInfoToDto(IcConnectorInfo info);

	/**
	 * Execute connector type -> execute some wizard step.
	 */
	ConnectorTypeDto execute(ConnectorTypeDto connectorType);

	/**
	 * Load data for specific wizard/step (for open existing system in the wizard).
	 */
	ConnectorTypeDto load(ConnectorTypeDto connectorType);

	/**
	 * Find connector key by connector name.
	 */
	IcConnectorKey findConnectorKey(String connectorName);

	/**
	 * Find connector type by system.
	 */
	ConnectorType findConnectorTypeBySystem(SysSystemDto systemDto);
}
