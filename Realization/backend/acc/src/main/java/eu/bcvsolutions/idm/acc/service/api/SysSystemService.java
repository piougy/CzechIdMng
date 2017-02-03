package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;

import eu.bcvsolutions.idm.acc.dto.filter.SysSystemFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.core.api.service.IdentifiableByNameEntityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;

/**
 * Target system configuration service 
 * 
 * @author Radek Tomiška
 *
 */
public interface SysSystemService extends ReadWriteEntityService<SysSystem, SysSystemFilter>, IdentifiableByNameEntityService<SysSystem> {

	/**
	 * Generate and persist schema to system. 
	 * Use connector info and connector configuration stored in system.
	 * If system contains any schema, then will be every object compare and only same will be regenerated
	 * 
	 * @param system
	 * @return all schemas on system
	 */
	List<SysSchemaObjectClass> generateSchema(SysSystem system);
	
	/**
	 * Returns connector configuration for given system
	 * 
	 * @param system
	 * @return
	 */
	IcConnectorConfiguration getConnectorConfiguration(SysSystem system);
	
	/**
	 * Returns form definition to given connector key. If no definition for connector type is found, then new definition is created by connector properties.
	 * 
	 * @param connectorKey
	 * @return
	 */
	IdmFormDefinition getConnectorFormDefinition(IcConnectorKey connectorKey);
	
	//
	// TODO: move to test after FE form implementation
	@Deprecated
	IcConnectorKey getTestConnectorKey();
	@Deprecated
	SysSystem createTestSystem();
}
