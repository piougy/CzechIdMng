package eu.bcvsolutions.idm.vs.service.api;

import java.util.UUID;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.ic.api.IcConnector;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.vs.connector.api.VsVirtualConnector;
import eu.bcvsolutions.idm.vs.dto.VsSystemDto;

/**
 * Service for virtual system
 * 
 * @author Svanda
 *
 */
public interface VsSystemService {

	static final String IMPLEMENTERS_PROPERTY = "implementers";
	static final String IMPLEMENTER_ROLES_PROPERTY = "implementerRoles";
	static final String ATTRIBUTES_PROPERTY = "attributes";
	// Multivalued default attribute
	static final String RIGHTS_ATTRIBUTE = "rights";

	/**
	 * Create virtual system. System will be included mapping by default fields
	 * 
	 * @param vsSystem
	 * @return
	 */
	SysSystemDto create(VsSystemDto vsSystem);

	/**
	 * Returns connector instance for given system
	 * 
	 * @param systemId
	 * @param connectorInfo
	 * @return
	 */
	IcConnector getConnectorInstance(UUID systemId, IcConnectorInfo connectorInfo);

	/**
	 * Returns connector info for given connector key
	 * 
	 * @param connectorKey
	 * @return
	 */
	IcConnectorInfo getConnectorInfo(String connectorKey);

	/**
	 * Returns initialised virtual connector for given system
	 * 
	 * @param systemId
	 * @param connectorKey
	 * @return
	 */
	VsVirtualConnector getVirtualConnector(UUID systemId, String connectorKey);

	/**
	 * Update virtual system configuration by given IC configuration (updates
	 * implementers, form definition)
	 * 
	 * @param configuration
	 * @param connectorClass
	 */
	void updateSystemConfiguration(IcConnectorConfiguration configuration, Class<? extends IcConnector> connectorClass);

}
