package eu.bcvsolutions.idm.acc.rest.impl;

import static org.junit.Assert.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

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
	
	/**
	 * Max time out for long-polling is 30 seconds.
	 */
	@Test
	public void testLongPollingExpirationTimeOut() {
		SysSystemDto system = createDto(prepareDto());
		
		try {
			MvcResult result = getMockMvc()
					.perform(get(String.format("%s/check-running-sync", getDetailUrl(system.getId())))
					.with(authentication(getAdminAuthentication())))
					.andExpect(MockMvcResultMatchers.request().asyncStarted())
					.andReturn();

			getMockMvc().perform(MockMvcRequestBuilders.asyncDispatch(result))
					.andDo(MockMvcResultHandlers.log())
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			fail();
		} catch (Exception e) {
			Assert.assertTrue(e.getLocalizedMessage().endsWith("was not set during the specified timeToWait=30000"));
		}
	}
	
	@Override
	protected boolean supportsFormValues() {
		// TODO: connector eav are controlled
		return false;
	}
}
