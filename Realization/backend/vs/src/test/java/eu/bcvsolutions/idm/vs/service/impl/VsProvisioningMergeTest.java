package eu.bcvsolutions.idm.vs.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysAttributeControlledValueService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.config.domain.EventConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.vs.TestHelper;
import eu.bcvsolutions.idm.vs.dto.VsSystemDto;

/**
 * Provisioning merge test. We want to use virtual system (has multivalue
 * attributes), that is reason why is test in this module.
 *
 * @author Svanda
 */
// @Transactional
public class VsProvisioningMergeTest extends AbstractIntegrationTest {

	private static final String RIGHTS_ATTRIBUTE = "rights";
	private static final String ONE_VALUE = "ONE";
	private static final String TWO_VALUE = "TWO";

	@Autowired
	private TestHelper helper;
	@Autowired
	private SysSystemAttributeMappingService attributeMappingService;
	@Autowired
	private SysRoleSystemAttributeService roleSystemAttributeService;
	@Autowired
	private SysSystemMappingService mappingService;
	@Autowired
	private AccAccountService accountService;
	@Autowired
	private ModuleService moduleService;
	

	@Before
	public void login() {
		loginAsAdmin();
		moduleService.enable(AccModuleDescriptor.MODULE_ID);
	}

	@After
	public void logout() {
		super.logout();
	}

	
	
	@Test
	public void testAttribteControlledValues() {
		moduleService.enable(AccModuleDescriptor.MODULE_ID);
		List<ModuleDescriptor> enabledModules = moduleService.getEnabledModules();
		List<ModuleDescriptor> installedModules = moduleService.getInstalledModules();
		
		VsSystemDto config = new VsSystemDto();
		config.setName(helper.createName());
		config.setCreateDefaultRole(false);
		
		SysSystemDto system = helper.createVirtualSystem(config);
		IdmRoleDto roleOne = helper.createRole();
		IdmRoleDto roleTwo = helper.createRole();

		SysRoleSystemDto roleSystemOne = helper.createRoleSystem(roleOne, system);
		SysRoleSystemDto roleSystemTwo = helper.createRoleSystem(roleTwo, system);
		SysSystemMappingDto mapping = mappingService.findProvisioningMapping(system.getId(), SystemEntityType.IDENTITY);
		
		SysSystemAttributeMappingFilter attributeFilter = new SysSystemAttributeMappingFilter();
		attributeFilter.setSystemMappingId(mapping.getId());
		attributeFilter.setSchemaAttributeName(RIGHTS_ATTRIBUTE);
		List<SysSystemAttributeMappingDto> attributes = attributeMappingService.find(attributeFilter, null).getContent();
		assertEquals(1, attributes.size());
		SysSystemAttributeMappingDto rightsAttribute = attributes.get(0);

		SysRoleSystemAttributeDto roleAttributeOne = new SysRoleSystemAttributeDto();
		roleAttributeOne.setName(RIGHTS_ATTRIBUTE);
		roleAttributeOne.setRoleSystem(roleSystemOne.getId());
		roleAttributeOne.setStrategyType(AttributeMappingStrategyType.MERGE);
		roleAttributeOne.setSystemAttributeMapping(rightsAttribute.getId());
		roleAttributeOne.setTransformToResourceScript("return '" + ONE_VALUE + "';");
		roleAttributeOne = roleSystemAttributeService.saveInternal(roleAttributeOne);

		SysRoleSystemAttributeDto roleAttributeTwo = new SysRoleSystemAttributeDto();
		roleAttributeTwo.setName(RIGHTS_ATTRIBUTE);
		roleAttributeTwo.setRoleSystem(roleSystemTwo.getId());
		roleAttributeTwo.setStrategyType(AttributeMappingStrategyType.MERGE);
		roleAttributeTwo.setSystemAttributeMapping(rightsAttribute.getId());
		roleAttributeTwo.setTransformToResourceScript("return '" + TWO_VALUE + "';");
		roleAttributeTwo = roleSystemAttributeService.saveInternal(roleAttributeTwo);
		
		IdmIdentityDto identity = helper.createIdentity();
		helper.createIdentityRole(identity, roleOne);
		helper.createIdentityRole(identity, roleTwo);
		
		
		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setSystemId(system.getId());
		List<AccAccountDto> accounts = accountService.find(accountFilter, null).getContent();
		assertEquals(1, accounts.size());
		AccAccountDto account = accounts.get(0);
		
		IcConnectorObject connectorObject = accountService.getConnectorObject(account);
		IcAttribute rightsAttributeFromSystem = connectorObject.getAttributeByName(RIGHTS_ATTRIBUTE);
		List<Object> values = rightsAttributeFromSystem.getValues();
		
		assertEquals(2, values.size());
		assertTrue(values.contains(ONE_VALUE));
		assertTrue(values.contains(TWO_VALUE));
		
	}

}
