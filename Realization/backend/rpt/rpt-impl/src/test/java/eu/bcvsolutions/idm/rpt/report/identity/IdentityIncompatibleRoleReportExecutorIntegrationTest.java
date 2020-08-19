package eu.bcvsolutions.idm.rpt.report.identity;

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

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.model.event.processor.module.InitTestDataProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.LoginService;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.dto.RptIdentityIncompatibleRoleDto;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Identity report tests
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class IdentityIncompatibleRoleReportExecutorIntegrationTest extends AbstractIntegrationTest {

	@Autowired private IdentityIncompatibleRoleReportExecutor reportExecutor;
	@Autowired private AttachmentManager attachmentManager;
	@Qualifier("objectMapper")
	@Autowired private ObjectMapper mapper;
	@Autowired private LoginService loginService;
	@Autowired private IdentityIncompatibleRoleReportXlsxRenderer xlsxRenderer;
	
	@Before
	public void before() {
		// report checks authorization policies - we need to log in
		loginService.login(new LoginDto(InitTestDataProcessor.TEST_ADMIN_USERNAME, new GuardedString(InitTestDataProcessor.TEST_ADMIN_PASSWORD)));
	}
	
	@After
	public void after() {
		super.logout();
	}
	
	@Test
	public void testReport() throws IOException {
		// prepare test identities and incompatible roles
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityTwo =getHelper().createIdentity((GuardedString) null);
		IdmRoleDto roleOne = getHelper().createRole();
		IdmRoleDto roleTwo = getHelper().createRole();
		IdmRoleDto roleThree = getHelper().createRole();
		IdmRoleDto roleFour = getHelper().createRole();
		getHelper().createIncompatibleRole(roleOne, roleTwo);
		getHelper().createIncompatibleRole(roleFour, roleThree);
		getHelper().createIdentityRole(identityOne, roleOne);
		getHelper().createIdentityRole(identityOne, roleTwo);
		getHelper().createIdentityRole(identityOne, roleThree);
		getHelper().createIdentityRole(identityTwo, roleOne);
		//
		// prepare report filter
		RptReportDto report = new RptReportDto(UUID.randomUUID());
		report.setExecutorName(reportExecutor.getName());
		//
		// generate report
		report = reportExecutor.generate(report);
		Assert.assertNotNull(report.getData());
		List<RptIdentityIncompatibleRoleDto> identityIncompatibleRoles = mapper.readValue(
				attachmentManager.getAttachmentData(report.getData()), 
				new TypeReference<List<RptIdentityIncompatibleRoleDto>>(){});
		//
		// test
		Assert.assertTrue(identityIncompatibleRoles.stream().anyMatch(i -> i.getIdentity().getId().equals(identityOne.getId()) 
				&& i.getSuperior().getId().equals(roleOne.getId())
				&& i.getSub().getId().equals(roleTwo.getId())));
		Assert.assertFalse(identityIncompatibleRoles.stream().anyMatch(i -> i.getIdentity().getId().equals(identityTwo.getId())));
		//
		attachmentManager.deleteAttachments(report);
	}
	
	@Test
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
