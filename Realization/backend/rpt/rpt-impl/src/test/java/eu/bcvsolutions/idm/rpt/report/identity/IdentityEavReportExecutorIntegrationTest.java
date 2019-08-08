package eu.bcvsolutions.idm.rpt.report.identity;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
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
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.LoginService;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.dto.RptIdentityWithFormValueDto;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Report test
 * 
 * @author Marek Klement
 * @author Radek Tomiška
 */
@Transactional
public class IdentityEavReportExecutorIntegrationTest extends AbstractIntegrationTest {

	@Autowired private IdentityEavReportExecutor reportExecutor;
	@Autowired private IdentityEavReportXlsxRenderer xlsxRenderer;
	@Autowired private IdmIdentityService identityService;
	@Autowired private AttachmentManager attachmentManager;
	@Autowired private LoginService loginService;
	@Autowired private FormService formService;
	@Qualifier("objectMapper")
	@Autowired
	private ObjectMapper mapper;

	@Before
	public void before() {
		// report checks authorization policies - we need to log in
		loginService.login(new LoginDto(InitTestData.TEST_ADMIN_USERNAME,
				new GuardedString(InitTestData.TEST_ADMIN_PASSWORD)));
	}

	@After
	public void after() {
		super.logout();
	}

	@Test
	public void testReportWithoutValueSpecified() throws IOException {
		// prepare test identities
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityDisabled = getHelper().createIdentity((GuardedString) null);
		identityService.disable(identityDisabled.getId());
		//
		// prepare report filter
		RptReportDto report = new RptReportDto(UUID.randomUUID());
		report.setExecutorName(reportExecutor.getName());
		IdmFormDto filter = new IdmFormDto();
		IdmFormDefinitionDto definition = reportExecutor.getFormDefinition();
		//
		IdmFormValueDto disabled =
				new IdmFormValueDto(definition.getMappedAttributeByCode(IdmIdentityFilter.PARAMETER_DISABLED));
		disabled.setValue(false);
		//
		IdmFormValueDto formDefinitionAttribute =
				new IdmFormValueDto(definition.getMappedAttributeByCode(IdentityEavReportExecutor.PARAMETER_FORM_DEFINITION));
		IdmFormDefinitionDto definitionAttribute = formService.getDefinition(IdmIdentityDto.class);
		formDefinitionAttribute.setValue(definitionAttribute.getId());
		//
		IdmFormValueDto eavName =
				new IdmFormValueDto(definition.getMappedAttributeByCode(IdentityEavReportExecutor.PARAMETER_FORM_ATTRIBUTE));
		String code = getHelper().createName();
		createFormAttribute(code, definitionAttribute.getId());
		eavName.setValue(code);
		// add all attributes
		filter.getValues().add(disabled);
		filter.getValues().add(formDefinitionAttribute);
		filter.getValues().add(eavName);
		filter.setFormDefinition(definition.getId());
		report.setFilter(filter);
		// set eav to identity
		List<String> values = new ArrayList<>();
		String testValue = getHelper().createName();
		values.add(testValue);
		//
		formService.saveValues(
				identityOne,
				definitionAttribute,
				code,
				Lists.newArrayList(values));
		// generate report
		report = reportExecutor.generate(report);
		Assert.assertNotNull(report.getData());
		
		List<RptIdentityWithFormValueDto> identities = mapper.readValue(
				attachmentManager.getAttachmentData(report.getData()), 
				new TypeReference<List<RptIdentityWithFormValueDto>>(){});
		//
		// test
		assertEquals(1, identities.size());
		assertEquals(testValue, identities.get(0).getFormValues().get(0));
		// rest renderer
		Assert.assertNotNull(xlsxRenderer.render(report));
		//
		attachmentManager.deleteAttachments(report);
	}
	
	@Test
	public void testReportWithValueSpecified() throws IOException {
		// prepare test identities
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityDisabled = getHelper().createIdentity((GuardedString) null);
		identityService.disable(identityDisabled.getId());
		//
		// prepare report filter
		RptReportDto report = new RptReportDto(UUID.randomUUID());
		report.setExecutorName(reportExecutor.getName());
		IdmFormDto filter = new IdmFormDto();
		IdmFormDefinitionDto reportForm = reportExecutor.getFormDefinition();
		//
		IdmFormValueDto parameterDefinition =
				new IdmFormValueDto(reportForm.getMappedAttributeByCode(IdentityEavReportExecutor.PARAMETER_FORM_DEFINITION));
		IdmFormDefinitionDto formDefinition = formService.getDefinition(IdmIdentityDto.class);
		parameterDefinition.setValue(formDefinition.getId());
		//
		String code = getHelper().createName();
		createFormAttribute(code, formDefinition.getId());
		IdmFormValueDto parameterAttribute =
				new IdmFormValueDto(reportForm.getMappedAttributeByCode(IdentityEavReportExecutor.PARAMETER_FORM_ATTRIBUTE));
		parameterAttribute.setValue(code);
		//
		String testValue = getHelper().createName();
		IdmFormValueDto parameterValue =
				new IdmFormValueDto(reportForm.getMappedAttributeByCode(IdentityEavReportExecutor.PARAMETER_EAV_VALUE));
		parameterValue.setValue(testValue);
		// add attributes
		filter.getValues().add(parameterDefinition);
		filter.getValues().add(parameterAttribute);
		filter.getValues().add(parameterValue);
		filter.setFormDefinition(reportForm.getId());
		report.setFilter(filter);
		//
		formService.saveValues(
				identityOne,
				formDefinition,
				code,
				Lists.newArrayList(testValue));
		// generate report
		report = reportExecutor.generate(report);
		Assert.assertNotNull(report.getData());
		
		List<RptIdentityWithFormValueDto> identities = mapper.readValue(
				attachmentManager.getAttachmentData(report.getData()), 
				new TypeReference<List<RptIdentityWithFormValueDto>>(){});
		//
		// test
		assertEquals(1, identities.size());
		assertEquals(testValue, identities.get(0).getFormValues().get(0));
		// rest renderer
		Assert.assertNotNull(xlsxRenderer.render(report));
		//
		attachmentManager.deleteAttachments(report);
	}
	
	@Test(expected = ResultCodeException.class)
	public void testWrongAttribute() throws IOException {
		// prepare report filter
		RptReportDto report = new RptReportDto(UUID.randomUUID());
		report.setExecutorName(reportExecutor.getName());
		IdmFormDto filter = new IdmFormDto();
		IdmFormDefinitionDto definition = reportExecutor.getFormDefinition();
		//
		IdmFormValueDto formDefinitionAttribute =
				new IdmFormValueDto(definition.getMappedAttributeByCode(IdentityEavReportExecutor.PARAMETER_FORM_DEFINITION));
		formDefinitionAttribute.setValue(formService.getDefinition(IdmIdentityDto.class).getId());
		//
		IdmFormValueDto eavCode =
				new IdmFormValueDto(definition.getMappedAttributeByCode(IdentityEavReportExecutor.PARAMETER_FORM_ATTRIBUTE));
		eavCode.setValue("wrong");
		// add all attributes
		filter.getValues().add(formDefinitionAttribute);
		filter.getValues().add(eavCode);
		filter.setFormDefinition(definition.getId());
		report.setFilter(filter);
		// generate report
		reportExecutor.generate(report);
	}
	
	@Test(expected = ResultCodeException.class)
	public void testEmptyAttribute() throws IOException {
		// prepare report filter
		RptReportDto report = new RptReportDto(UUID.randomUUID());
		report.setExecutorName(reportExecutor.getName());
		IdmFormDto filter = new IdmFormDto();
		IdmFormDefinitionDto definition = reportExecutor.getFormDefinition();
		//
		IdmFormValueDto formDefinitionAttribute =
				new IdmFormValueDto(definition.getMappedAttributeByCode(IdentityEavReportExecutor.PARAMETER_FORM_DEFINITION));
		formDefinitionAttribute.setValue(formService.getDefinition(IdmIdentityDto.class).getId());
		//
		// add all attributes
		filter.getValues().add(formDefinitionAttribute);
		filter.setFormDefinition(definition.getId());
		report.setFilter(filter);
		// generate report
		reportExecutor.generate(report);
	}
	
	@Test(expected = ResultCodeException.class)
	public void testWrongDefinition() throws IOException {
		// prepare report filter
		RptReportDto report = new RptReportDto(UUID.randomUUID());
		report.setExecutorName(reportExecutor.getName());
		IdmFormDto filter = new IdmFormDto();
		IdmFormDefinitionDto definition = reportExecutor.getFormDefinition();
		//
		IdmFormValueDto formDefinitionAttribute =
				new IdmFormValueDto(definition.getMappedAttributeByCode(IdentityEavReportExecutor.PARAMETER_FORM_DEFINITION));
		IdmFormDefinitionDto definitionAttribute = formService.getDefinition(IdmRoleDto.class);
		formDefinitionAttribute.setValue(definitionAttribute.getId());
		//
		// add all attributes
		filter.getValues().add(formDefinitionAttribute);
		filter.setFormDefinition(definition.getId());
		report.setFilter(filter);
		// generate report
		reportExecutor.generate(report);
	}

	private void createFormAttribute(String code, UUID formDefinition) {
		IdmFormAttributeDto formAttributeDto = new IdmFormAttributeDto();
		formAttributeDto.setCode(code);
		formAttributeDto.setName(code);
		formAttributeDto.setFormDefinition(formDefinition);
		formAttributeDto.setPersistentType(PersistentType.SHORTTEXT);
		formAttributeDto.setMultiple(true);
		formService.saveAttribute(formAttributeDto);
	}

}