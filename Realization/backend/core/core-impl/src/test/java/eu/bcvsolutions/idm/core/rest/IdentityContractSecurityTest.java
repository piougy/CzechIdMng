package eu.bcvsolutions.idm.core.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.IdentityDto;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;

/**
 * Test for get working positions for signed and unsigned user
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class IdentityContractSecurityTest extends AbstractRestTest {	
	
	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private IdmIdentityRepository identityRepository;
	
	@Autowired
	private IdmIdentityContractRepository identityContractRepository;
	
	@Autowired
	@Qualifier("objectMapper")
	protected ObjectMapper mapper;
	
	@Test
	public void getWorkPositions() {	
		SecurityMockMvcRequestPostProcessors.securityContext(null);
		Exception ex = null;
		int status = 0;
		try {
			status = getMockMvc().perform(get(BaseEntityController.BASE_PATH + "/identity-contracts")).andReturn().getResponse().getStatus();
		} catch (Exception e) {
			ex = e;
		}
		assertNull(ex);
		
		assertEquals(403, status);
		
		MvcResult mvcResult = null;
		ex = null;
		status = 0;
		try {
			mvcResult = getMockMvc().perform(get(BaseEntityController.BASE_PATH + "/identity-contracts").with(authentication(getAuthentication()))).andReturn();
		} catch (Exception e) {
			ex = e;
		}
		
		assertNull(ex);
		assertNotNull(mvcResult);		
		assertEquals(200, mvcResult.getResponse().getStatus());
		
		logout();
	}
	
	@Test
	public void createWorkPositions() {
		SecurityMockMvcRequestPostProcessors.securityContext(null);

		IdmIdentity user = identityRepository.findOneByUsername("kopr");
		
		Map<String, String> body = new HashMap<>();
		body.put("identity", "identity/" + user.getUsername());
		body.put("position", "TEST_POSITION");
		
        String jsonContent = null;
		try {
			jsonContent = mapper.writeValueAsString(body);
		} catch (JsonProcessingException e1) {
			e1.printStackTrace();
		}
		
		int status = 0;
		Exception ex = null;
		try {
			status = getMockMvc().perform(post(BaseEntityController.BASE_PATH +  "/identity-contracts")
					.content(jsonContent)
					.contentType(MediaType.APPLICATION_JSON))
					.andReturn()
					.getResponse()
					.getStatus();
		} catch (Exception e) {
			ex = e;
		}
		assertNull(ex);
		
		assertEquals(403, status);
		
		ex = null;
		status = 0;
		try {
			status = getMockMvc().perform(post(BaseEntityController.BASE_PATH + "/identity-contracts").with(authentication(getAuthentication()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonContent))
						.andReturn()
						.getResponse()
						.getStatus();
		} catch (Exception e) {
			ex = e;
		}
		assertNull(ex);
		
		assertEquals(201, status);
		
		logout();
	}
	
	@Test
	public void deleteWorkPositions() {
		SecurityMockMvcRequestPostProcessors.securityContext(null);

		IdmIdentity user = identityRepository.findOneByUsername("kopr");
		List<IdmIdentityContract> pages = identityContractRepository.findAllByIdentity(user, null);
		
		Serializable positionId = null;
		for	(IdmIdentityContract position : pages) {
			positionId = position.getId();
			break;
		}
		
		int status = 0;
		Exception ex = null;
		try {
			status = getMockMvc().perform(delete(BaseEntityController.BASE_PATH + "/identity-contracts/" + positionId).contentType(MediaType.APPLICATION_JSON))
					.andReturn()
					.getResponse()
					.getStatus();
		} catch (Exception e) {
			ex = e;
		}
		assertNull(ex);
		
		assertEquals(403, status);
		
		
		ex = null;
		status = 0;
		try {
			status = getMockMvc().perform(delete(BaseEntityController.BASE_PATH + "/identity-contracts/" + positionId).contentType(MediaType.APPLICATION_JSON)
						.with(authentication(getAuthentication())))
						.andReturn()
						.getResponse()
						.getStatus();
		} catch (Exception e) {
			ex = e;
		}
		assertNull(ex);
		
		assertEquals(204, status);
		
		logout();
	}
	
	private Authentication getAuthentication() {
		return new IdmJwtAuthentication(new IdentityDto("[SYSTEM]"), null, Lists.newArrayList(securityService.getAdminAuthority()), "test");
	}
}
