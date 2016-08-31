package eu.bcvsolutions.idm.workingpositions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import eu.bcvsolutions.idm.core.AbstractRestTest;
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
	
	@Test
	public void getWorkingPositions() {	
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
			mvcResult = mockMvc.perform(get("/api/workingPositions/").with(security())).andReturn();
		} catch (Exception e) {
			ex = e;
		}
		
		assertNull(ex);
		assertNotNull(mvcResult);		
		assertEquals(200, mvcResult.getResponse().getStatus());
		
		logout();
	}
	
	private RequestPostProcessor security() {
		SecurityContextHolder.getContext().setAuthentication(new IdmJwtAuthentication("[SYSTEM]", null, securityService.getAllAvailableAuthorities()));
        return SecurityMockMvcRequestPostProcessors.securityContext(SecurityContextHolder.getContext());
	}
}
