package eu.bcvsolutions.idm.rpt.executor;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.LoginService;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.renderer.IdentityReportXlsxRenderer;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Identity report tests
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityReportExecutorIntegrationTest extends AbstractIntegrationTest {

	@Autowired private IdentityReportExecutor reportExecutor;
	@Autowired private IdmIdentityService identityService;
	@Autowired private AttachmentManager attachmentManager;
	@Qualifier("objectMapper")
	@Autowired private ObjectMapper mapper;
	@Autowired private LoginService loginService;
	@Autowired private IdentityReportXlsxRenderer xlsxRenderer;
	
	@Before
	public void before() {
		// report checks authorization policies - we need to log in
		loginService.login(new LoginDto(InitTestData.TEST_ADMIN_USERNAME, new GuardedString(InitTestData.TEST_ADMIN_PASSWORD)));
	}
	
	@After
	public void after() {
		super.logout();
	}
	
	@Test
	@Transactional
	public void testDisabledIdentity() throws IOException {
		// prepare test identities
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityDisabled =getHelper().createIdentity((GuardedString) null);
		identityService.disable(identityDisabled.getId());
		//
		// prepare report filter
		RptReportDto report = new RptReportDto(UUID.randomUUID());
		report.setExecutorName(reportExecutor.getName());
		IdmFormDto filter = new IdmFormDto();
		IdmFormDefinitionDto definition = reportExecutor.getFormDefinition();
		IdmFormValueDto disabled = new IdmFormValueDto(definition.getMappedAttributeByCode(IdmIdentityFilter.PARAMETER_DISABLED));
		disabled.setValue(false);
		filter.getValues().add(disabled);
		filter.setFormDefinition(definition.getId());
		report.setFilter(filter);
		//
		// generate report
		report = reportExecutor.generate(report);
		Assert.assertNotNull(report.getData());
		List<IdmIdentityDto> identityRoles = mapper.readValue(
				attachmentManager.getAttachmentData(report.getData()), 
				new TypeReference<List<IdmIdentityDto>>(){});
		//
		// test
		Assert.assertTrue(identityRoles.stream().anyMatch(i -> i.equals(identityOne)));
		Assert.assertFalse(identityRoles.stream().anyMatch(i -> i.equals(identityDisabled)));
		//
		attachmentManager.deleteAttachments(report);
	}
	
	@Test
	@Transactional
	public void testRenderers() {
		getHelper().createIdentity((GuardedString) null);
		//
		// prepare report filter
		RptReportDto report = new RptReportDto(UUID.randomUUID());
		report.setExecutorName(reportExecutor.getName());
		//
		// generate report
		report = reportExecutor.generate(report);
		//
		Assert.assertNotNull(xlsxRenderer.render(report));
	}
}
