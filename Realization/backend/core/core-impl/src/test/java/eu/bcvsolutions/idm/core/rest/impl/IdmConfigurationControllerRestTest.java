package eu.bcvsolutions.idm.core.rest.impl;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;

import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.model.service.impl.LogbackLoggerManagerIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Identity controller tests
 * - TODO: move filters here
 * 
 * @author Radek Tomiška
 *
 */
public class IdmConfigurationControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmConfigurationDto> {

	@Autowired private IdmConfigurationController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmConfigurationDto, ?> getController() {
		return controller;
	}
	
	@Override
	protected boolean supportsPatch() {
		return false;
	}
	
	@Override
	protected boolean supportsAutocomplete() {
		return false;
	}

	@Override
	protected IdmConfigurationDto prepareDto() {
		IdmConfigurationDto dto = new IdmConfigurationDto();
		dto.setName(ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX + getHelper().createName());
		dto.setValue(getHelper().createName());
		return dto;
	}
	
	@Test
	public void testGetAllConfigurationsFromFiles() throws Exception {
		// configuration from files and logback logger
		String response = getMockMvc().perform(get(getBaseUrl() + "/all/file")
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
		//
		List<IdmConfigurationDto> dtos = getMapper().readValue(response, new TypeReference<List<IdmConfigurationDto>>() {});
		//
		Assert.assertFalse(dtos.isEmpty());
		Assert.assertTrue(dtos.stream().anyMatch(c -> c.getName().equals(ConfigurationService.PROPERTY_APP_INSTANCE_ID))); // all property files has this ...
		Assert.assertTrue(dtos.stream().anyMatch(c -> c.getName().endsWith(LogbackLoggerManagerIntegrationTest.TEST_PACKAGE_FROM_PROPERTIES))); // all logger configuration has this test package
	}
	
	@Test
	public void getAllConfigurationsFromEnvironment() throws Exception {
		// configuration from files and logback logger
		String response = getMockMvc().perform(get(getBaseUrl() + "/all/environment")
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
		//
		List<IdmConfigurationDto> dtos = getMapper().readValue(response, new TypeReference<List<IdmConfigurationDto>>() {});
		Assert.assertFalse(dtos.isEmpty());
		Assert.assertTrue(dtos.stream().anyMatch(c -> c.getName().equals("java.specification.version"))); // all property files has this ...
	}
}
