package eu.bcvsolutions.idm.acc.rest.impl;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.acc.dto.SysConnectorServerDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemFilter;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyGenerateType;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.TestHelper;

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
	
	@Test
	public void testGetRemoteServerPasswordContainsAsterisks() throws Exception {
		String password = "testPassword123654";
		SysSystemDto system = prepareDto();
		system.setRemote(true);
		SysConnectorServerDto conServer = new SysConnectorServerDto();
		conServer.setPassword(new GuardedString(password));
		conServer.setHost("localhost");
		system.setConnectorServer(conServer);
		system = createDto(system);
		
		ObjectMapper mapper = getMapper();
				
		String response = getMockMvc().perform(get(getDetailUrl(system.getId()))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andReturn()
                .getResponse()
                .getContentAsString();
		SysSystemDto gotSystem = (SysSystemDto) mapper.readValue(response, SysSystemDto.class);
		
		Assert.assertNotNull(gotSystem);
		Assert.assertEquals(GuardedString.SECRED_PROXY_STRING, gotSystem.getConnectorServer().getPassword().asString());
	}
	
	@Test
	public void testGetRemoteServerPasswordContainsAsterisksByCode() throws Exception {
		String password = "testPassword123654";
		SysSystemDto system = prepareDto();
		system.setRemote(true);
		SysConnectorServerDto conServer = new SysConnectorServerDto();
		conServer.setPassword(new GuardedString(password));
		conServer.setHost("localhost");
		system.setConnectorServer(conServer);
		system = createDto(system);
		
		ObjectMapper mapper = getMapper();
				
		String response = getMockMvc().perform(get(getDetailUrl(system.getCode()))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andReturn()
                .getResponse()
                .getContentAsString();
		SysSystemDto gotSystem = (SysSystemDto) mapper.readValue(response, SysSystemDto.class);
		
		Assert.assertNotNull(gotSystem);
		Assert.assertEquals(GuardedString.SECRED_PROXY_STRING, gotSystem.getConnectorServer().getPassword().asString());
	}
	
	@Test
	public void testGetRemoteServerPasswordContainsAsterisksByUUIDCode() throws Exception {
		String password = "testPassword123654";
		SysSystemDto system = prepareDto();
		// System name is UUID in string. For testing if will be used lookupService for get correct system.
		String codeFromUUID = UUID.randomUUID().toString();
		system.setName(codeFromUUID);
		system.setRemote(true);
		SysConnectorServerDto conServer = new SysConnectorServerDto();
		conServer.setPassword(new GuardedString(password));
		conServer.setHost("localhost");
		system.setConnectorServer(conServer);
		createDto(system);
		
		ObjectMapper mapper = getMapper();
				
		String response = getMockMvc().perform(get(getDetailUrl(codeFromUUID))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andReturn()
                .getResponse()
                .getContentAsString();
		SysSystemDto gotSystem = (SysSystemDto) mapper.readValue(response, SysSystemDto.class);
		
		Assert.assertNotNull(gotSystem);
		Assert.assertEquals(GuardedString.SECRED_PROXY_STRING, gotSystem.getConnectorServer().getPassword().asString());
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
