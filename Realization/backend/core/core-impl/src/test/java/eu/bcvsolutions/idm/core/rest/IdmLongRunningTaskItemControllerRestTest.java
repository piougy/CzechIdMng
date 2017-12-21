package eu.bcvsolutions.idm.core.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmProcessedTaskItemService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.utils.IdmAuthorityUtils;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Tests for IdmLongRunningTaskItemController
 *
 * @author Marek Klement
 */
public class IdmLongRunningTaskItemControllerRestTest extends AbstractRestTest {
	@Autowired
	private IdmProcessedTaskItemService service;

	@Autowired
	private IdmIdentityService identityService;

	@Autowired
	private TestHelper helper;

	private String PATH = "/long-running-task-items";

	private Authentication getAuthentication() {
		return new IdmJwtAuthentication(identityService.getByUsername(InitTestData.TEST_ADMIN_USERNAME), null, Lists.newArrayList(IdmAuthorityUtils.getAdminAuthority()), "test");
	}

	@After
	public void logout() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void itemNotFound() throws Exception {
		getMockMvc().perform(get(BaseController.BASE_PATH + PATH + "/n_a_item_found")
				.with(authentication(getAuthentication()))
				.contentType(InitTestData.HAL_CONTENT_TYPE))
				.andExpect(status().isNotFound());
	}

	@Test
	public void itemFoundById() throws Exception {
		IdmScheduledTaskDto scheduledTaskDto = helper.createSchedulableTask();
		IdmProcessedTaskItemDto taskItem = service.saveInternal(helper.prepareProcessedItem(scheduledTaskDto));
		getMockMvc().perform(get(BaseController.BASE_PATH + PATH + "/" + taskItem.getId())
				.with(authentication(getAuthentication()))
				.contentType(InitTestData.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(InitTestData.HAL_CONTENT_TYPE))
				.andExpect(jsonPath("$.id", equalTo(taskItem.getId().toString())));
	}

	@Test
	public void getItem() throws Exception{
		SecurityMockMvcRequestPostProcessors.securityContext(null);
		int status = getMockMvc().perform(get(BaseDtoController.BASE_PATH + PATH))
				.andReturn()
				.getResponse()
				.getStatus();

		assertEquals(403, status);

		MvcResult mvcResult = getMockMvc().perform(get(BaseDtoController.BASE_PATH + PATH).with(authentication(getAuthentication())))
				.andReturn();

		assertNotNull(mvcResult);
		assertEquals(200, mvcResult.getResponse().getStatus());
	}

	@Test
	public void deleteItem() throws Exception {
		IdmScheduledTaskDto scheduledTaskDto = helper.createSchedulableTask();
		IdmProcessedTaskItemDto taskItem = service.saveInternal(helper.prepareProcessedItem(scheduledTaskDto));
		getMockMvc().perform(get(BaseController.BASE_PATH + PATH + "/" + taskItem.getId())
				.with(authentication(getAuthentication()))
				.contentType(InitTestData.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(InitTestData.HAL_CONTENT_TYPE))
				.andExpect(jsonPath("$.id", equalTo(taskItem.getId().toString())));

		int status = getMockMvc().perform(delete(BaseController.BASE_PATH + PATH + "/" + taskItem.getId()).contentType(MediaType.APPLICATION_JSON))
				.andReturn()
				.getResponse()
				.getStatus();

		assertEquals(403, status);

		getMockMvc().perform(get(BaseController.BASE_PATH + PATH + taskItem.getId())
				.with(authentication(getAuthentication()))
				.contentType(InitTestData.HAL_CONTENT_TYPE))
				.andExpect(status().isNotFound());
	}

}
