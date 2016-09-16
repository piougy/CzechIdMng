package eu.bcvsolutions.idm.workingpositions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import java.util.HashMap;
import java.util.Map;

import eu.bcvsolutions.idm.core.AbstractRestTest;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityWorkingPosition;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityWorkingPositionRepository;
import eu.bcvsolutions.idm.security.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.security.service.SecurityService;

/**
 * Test for get working positions for signed and unsigned user
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class WorkingPositionsSecurityTest extends AbstractRestTest {	
	
	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private IdmIdentityRepository identityRepository;
	
	@Autowired
	private IdmIdentityWorkingPositionRepository workingPositionRepository;
	
	@Autowired
	@Qualifier("objectMapper")
	protected ObjectMapper mapper;
	
	@Test
	public void getWorkingPositions() {	
		SecurityMockMvcRequestPostProcessors.securityContext(null);
		Exception ex = null;
		int status = 0;
		try {
			status = mockMvc.perform(get("/api/workingPositions/")).andReturn().getResponse().getStatus();
		} catch (Exception e) {
			ex = e;
		}
		assertNull(ex);
		
		assertEquals(403, status);
		
		MvcResult mvcResult = null;
		ex = null;
		status = 0;
		try {
			mvcResult = mockMvc.perform(get("/api/workingPositions/").with(authentication(getAuthentication()))).andReturn();
		} catch (Exception e) {
			ex = e;
		}
		
		assertNull(ex);
		assertNotNull(mvcResult);		
		assertEquals(200, mvcResult.getResponse().getStatus());
		
		logout();
	}
	
	@Test
	public void createWorkingPositions() {
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
			status = mockMvc.perform(post("/api/workingPositions/")
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
			status = mockMvc.perform(post("/api/workingPositions").with(authentication(getAuthentication()))
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
	public void deleteWorkingPositions() {
		SecurityMockMvcRequestPostProcessors.securityContext(null);

		IdmIdentity user = identityRepository.findOneByUsername("kopr");
		Page<IdmIdentityWorkingPosition> pages = workingPositionRepository.findByIdentity(user, null);
		
		long positionId = 0;
		for	(IdmIdentityWorkingPosition position : pages) {
			positionId = position.getId();
			break;
		}
		
		int status = 0;
		Exception ex = null;
		try {
			status = mockMvc.perform(delete("/api/workingPositions/" + positionId).contentType(MediaType.APPLICATION_JSON))
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
			status = mockMvc.perform(delete("/api/workingPositions/" + positionId).contentType(MediaType.APPLICATION_JSON)
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
		return new IdmJwtAuthentication("[SYSTEM]", null, securityService.getAllAvailableAuthorities());
	}
}
