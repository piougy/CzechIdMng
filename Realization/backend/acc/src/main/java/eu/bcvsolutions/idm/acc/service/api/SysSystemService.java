package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;

import eu.bcvsolutions.idm.acc.dto.filter.SysSystemFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.core.api.service.IdentifiableByNameEntityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;

/**
 * Target system configuration service 
 * 
 * @author Radek Tomiška
 *
 */
public interface SysSystemService extends ReadWriteEntityService<SysSystem, SysSystemFilter>, IdentifiableByNameEntityService<SysSystem> {
	
	public static final String REMOTE_SERVER_PASSWORD = "remoteServerPassword";
	
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
	IdmFormDefinition getConnectorFormDefinition(IcConnectorInstance connectorInstance);
	
	/**
	 * Check if is connector works fine 
	 * @param system
	 */
	void checkSystem(SysSystem system);
	
	//
	// TODO: move to test after FE form implementation
	@Deprecated
	IcConnectorKey getTestConnectorKey();
	@Deprecated
	SysSystem createTestSystem();
	
	/**
	 * Return {@link IcConnectorObject} (object from system)  for entityUID
	 * 
	 * @param system
	 * @param operation
	 * @param systemEntityUid
	 * @param entityType
	 * @return
	 */
	IcConnectorObject readObject(SysSystem system, SysSystemMapping systemMapping, String systemEntityUid);

}
