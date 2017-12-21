package eu.bcvsolutions.idm.rpt.service;

import java.io.IOException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.CommonFormService;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.service.ReportManager;
import eu.bcvsolutions.idm.rpt.api.service.RptReportService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Reports crud tests
 * - delete form
 * - delete attachmant
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultRptReportServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private ApplicationContext context;
	@Autowired private CommonFormService commonFormService;
	@Autowired private AttachmentManager attachmentManager;
	@Autowired private ReportManager reportManager;
	//
	private RptReportService reportService;
	
	@Before
	public void init() {
		reportService = context.getAutowireCapableBeanFactory().createBean(DefaultRptReportService.class);
	}
	
	@Test
	public void testReportReferentialIntegrity() throws IOException {
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
		report = reportManager.generate(report);
		final UUID reportId = report.getId();
		Assert.assertNotNull(reportId);
		
		helper.waitForResult(res -> {
			return OperationState.isRunnable(reportService.get(reportId).getResult().getState());
		});
		
		Assert.assertNotNull(report.getData());
		Assert.assertNotNull(report.getFilter());
		Assert.assertFalse(commonFormService.getForms(report).isEmpty());
		Assert.assertFalse(attachmentManager.getAttachments(report, null).getTotalElements() == 0);
		//
		// delete report
		reportService.delete(report);
		// check report is deleted
		Assert.assertNull(reportService.get(report.getId()));
		// check attachment was deleted
		Assert.assertTrue(attachmentManager.getAttachments(report, null).getTotalElements() == 0);
		// check filter is deleted
		Assert.assertTrue(commonFormService.getForms(report).isEmpty());
	}	
}
