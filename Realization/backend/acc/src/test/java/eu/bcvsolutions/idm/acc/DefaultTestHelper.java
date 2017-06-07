package eu.bcvsolutions.idm.acc;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.filter.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.service.DefaultSysAccountManagementServiceTest;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Acc / Provisioning test helper
 * 
 * @author Radek Tomiška
 */
@Component
public class DefaultTestHelper implements TestHelper {
	
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private DefaultSysAccountManagementServiceTest accountManagementServiceTest;
	@Autowired private SysSystemService systemService;
	@Autowired private SysSystemMappingService systemMappingService;
	@Autowired private SysSystemAttributeMappingService systemAttributeMappingService;
	@Autowired private SysSchemaAttributeService schemaAttributeService;
	@Autowired private EntityManager entityManager;
	@Autowired private SysRoleSystemService roleSystemService;

	@Override
	public IdmIdentityDto createIdentity() {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername("test" + "-" + UUID.randomUUID());
		identity.setFirstName("Test");
		identity.setLastName("Identity");
		identity.setPassword(new GuardedString("password"));
		return identityService.save(identity);
	}
	
	@Override
	public IdmRole createRole() {
		IdmRole role = new IdmRole();
		role.setName("test" + "-" + UUID.randomUUID());
		return roleService.save(role);
	}
	
	@Override
	public IdmIdentityRoleDto createIdentityRole(IdmIdentityDto identity, IdmRole role) {
		return createIdentityRole(identityContractService.getPrimeContract(identity.getId()), role);
	}
	
	private IdmIdentityRoleDto createIdentityRole(IdmIdentityContractDto identityContract, IdmRole role) {
		IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
		identityRole.setIdentityContract(identityContract.getId());
		identityRole.setRole(role.getId());
		return identityRoleService.save(identityRole);
	}
	
	@Override
	public SysSystem createSystem(String tableName, boolean withMapping) {
		// create test system
		SysSystem system = accountManagementServiceTest.createTestSystem("test_resource");
		//
		// generate schema for system
		List<SysSchemaObjectClass> objectClasses = systemService.generateSchema(system);
		
		if (!withMapping) {
			return system;
		}
		
		SysSystemMapping systemMapping = new SysSystemMapping();
		systemMapping.setName("default_" + System.currentTimeMillis());
		systemMapping.setEntityType(SystemEntityType.IDENTITY);
		systemMapping.setOperationType(SystemOperationType.PROVISIONING);
		systemMapping.setObjectClass(objectClasses.get(0));
		systemMapping = systemMappingService.save(systemMapping);

		SchemaAttributeFilter schemaAttributeFilter = new SchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());
		
		Page<SysSchemaAttribute> schemaAttributesPage = schemaAttributeService.find(schemaAttributeFilter, null);
		for(SysSchemaAttribute schemaAttr : schemaAttributesPage) {
			if ("__NAME__".equals(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeMapping = new SysSystemAttributeMapping();
				attributeMapping.setUid(true);
				attributeMapping.setEntityAttribute(true);
				attributeMapping.setIdmPropertyName(IdmIdentity_.username.getName());
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr);
				attributeMapping.setSystemMapping(systemMapping);
				systemAttributeMappingService.save(attributeMapping);

			} else if ("firstname".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeMapping = new SysSystemAttributeMapping();
				attributeMapping.setIdmPropertyName(IdmIdentity_.firstName.getName());
				attributeMapping.setSchemaAttribute(schemaAttr);
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSystemMapping(systemMapping);
				systemAttributeMappingService.save(attributeMapping);

			} else if ("lastname".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeMapping = new SysSystemAttributeMapping();
				attributeMapping.setIdmPropertyName(IdmIdentity_.lastName.getName());
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr);
				attributeMapping.setSystemMapping(systemMapping);
				systemAttributeMappingService.save(attributeMapping);

			} else if (IcConnectorFacade.PASSWORD_ATTRIBUTE_NAME.equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeMapping = new SysSystemAttributeMapping();
				attributeMapping.setIdmPropertyName("password");
				attributeMapping.setSchemaAttribute(schemaAttr);
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSystemMapping(systemMapping);
				systemAttributeMappingService.save(attributeMapping);

			} else if ("email".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeMapping = new SysSystemAttributeMapping();
				attributeMapping.setIdmPropertyName(IdmIdentity_.email.getName());
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr);
				attributeMapping.setSystemMapping(systemMapping);
				systemAttributeMappingService.save(attributeMapping);

			}
		}		
		return system;
	}
	
	@Override
	public SysSystemMapping getDefaultMapping(SysSystem system) {
		List<SysSystemMapping> mappings = systemMappingService.findBySystem(system, SystemOperationType.PROVISIONING, SystemEntityType.IDENTITY);
		if(mappings.isEmpty()) {
			throw new CoreException(String.format("Default mapping for system[%s] not found", system.getId()));
		}
		//
		return mappings.get(0);
	}
	
	@Override
	public SysRoleSystem createRoleSystem(IdmRole role, SysSystem system) {
		SysRoleSystem roleSystem = new SysRoleSystem();
		roleSystem.setRole(role);
		roleSystem.setSystem(system);
		// default mapping
		List<SysSystemMapping> mappings = systemMappingService.findBySystem(system, SystemOperationType.PROVISIONING, SystemEntityType.IDENTITY);
		//
		roleSystem.setSystemMapping(mappings.get(0));
		return roleSystemService.save(roleSystem);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public TestResource findResource(String uid) {
		return entityManager.find(TestResource.class, uid);
	}
}
