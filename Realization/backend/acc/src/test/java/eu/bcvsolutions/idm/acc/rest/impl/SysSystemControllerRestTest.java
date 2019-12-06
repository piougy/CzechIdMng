package eu.bcvsolutions.idm.acc.rest.impl;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Controller tests
 * - CRUD
 * 
 * @author Radek Tomiška
 *
 */
public class SysSystemControllerRestTest extends AbstractReadWriteDtoControllerRestTest<SysSystemDto> {

	@Autowired private SysSystemController controller;
	
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
		// TODO: connector eav are controlled
		return false;
	}
}
