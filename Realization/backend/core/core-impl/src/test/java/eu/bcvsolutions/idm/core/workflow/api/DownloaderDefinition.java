package eu.bcvsolutions.idm.core.workflow.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import org.activiti.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.workflow.api.service.WorkflowDeploymentService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessDefinitionService;
import eu.bcvsolutions.idm.core.workflow.service.impl.DefaultWorkflowProcessDefinitionService;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;


public class DownloaderDefinition extends AbstractRestTest {

	@Autowired
	public DefaultWorkflowProcessDefinitionService workflowProcessDefinitionService;
	@Autowired
	public WorkflowDeploymentService deploymentService;
	@Autowired
	public WorkflowProcessDefinitionService definitionService;


	private static final String WORK_DEFINITION_ID = "testDeployAndRun";

	@Test
	@Deployment(resources = {"eu/bcvsolutions/idm/workflow/deploy/testDeployAndRun.bpmn20.xml"})
	public void testDownloadFile() throws Exception {
		MvcResult result = getMockMvc()
				.perform(MockMvcRequestBuilders
						.get(String.format("%s/%s/definition", BaseDtoController.BASE_PATH + "/workflow-definitions", WORK_DEFINITION_ID))
						.contentType(MediaType.APPLICATION_OCTET_STREAM)
						.with(authentication(getAdminAuthentication()))
				)
				.andExpect(MockMvcResultMatchers.status().is(200))
				.andReturn();
		Assert.assertEquals(200, result.getResponse().getStatus());
		Assert.assertEquals(MediaType.APPLICATION_XML.toString(), result.getResponse().getContentType());

		Assert.assertEquals((Integer) 1, definitionService.getByName(WORK_DEFINITION_ID).getVersion());

		result = getMockMvc()
				.perform(MockMvcRequestBuilders
						.fileUpload(BaseDtoController.BASE_PATH + "/workflow-definitions")
						.file(
								"data",
								result.getResponse().getContentAsByteArray()
						)
						.param("fileName", "testDeployAndRun.bpmn20.xml")
						.with(authentication(getAdminAuthentication()))
				)
				.andExpect(MockMvcResultMatchers.status().is(200))
				.andReturn();
		Assert.assertEquals(200, result.getResponse().getStatus());

		Assert.assertEquals((Integer) 2, definitionService.getByName(WORK_DEFINITION_ID).getVersion());

	}
}
