package eu.bcvsolutions.idm.acc.service.impl.mock;

import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaObjectClassFilter;
import eu.bcvsolutions.idm.acc.repository.SysSystemRepository;
import eu.bcvsolutions.idm.acc.service.api.FormPropertyManager;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemFormValueService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.impl.DefaultSysSystemService;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationFacade;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Mock system service. Primary use in wizard AD test. We don't have an AD system, so check system and generate schema operations are mocked.
 *
 * @author Vít Švanda
 * @since 10.8.0
 */
public class MockSysSystemService extends DefaultSysSystemService {
	
	@Autowired
	private SysSchemaObjectClassService schemaService;


	public MockSysSystemService(SysSystemRepository systemRepository, FormService formService, IcConfigurationFacade icConfigurationFacade, SysSchemaObjectClassService objectClassService, SysSchemaAttributeService attributeService, SysSyncConfigService synchronizationConfigService, FormPropertyManager formPropertyManager, ConfidentialStorage confidentialStorage, IcConnectorFacade connectorFacade, SysSystemFormValueService systemFormValueService, SysSystemMappingService systemMappingService, SysSystemAttributeMappingService systemAttributeMappingService, SysSchemaObjectClassService schemaObjectClassService, EntityEventManager entityEventManager) {
		super(systemRepository, formService, icConfigurationFacade, objectClassService, attributeService, synchronizationConfigService, formPropertyManager, confidentialStorage, connectorFacade, systemFormValueService, systemMappingService, systemAttributeMappingService, schemaObjectClassService, entityEventManager);
	}

	@Override
	public void checkSystem(SysSystemDto system) {
		// Mock - We don't have an AD.
	}

	@Override
	public List<SysSchemaObjectClassDto> generateSchema(SysSystemDto system) {
		// Mock - We don't have an AD.
		SysSchemaObjectClassFilter schemaFilter = new SysSchemaObjectClassFilter();
		schemaFilter.setSystemId(system.getId());
		
		return schemaService.find(schemaFilter, null).getContent();
	}
}
