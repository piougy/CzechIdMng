package eu.bcvsolutions.idm.core.rest.impl;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.EntityEventProcessorDto;
import eu.bcvsolutions.idm.core.api.dto.filter.EntityEventProcessorFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.eav.processor.FormInstanceValidateProcessor;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * EntityEventProcessorController tests.
 * - enable / disable
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class EntityEventProcessorControllerRestTest extends AbstractRestTest {
	
	@Autowired private EntityEventManager manager;
	@Autowired private FormInstanceValidateProcessor processor;
	
	@Test
	public void testEnable() throws Exception {
		try {
			manager.enable(processor.getId());
			//
			EntityEventProcessorFilter filter = new EntityEventProcessorFilter();
			filter.setName(FormInstanceValidateProcessor.PROCESSOR_NAME);
			List<EntityEventProcessorDto> results = find(filter);
			//
			Assert.assertEquals(1, results.size());
			Assert.assertEquals(FormInstanceValidateProcessor.PROCESSOR_NAME, results.get(0).getName());
			Assert.assertFalse(results.get(0).isDisabled());
			//
			getMockMvc().perform(put(BaseController.BASE_PATH + "/entity-event-processors/" + results.get(0).getId() + "/disable")
					.with(authentication(getAdminAuthentication()))
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().is2xxSuccessful());
			//
			results = find(filter);
			Assert.assertEquals(1, results.size());
			Assert.assertEquals(FormInstanceValidateProcessor.PROCESSOR_NAME, results.get(0).getName());
			Assert.assertTrue(results.get(0).isDisabled());
			//
			// enable again
			getMockMvc().perform(put(BaseController.BASE_PATH + "/entity-event-processors/" + results.get(0).getId() + "/enable")
					.with(authentication(getAdminAuthentication()))
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().is2xxSuccessful());
			//
			results = find(filter);
			Assert.assertEquals(1, results.size());
			Assert.assertEquals(FormInstanceValidateProcessor.PROCESSOR_NAME, results.get(0).getName());
			Assert.assertFalse(results.get(0).isDisabled());
		} finally {
			manager.enable(processor.getId());
		}
	}
	
	@Test
	public void testNotFound() {
		EntityEventProcessorFilter filter = new EntityEventProcessorFilter();
		filter.setName(getHelper().createName());
		List<EntityEventProcessorDto> results = find(filter);
		//
		Assert.assertTrue(results.isEmpty());
	}

	protected List<EntityEventProcessorDto> find(EntityEventProcessorFilter filter) {
		try {
			String response = getMockMvc().perform(get(BaseController.BASE_PATH + "/entity-event-processors")
	        		.with(authentication(getAdminAuthentication()))
	        		.params(toQueryParams(filter))
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isOk())
	                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
	                .andReturn()
	                .getResponse()
	                .getContentAsString();
			//
			return toDtos(response, EntityEventProcessorDto.class);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to find entities", ex);
		}
	}
}
