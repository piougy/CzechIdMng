package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemFilter;
import eu.bcvsolutions.idm.acc.event.SystemEvent;
import eu.bcvsolutions.idm.core.api.service.CloneableService;
import eu.bcvsolutions.idm.core.api.service.CodeableService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormableDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;

/**
 * Target system configuration service.
 * 
 * @author Radek Tomiška
 */
public interface SysSystemService extends 
		FormableDtoService<SysSystemDto, SysSystemFilter>,
		CodeableService<SysSystemDto>, 
		CloneableService<SysSystemDto>, 
		AuthorizableService<SysSystemDto>,
		ScriptEnabled {

	String REMOTE_SERVER_PASSWORD = "remoteServerPassword";
	String CONNECTOR_FRAMEWORK_CZECHIDM = "czechidm";
	// Pooling
	String POOLING_DEFINITION_KEY = "pooling-connector-configuration";
	String OPERATION_OPTIONS_DEFINITION_KEY = "operation-options-connector-configuration";
	String POOLING_SUPPORTED_PROPERTY = "poolingSupported";
	String POOLING_SUPPORTED_NAME = "Pooling supported";
	String MAX_IDLE_PROPERTY = "maxIdle";
	String MAX_IDLE_NAME = "Max idle objects";
	String MIN_IDLE_PROPERTY = "minIdle";
	String MIN_IDLE_NAME = "Minimum number of idle objects";
	String MAX_OBJECTS_PROPERTY = "maxObjects";
	String MAX_OBJECTS_NAME = "Max objects";
	String MAX_WAIT_PROPERTY = "maxWait";
	String MAX_WAIT_NAME = "Max time to wait";
	String MIN_TIME_TO_EVIC_PROPERTY = "minEvictableIdleTimeMillis";
	String MIN_TIME_TO_EVIC_NAME = "Minimum time to wait before evicting";

	
	/**
	 * Generate and persist schema to system. Use connector info and connector
	 * configuration stored in system. If system contains any schema, then will be
	 * every object compare and only same will be regenerated
	 * 
	 * @param system
	 * @return all schemas on system
	 */
	List<SysSchemaObjectClassDto> generateSchema(SysSystemDto system);

	/**
	 * Returns connector configuration for given system
	 * 
	 * @param system
	 * @return
	 */
	IcConnectorConfiguration getConnectorConfiguration(SysSystemDto system);

	/**
	 * Returns form definition to given connector key. If no definition for
	 * connector type is found, then new definition is created by connector
	 * properties.
	 * 
	 * @param connectorKey
	 * @return
	 * @deprecated @since 10.8.0 use {@link #getConnectorFormDefinition(SysSystemDto)}
	 */
	@Deprecated
	IdmFormDefinitionDto getConnectorFormDefinition(IcConnectorInstance connectorInstance);
	
	/**
	 * Returns form definition to given connector key. If no definition for
	 * connector type is found, then new definition is created by connector
	 * properties.
	 * 
	 * @param system
	 * @return form definition
	 * @since 10.8.0
	 */
	IdmFormDefinitionDto getConnectorFormDefinition(SysSystemDto system);
	
	/**
	 * Returns form-definition (for pool configuration) by given connector key. If no definition for
	 * connector type is found, then new definition is created by connector
	 * pool configuration.
	 * 
	 * @param connectorInstance
	 * @return
	 * @deprecated @since 10.8.0 use {@link #getPoolingConnectorFormDefinition(SysSystemDto)}
	 */
	@Deprecated
	IdmFormDefinitionDto getPoolingConnectorFormDefinition(IcConnectorInstance connectorInstance);
	
	/**
	 * Returns form-definition (for pool configuration) by given connector key. If no definition for
	 * connector type is found, then new definition is created by connector
	 * pool configuration.
	 * 
	 * @param system
	 * @return form definition
	 * @since 10.8.0
	 */
	IdmFormDefinitionDto getPoolingConnectorFormDefinition(SysSystemDto system);

	/**
	 * Check if is connector works fine
	 * 
	 * @param system
	 */
	void checkSystem(SysSystemDto system);

	//
	// TODO: move to test after FE form implementation
	@Deprecated
	IcConnectorKey getTestConnectorKey();

	@Deprecated
	SysSystemDto createTestSystem();

	/**
	 * Read connector object by given UID. Method call directly connector
	 * (AccAccount or SysSystemEntity is not required).
	 * 
	 * @param systemId
	 * @param uid
	 * @param objectClass
	 * @return
	 */
	IcConnectorObject readConnectorObject(UUID systemId, String uid, IcObjectClass objectClass);

	/**
	 * @param connectorInstance
	 * @return
	 * @since 10.2.0
	 * @deprecated @since 10.8.0 use {@link #getOperationOptionsConnectorFormDefinition(SysSystemDto)}
	 */
	@Deprecated
	IdmFormDefinitionDto getOperationOptionsConnectorFormDefinition(IcConnectorInstance connectorInstance);
	
	/**
	 * Returns definition for additional operation options.
	 * 
	 * @param connectorInstance
	 * @return
	 * @since 10.8.0
	 */
	IdmFormDefinitionDto getOperationOptionsConnectorFormDefinition(SysSystemDto system);

	/**
	 * Duplicate (create/persist new) system with all configurations.
	 * Do not call directly but emit {@link SystemEvent} DUPLICATE instead.
	 *   
	 * @param id
	 * @return
	 */
	SysSystemDto duplicate(UUID id);

	/**
	 * Return {@link IcConnectorInstance} for given system.
	 * {@link IcConnectorInstance} has filled password for remote connector server
	 * 
	 * @param system
	 * @return
	 */
	IcConnectorInstance getConnectorInstance(SysSystemDto system);
}
