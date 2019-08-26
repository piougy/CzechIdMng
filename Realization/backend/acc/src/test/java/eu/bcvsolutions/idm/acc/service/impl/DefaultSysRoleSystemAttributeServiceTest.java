package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysAttributeControlledValueDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysAttributeControlledValueFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.scheduler.task.impl.AttributeControlledValuesRecalculationTaskExecutor;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysAttributeControlledValueService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.ic.api.IcObjectClassInfo;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * SysRoleSystemAttributeService tests
 * 
 * @author svandav
 * @author Radek Tomiška
 */
public class DefaultSysRoleSystemAttributeServiceTest extends AbstractIntegrationTest {

	@Autowired
	private TestHelper helper;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private SysRoleSystemService roleSystemService;
	@Autowired
	private SysRoleSystemAttributeService roleSystemAttributeService;
	@Autowired
	private SysSystemService systemService;
	@Autowired 
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired 
	private SysSystemAttributeMappingService systemAttributeMappingService;
	@Autowired 
	private AccAccountService accountService;
	@Autowired
	private SysAttributeControlledValueService attributeControlledValueService;
	@Autowired
	private IdmLongRunningTaskService longRunningTaskService;
	

	@Test
	public void testAddRoleMappingAttribute() {
		IdmRoleDto role = helper.createRole();
		SysSystemDto system = helper.createTestResourceSystem(true);

		SysRoleSystemAttributeDto attribute = roleSystemAttributeService.addRoleMappingAttribute(system.getId(),
				role.getId(), "FIRSTNAME", null, IcObjectClassInfo.ACCOUNT);
		Assert.assertNotNull(attribute);

		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setRoleId(role.getId());
		roleSystemFilter.setSystemId(system.getId());

		List<SysRoleSystemDto> roleSystems = roleSystemService.find(roleSystemFilter, null).getContent();
		Assert.assertEquals(1, roleSystems.size());
		SysRoleSystemDto roleSystem = roleSystems.get(0);
		SysSystemMappingDto systemMapping = roleSystemAttributeService.getSystemMapping(system.getId(),
				IcObjectClassInfo.ACCOUNT, SystemOperationType.PROVISIONING);
		Assert.assertNotNull(systemMapping);
		Assert.assertEquals(systemMapping.getId(), roleSystem.getSystemMapping());
	}
	
	@Test
	public void testArchiveControledValuesAfterRoleIsDeleted() {
		// prepare mapped attribute with controled value 
		IdmRoleDto role = helper.createRole();
		DateTime from = DateTime.now(); // preparation for LRT filter
		SysSystemDto system = helper.createTestResourceSystem(true);
		systemService.generateSchema(system);
		//
		// create eav attribute
		String attributeName = "EAV_ATTRIBUTE";
		SysSchemaAttributeFilter schemaAttFilter = new SysSchemaAttributeFilter();
		schemaAttFilter.setSystemId(system.getId());
		schemaAttFilter.setName(attributeName);
		List<SysSchemaAttributeDto> schemaAttrs = schemaAttributeService.find(schemaAttFilter, null).getContent();
		Assert.assertEquals(1, schemaAttrs.size());
		SysSchemaAttributeDto schemaAttributeDto = schemaAttrs.get(0);
		schemaAttributeDto.setMultivalued(true);
		schemaAttributeDto = schemaAttributeService.save(schemaAttributeDto);
		SysSystemMappingDto systemMapping = helper.getDefaultMapping(system);

		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());
		schemaAttributeFilter.setName(attributeName);
		List<SysSchemaAttributeDto> atts = schemaAttributeService.find(schemaAttributeFilter, null).getContent();
		Assert.assertEquals(1, atts.size());
		SysSchemaAttributeDto sysSchemaAttributeEav = atts.get(0);
		//
		// create eav attribute mapping with merge
		SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
		attributeMapping.setExtendedAttribute(true);
		attributeMapping.setName(attributeName);
		attributeMapping.setIdmPropertyName(attributeName);
		attributeMapping.setStrategyType(AttributeMappingStrategyType.MERGE);
		attributeMapping.setSchemaAttribute(sysSchemaAttributeEav.getId());
		attributeMapping.setSystemMapping(systemMapping.getId());
		attributeMapping = systemAttributeMappingService.save(attributeMapping);
		//
		// role system mapping with merge value
		SysRoleSystemDto roleSystem = helper.createRoleSystem(role, system);
		SysRoleSystemAttributeDto overloadedRoleOne = new SysRoleSystemAttributeDto();
		overloadedRoleOne.setSystemAttributeMapping(attributeMapping.getId());
		overloadedRoleOne.setEntityAttribute(false);
		overloadedRoleOne.setExtendedAttribute(true);
		overloadedRoleOne.setStrategyType(AttributeMappingStrategyType.MERGE);
		overloadedRoleOne.setName(attributeMapping.getName());
		overloadedRoleOne.setDisabledDefaultAttribute(false);
		overloadedRoleOne.setIdmPropertyName(attributeName);
		overloadedRoleOne.setRoleSystem(roleSystem.getId());
		String valueOne = helper.createName();
		overloadedRoleOne.setTransformToResourceScript("return '" + valueOne + "';");
		overloadedRoleOne = roleSystemAttributeService.save(overloadedRoleOne);
		//
		// assign role to identity
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityRoleDto identityRole = helper.createIdentityRole(identity, role);
		//
		List<AccAccountDto> accounts = accountService.getAccounts(system.getId(), identity.getId());
		Assert.assertEquals(1, accounts.size());
		//
		// find controlled values
		SysAttributeControlledValueFilter valueFilter = new SysAttributeControlledValueFilter();
		valueFilter.setAttributeMappingId(attributeMapping.getId());
		List<SysAttributeControlledValueDto> controlledValues = attributeControlledValueService.find(valueFilter, null).getContent();
		Assert.assertEquals(1, controlledValues.size());
		Assert.assertEquals(valueOne, controlledValues.get(0).getValue());
		//
		// try to delete role => role is still assigned 
		try {
			roleService.delete(role);
		} catch (ResultCodeException ex) {
			Assert.assertEquals(
					CoreResultCode.ROLE_DELETE_FAILED_IDENTITY_ASSIGNED.getCode(), 
					((ResultCodeException) ex).getError().getError().getStatusEnum());
		}
		//
		// check long running task is ended correctly
		IdmLongRunningTaskFilter taskFilter = new IdmLongRunningTaskFilter();
		taskFilter.setTaskType(AttributeControlledValuesRecalculationTaskExecutor.class.getCanonicalName());
		taskFilter.setFrom(from);
		//
		List<IdmLongRunningTaskDto> executedTasks = longRunningTaskService.find(taskFilter, null).getContent();
		int executedTasksSize = executedTasks.size();
		Assert.assertFalse(executedTasks.isEmpty());
		Assert.assertTrue(executedTasks.stream().allMatch(t -> t.getResultState() == OperationState.EXECUTED));
		controlledValues = attributeControlledValueService.find(valueFilter, null).getContent();
		Assert.assertEquals(1, controlledValues.size());
		Assert.assertEquals(valueOne, controlledValues.get(0).getValue());
		Assert.assertTrue(controlledValues.stream().allMatch(v -> !v.isHistoricValue()));
		//
		// remove assigned role and remove role with controller values
		identityRoleService.delete(identityRole);
		roleService.delete(role);
		//
		executedTasks = longRunningTaskService.find(taskFilter, null).getContent();
		Assert.assertTrue(executedTasksSize < executedTasks.size());
		Assert.assertTrue(executedTasks.stream().allMatch(t -> t.getResultState() == OperationState.EXECUTED));
		//
		controlledValues = attributeControlledValueService.find(valueFilter, null).getContent();
		Assert.assertEquals(1, controlledValues.size());
		Assert.assertEquals(valueOne, controlledValues.get(0).getValue());
		Assert.assertTrue(controlledValues.stream().allMatch(v -> v.isHistoricValue()));
		//
		// delete system (referential integrity just for sure)
		systemService.delete(system);
		controlledValues = attributeControlledValueService.find(valueFilter, null).getContent();
		Assert.assertTrue(controlledValues.isEmpty());
	}

}
