package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemFilter;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyGenerateType;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;

/**
 * Controller tests:
 * - CRUD
 * - filters
 * 
 * @author Radek Tomiška
 *
 */
public class SysSystemControllerRestTest extends AbstractReadWriteDtoControllerRestTest<SysSystemDto> {

	@Autowired private SysSystemController controller;
	@Autowired private IdmPasswordPolicyService passwordPolicyService;
	
	@Override
	protected AbstractReadWriteDtoController<SysSystemDto, ?> getController() {
		return controller;
	}

	@Override
	protected SysSystemDto prepareDto() {
		SysSystemDto dto = new SysSystemDto();
		dto.setName(getHelper().createName());
		return dto;
	}
	
	@Test
	public void testCreateDisabledProvisioningSystemWithoutDisabledFlag() {
		SysSystemDto system = prepareDto();
		system.setDisabledProvisioning(true);
		//
		system = createDto(system);
		//
		Assert.assertTrue(system.isDisabledProvisioning());
		Assert.assertTrue(system.isDisabled());
		Assert.assertFalse(system.isReadonly());
	}
	
	@Test
	public void testCreateDisabledProvisioningSystemWithReadOnlyFlag() {
		SysSystemDto system = prepareDto();
		system.setReadonly(true);
		system.setDisabledProvisioning(true);
		//
		system = createDto(system);
		//
		Assert.assertTrue(system.isDisabledProvisioning());
		Assert.assertFalse(system.isDisabled());
		Assert.assertTrue(system.isReadonly());
	}
	
	@Override
	protected boolean supportsFormValues() {
		// TODO: Connector eav are controlled only.
		return false;
	}
	
	@Test
	public void testFindByVirtual() {
		SysSystemDto system = prepareDto();
		system.setVirtual(true);
		system.setDescription(getHelper().createName());
		SysSystemDto systemOne = createDto(system);
		system = prepareDto();
		system.setVirtual(false);
		system.setDescription(systemOne.getDescription());
		SysSystemDto systemTwo = createDto(system);
		//
		SysSystemFilter filter = new SysSystemFilter();
		filter.setVirtual(Boolean.TRUE);
		filter.setText(system.getDescription());
		List<SysSystemDto> results = find(filter);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().allMatch(r -> r.getId().equals(systemOne.getId())));
		//
		filter.setVirtual(Boolean.FALSE);
		results = find(filter);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().allMatch(r -> r.getId().equals(systemTwo.getId())));
		//
		filter.setVirtual(null);
		results = find(filter);
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(systemOne.getId())));
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(systemTwo.getId())));
	}
	
	@Test
	public void testFindByPasswordPolicyGeneration() {
		SysSystemDto system = prepareDto();
		system.setPasswordPolicyGenerate(createPasswordPolicy(IdmPasswordPolicyType.GENERATE));
		system.setDescription(getHelper().createName());
		SysSystemDto systemOne = createDto(system);
		system = prepareDto();
		system.setPasswordPolicyGenerate(createPasswordPolicy(IdmPasswordPolicyType.GENERATE));
		system.setDescription(systemOne.getDescription());
		createDto(system); // mock
		//
		SysSystemFilter filter = new SysSystemFilter();
		filter.setPasswordPolicyGenerationId(systemOne.getPasswordPolicyGenerate());
		filter.setText(system.getDescription());
		List<SysSystemDto> results = find(filter);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().allMatch(r -> r.getId().equals(systemOne.getId())));
	}
	
	@Test
	public void testFindByPasswordPolicyValidation() {
		SysSystemDto system = prepareDto();
		system.setPasswordPolicyValidate(createPasswordPolicy(IdmPasswordPolicyType.VALIDATE));
		system.setDescription(getHelper().createName());
		SysSystemDto systemOne = createDto(system);
		system = prepareDto();
		system.setPasswordPolicyValidate(createPasswordPolicy(IdmPasswordPolicyType.VALIDATE));
		system.setDescription(systemOne.getDescription());
		createDto(system); // mock
		//
		SysSystemFilter filter = new SysSystemFilter();
		filter.setPasswordPolicyValidationId(systemOne.getPasswordPolicyValidate());
		filter.setText(system.getDescription());
		List<SysSystemDto> results = find(filter);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().allMatch(r -> r.getId().equals(systemOne.getId())));
	}
	
	private UUID createPasswordPolicy(IdmPasswordPolicyType type) {
		IdmPasswordPolicyDto passPolicy = new IdmPasswordPolicyDto();
		passPolicy.setName(getHelper().createName());
		passPolicy.setType(type);
		passPolicy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		passPolicy.setMinPasswordLength(5);
		passPolicy.setMaxPasswordLength(12);
		passPolicy = passwordPolicyService.save(passPolicy);
		//
		return passPolicy.getId();
	}
}
