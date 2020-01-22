package eu.bcvsolutions.idm.core.rest.impl;

import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.dto.IdmCacheDto;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.TestHelper;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Filter builder tests.
 * - enable filter
 * - fully find is tested in DefaultFilterManagerUnitTest.
 * 
 * @author Radek Tomiška
 *
 */
public class CacheControllerRestTest extends AbstractRestTest{

	@Test
	public void testFindAll() {
		List<IdmCacheDto> results = find();


		Assert.assertTrue(results.isEmpty());
	}
	
	protected List<IdmCacheDto> find() {
		try {
			String response = getMockMvc().perform(get(BaseController.BASE_PATH + "/cache")
	        		.with(authentication(getAdminAuthentication()))
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isOk())
	                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
	                .andReturn()
	                .getResponse()
	                .getContentAsString();
			//
			return toDtos(response, IdmCacheDto.class);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to find entities", ex);
		}
	}
}
