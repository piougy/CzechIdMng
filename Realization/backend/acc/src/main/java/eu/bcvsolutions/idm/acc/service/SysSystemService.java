package eu.bcvsolutions.idm.acc.service;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.core.api.dto.QuickFilter;
import eu.bcvsolutions.idm.core.api.service.IdentifiableByNameEntityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorInfo;
import eu.bcvsolutions.idm.icf.dto.IcfConnectorInfoDto;
import eu.bcvsolutions.idm.icf.dto.IcfConnectorKeyDto;

/**
 * Target system configuration service 
 * 
 * @author Radek Tomiška
 *
 */
public interface SysSystemService extends ReadWriteEntityService<SysSystem, QuickFilter>, IdentifiableByNameEntityService<SysSystem> {

	/**
	 * Generate and persist schema to system. 
	 * Use connector info and connector configuration stored in system.
	 * @param system
	 */
	void generateSchema(SysSystem system);
	
	/**
	 * Return connector info for given system
	 * @param system
	 * @return
	 */
	IcfConnectorInfo getConnectorInfo(SysSystem system);

	/**
	 * Return connector configuration for given system
	 * @param system
	 * @return
	 */
	IcfConnectorConfiguration getConnectorConfiguration(SysSystem system);
}
