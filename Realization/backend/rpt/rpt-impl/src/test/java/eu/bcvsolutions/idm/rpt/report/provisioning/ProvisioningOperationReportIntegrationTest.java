package eu.bcvsolutions.idm.rpt.report.provisioning;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.rpt.acc.TestHelper;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.dto.RptProvisioningOperationDto;
import eu.bcvsolutions.idm.rpt.report.provisioning.ProvisioningOperationReportExecutor;
import eu.bcvsolutions.idm.rpt.report.provisioning.ProvisioningOperationReportXlsxRenderer;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Provisioning report test
 * 
 * @author Radek Tomiška
 *
 */
public class ProvisioningOperationReportIntegrationTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private ProvisioningOperationReportExecutor reportExecutor;
	@Autowired private AttachmentManager attachmentManager;
	@Qualifier("objectMapper")
	@Autowired private ObjectMapper mapper;
	@Autowired private ProvisioningOperationReportXlsxRenderer xlsxRenderer;
	@Autowired private SysSystemService systemService;
	
	public TestHelper getHelper() {
		return helper;
	}
	
	@Before
	public void before() {
		getHelper().login(InitTestData.TEST_ADMIN_USERNAME, InitTestData.TEST_ADMIN_PASSWORD);
	}
	
	@After
	public void after() {
		super.logout();
	}
	
	@Test
	public void testProvisioningOperationReport() throws IOException {
		SysSystemDto systemOne = createSystemWithOperation();
		SysSystemDto systemTwo = createSystemWithOperation();
		
		// prepare report filter
		RptReportDto report = new RptReportDto(UUID.randomUUID());
		report.setExecutorName(reportExecutor.getName());
		IdmFormDto filter = new IdmFormDto();
		IdmFormDefinitionDto definition = reportExecutor.getFormDefinition();
		IdmFormValueDto systemFilter = new IdmFormValueDto(definition.getMappedAttributeByCode(ProvisioningOperationReportExecutor.PARAMETER_SYSTEM));
		systemFilter.setUuidValue(systemOne.getId());
		filter.getValues().add(systemFilter);
		filter.setFormDefinition(definition.getId());
		report.setFilter(filter);
		//
		// generate report
		report = reportExecutor.generate(report);
		Assert.assertNotNull(report.getData());
		List<RptProvisioningOperationDto> reportItems = mapper.readValue(
				attachmentManager.getAttachmentData(report.getData()), 
				new TypeReference<List<RptProvisioningOperationDto>>(){});
		//
		// test
		Assert.assertTrue(reportItems.stream().anyMatch(ri -> ri.getSystem().equals(systemOne.getName())));
		Assert.assertFalse(reportItems.stream().anyMatch(ri -> ri.getSystem().equals(systemTwo.getName())));
		//
		// test renderer
		Assert.assertNotNull(xlsxRenderer.render(report));
		//
		attachmentManager.deleteAttachments(report);
	}
	
	private SysSystemDto createSystemWithOperation() {
		SysSystemDto systemOne = getHelper().createTestResourceSystem(true);
		systemOne.setReadonly(true);
		systemOne = systemService.save(systemOne);
		//
		// create provisioning operation
		IdmRoleDto role = getHelper().createRole();
		getHelper().createRoleSystem(role, systemOne);
		getHelper().createIdentityRole(getHelper().createIdentity(), role);
		//
		return systemOne;
	}
}
