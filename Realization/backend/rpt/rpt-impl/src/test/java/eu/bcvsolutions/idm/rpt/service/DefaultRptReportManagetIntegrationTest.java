package eu.bcvsolutions.idm.rpt.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.rpt.api.dto.RptRenderedReportDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportExecutorDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportRendererDto;
import eu.bcvsolutions.idm.rpt.api.service.RptReportService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

public class DefaultRptReportManagetIntegrationTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private ApplicationContext context;
	@Autowired private RptReportService reportService;
	@Qualifier("objectMapper")
	@Autowired private ObjectMapper mapper;
	@Autowired private AttachmentManager attachmentManager; 
	//
	private DefaultReportManager manager;
	
	@Before
	public void init() {
		manager = context.getAutowireCapableBeanFactory().createBean(DefaultReportManager.class);
	}
	
	@Test
	public void testSupportedReports() {
		List<RptReportExecutorDto> reports = manager.getExecutors();
		//
		Assert.assertTrue(reports.size() > 0);
		Assert.assertTrue(reports.stream().anyMatch(e -> e.getName().equals(TestReportExecutor.REPORT_NAME)));
	}
	
	@Test
	public void testSupportedRenderers() {
		List<RptReportRendererDto> renderers = manager.getRenderers(TestReportExecutor.REPORT_NAME);
		//
		Assert.assertTrue(renderers.size() > 0);
		Assert.assertTrue(renderers.stream().anyMatch(e -> e.getName().equals(TestReportRenderer.RENDERER_NAME)));
	}
	
	@Test
	@Transactional
	public void testGenerateReportByExecutor() throws IOException {
		TestReportExecutor testReportExecutor = context.getAutowireCapableBeanFactory().createBean(TestReportExecutor.class);
		
		RptReportDto report = testReportExecutor.generate(new RptReportDto(UUID.randomUUID()));
		
		Assert.assertNotNull(report.getData());
		Assert.assertEquals(mapper.writeValueAsString(TestReportExecutor.identities), IOUtils.toString(attachmentManager.getAttachmentData(report.getData())));
		//
		attachmentManager.deleteAttachments(report);
	}
	
	@Test
	public void testGenerateReportByManager() throws IOException {
		RptReportDto report = new RptReportDto();
		report.setExecutorName(TestReportExecutor.REPORT_NAME);
		report = manager.generate(report);
		final UUID reportId = report.getId();
		Assert.assertNotNull(reportId);
		
		helper.waitForResult(res -> {
			return OperationState.isRunnable(reportService.get(reportId).getResult().getState());
		});
		
		Assert.assertNotNull(report.getData());
		Assert.assertEquals(mapper.writeValueAsString(TestReportExecutor.identities), IOUtils.toString(attachmentManager.getAttachmentData(report.getData())));
		
		attachmentManager.deleteAttachments(report);
	}
	
	@Test
	public void testGenerateReportWithFilter() throws IOException {
		RptReportDto report = new RptReportDto();
		report.setExecutorName(TestFilterReportExecutor.REPORT_NAME);
		IdmFormDto filter = new IdmFormDto();
		TestFilterReportExecutor testReportExecutor = context.getAutowireCapableBeanFactory().createBean(TestFilterReportExecutor.class);
		IdmFormDefinitionDto definition = testReportExecutor.getFormDefinition();
		IdmFormValueDto username = new IdmFormValueDto(definition.getMappedAttributeByCode(IdmIdentity_.username.getName()));
		username.setValue(TestReportExecutor.identities.get(0).getUsername());
		filter.getValues().add(username);
		filter.setFormDefinition(definition.getId());
		report.setFilter(filter);
		//
		report = manager.generate(report);
		final UUID reportId = report.getId();
		Assert.assertNotNull(reportId);
		
		helper.waitForResult(res -> {
			return OperationState.isRunnable(reportService.get(reportId).getResult().getState());
		});
		
		Assert.assertNotNull(report.getData());
		Assert.assertEquals(
				mapper.writeValueAsString(Lists.newArrayList(TestReportExecutor.identities.get(0))), 
				IOUtils.toString(attachmentManager.getAttachmentData(report.getData())));
		
		attachmentManager.deleteAttachments(report);
	}
	
	@Test
	public void testRenderReportAsJson() throws IOException {
		TestReportExecutor testReportExecutor = context.getAutowireCapableBeanFactory().createBean(TestReportExecutor.class);
		
		RptReportDto report = testReportExecutor.generate(new RptReportDto(UUID.randomUUID()));
		
		RptRenderedReportDto renderedData = manager.render(report, TestReportRenderer.RENDERER_NAME);
		//
		Assert.assertEquals(mapper.writeValueAsString(TestReportExecutor.identities), IOUtils.toString(renderedData.getRenderedReport()));
	}
}
