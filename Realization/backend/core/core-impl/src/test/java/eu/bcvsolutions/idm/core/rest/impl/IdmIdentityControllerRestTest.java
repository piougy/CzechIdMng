package eu.bcvsolutions.idm.core.rest.impl;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmProfileDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.ResolvedIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmProfileService;
import eu.bcvsolutions.idm.core.bulk.action.impl.IdentityDisableBulkAction;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormProjectionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormProjectionService;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityFormValue;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.evaluator.eav.IdentityFormValueEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.identity.SelfIdentityEvaluator;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Identity controller tests
 * - CRUD
 * - bulk actions
 * - eav attributes with authorization policies
 * - profile CRUD
 * 
 * - TODO: move all filters here
 * 
 * @author Radek Tomiška
 *
 */
public class IdmIdentityControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmIdentityDto> {

	@Autowired private IdmIdentityController controller;
	@Autowired private FormService formService;
	@Autowired private IdmIdentityService identityService;
	@Autowired private AttachmentManager attachmentManager;
	@Autowired private IdmProfileService profileService;
	@Autowired private IdmFormProjectionService formProjectionService;
	@Autowired private IdmIdentityContractService contractService;
	@Autowired private ConfigurationService configurationService;
	
	@Override
	protected AbstractReadWriteDtoController<IdmIdentityDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmIdentityDto prepareDto() {
		IdmIdentityDto dto = new IdmIdentityDto();
		dto.setUsername(getHelper().createName());
		return dto;
	}
	
	@Test
    public void userNotFound() throws Exception {
		getMockMvc().perform(get(getDetailUrl("n_a_user"))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(status().isNotFound());
    }
	
	@Test
    public void userFoundByUsername() throws Exception {
		getMockMvc().perform(get(getDetailUrl(TestHelper.ADMIN_USERNAME))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(jsonPath("$.username", equalTo(TestHelper.ADMIN_USERNAME)))
                .andExpect(jsonPath("$._links.profile", Matchers.notNullValue()));
    }
	
	@Test
    public void testUsernameWithDotCharacter() throws Exception {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername(getHelper().createName() + ".com");
		identity.setFirstName("test");
		identity.setLastName("test");
		identity = getHelper().getService(IdmIdentityService.class).save(identity);
		//
		getMockMvc().perform(get(getDetailUrl(URLEncoder.encode(identity.getUsername(), "UTF-8")))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(jsonPath("$.username", equalTo(identity.getUsername())));
    }
	
	@Test
	@Ignore // TODO: surefire parameters are ignored ... why?
    public void testUsernameWithSlashCharacter() throws Exception {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername(getHelper().createName() + "/com");
		identity.setFirstName("test");
		identity.setLastName("test");
		identity = getHelper().getService(IdmIdentityService.class).save(identity);
		//
		getMockMvc().perform(get(getDetailUrl(URLEncoder.encode(identity.getUsername(), "UTF-8")))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(jsonPath("$.username", equalTo(identity.getUsername())));
    }
	
	@Test
	public void testDisableEnable() throws Exception {
		IdmIdentityDto dto = createDto();
		Assert.assertFalse(dto.isDisabled());
		//
		getMockMvc().perform(patch(String.format("%s/disable", getDetailUrl(dto.getId())))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(jsonPath("$.disabled", equalTo(true)));
		Assert.assertTrue(getDto(dto.getId()).isDisabled());	
		//
		getMockMvc().perform(patch(String.format("%s/enable", getDetailUrl(dto.getId())))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(jsonPath("$.disabled", equalTo(false)));
		//
		Assert.assertFalse(getDto(dto.getId()).isDisabled());
	}
	
	@Test
	public void testBulkDisable() {
		IdmIdentityDto identity = createDto();
		Assert.assertFalse(identity.isDisabled());
		// check disable operation is available
		List<IdmBulkActionDto> availableBulkActions = getAvailableBulkActions();
		//
		IdmBulkActionDto disableAction = availableBulkActions
				.stream()
				.filter(action -> action.getName().equals(IdentityDisableBulkAction.NAME))
				.findFirst()
				.orElse(null);
		Assert.assertNotNull(disableAction);
		//
		disableAction.getIdentifiers().add(identity.getId());
		//
		IdmBulkActionDto result = bulkAction(disableAction);
		//
		Assert.assertNotNull(result);
		Assert.assertTrue(getDto(identity.getId()).isDisabled());
	}
	
	@Test
	public void testSaveFormValuesWithAuthorizationPoliciesSupport() throws Exception {
		//
		// create definition with two attributes
		IdmFormAttributeDto formAttributeOne = new IdmFormAttributeDto("one");
		IdmFormAttributeDto formAttributeTwo = new IdmFormAttributeDto("two");
		IdmFormDefinitionDto formDefinition = formService.createDefinition(
				prepareDto().getClass(), 
				getHelper().createName(), 
				Lists.newArrayList(formAttributeOne, formAttributeTwo));
		formAttributeOne = formDefinition.getMappedAttributeByCode(formAttributeOne.getCode());
		formAttributeTwo = formDefinition.getMappedAttributeByCode(formAttributeTwo.getCode());
		//
		IdmIdentityDto identityOne = getHelper().createIdentity(); // password is needed
		IdmIdentityDto identityTwo = getHelper().createIdentity(); // password is needed
		IdmIdentityDto identityOther = getHelper().createIdentity((GuardedString) null);
		//
		// assign self identity authorization policy - READ - to identityOne
		IdmRoleDto roleReadIdentity = getHelper().createRole();		
		getHelper().createAuthorizationPolicy(
				roleReadIdentity.getId(), 
				CoreGroupPermission.IDENTITY, 
				IdmIdentity.class, 
				SelfIdentityEvaluator.class, 
				IdmBasePermission.AUTOCOMPLETE, IdmBasePermission.READ);
		getHelper().createUuidPolicy( // and other
				roleReadIdentity.getId(), 
				identityOther.getId(), 
				IdmBasePermission.AUTOCOMPLETE, IdmBasePermission.READ);
		getHelper().createIdentityRole(identityOne, roleReadIdentity);
		//
		// assign self identity authorization policy - UPDATE - to identityOne
		IdmRoleDto roleUpdateIdentity = getHelper().createRole();		
		getHelper().createAuthorizationPolicy(
				roleUpdateIdentity.getId(), 
				CoreGroupPermission.IDENTITY, 
				IdmIdentity.class, 
				SelfIdentityEvaluator.class, // self
				IdmBasePermission.AUTOCOMPLETE, IdmBasePermission.READ, IdmBasePermission.UPDATE);
		getHelper().createUuidPolicy( // and other
				roleUpdateIdentity.getId(), 
				identityOther.getId(), 
				IdmBasePermission.AUTOCOMPLETE, IdmBasePermission.READ, IdmBasePermission.UPDATE);
		getHelper().createIdentityRole(identityTwo, roleUpdateIdentity);
		//
		// form definition cannot be still read - authorization policies are supported
		List<IdmFormDefinitionDto> formDefinitions = getFormDefinitions(identityOne.getId(), identityOne.getUsername());
		Assert.assertTrue(formDefinitions.isEmpty());
		formDefinitions = getFormDefinitions(identityTwo.getId(), identityTwo.getUsername());
		Assert.assertTrue(formDefinitions.isEmpty());
		//
		// assign autocomplete to form definition 
		getHelper().createUuidPolicy( 
				roleReadIdentity.getId(), 
				formDefinition.getId(), 
				IdmBasePermission.AUTOCOMPLETE);
		getHelper().createUuidPolicy( // and other
				roleUpdateIdentity.getId(), 
				formDefinition.getId(), 
				IdmBasePermission.AUTOCOMPLETE);
		//
		// form definition can be read - look out form definitions in list are trimmed
		formDefinitions = getFormDefinitions(identityOne.getId(), identityOne.getUsername());
		Assert.assertTrue(formDefinitions.stream().anyMatch(d -> d.getCode().equals(formDefinition.getCode())));
		formDefinitions = getFormDefinitions(identityTwo.getId(), identityTwo.getUsername());
		Assert.assertTrue(formDefinitions.stream().anyMatch(d -> d.getCode().equals(formDefinition.getCode())));
		//
		// save some values as admin to identity one
		IdmFormValueDto formValueOne = new IdmFormValueDto(formAttributeOne);
		formValueOne.setValue(getHelper().createName());
		IdmFormValueDto formValueTwo = new IdmFormValueDto(formAttributeTwo);
		formValueTwo.setValue(getHelper().createName());
		List<IdmFormValueDto> formValues = Lists.newArrayList(formValueOne, formValueTwo);
		saveFormValues(identityOne.getId(), TestHelper.ADMIN_USERNAME, formDefinition.getCode(), formValues);
		//
		// values cannot be read as identity one 
		IdmFormInstanceDto formInstance = getFormInstance(identityOne.getId(), identityOne.getUsername(), formDefinition.getCode());
		Assert.assertTrue(formInstance.getValues().isEmpty());
		Assert.assertEquals(0, formInstance.getFormDefinition().getFormAttributes().size());
		formInstance = getFormInstance(identityOther.getId(), identityTwo.getUsername(), formDefinition.getCode());
		Assert.assertTrue(formInstance.getValues().isEmpty());
		Assert.assertEquals(0, formInstance.getFormDefinition().getFormAttributes().size());
		//
		// configure authorization policy to read attribute one and edit attribute two - for self
		ConfigurationMap properties = new ConfigurationMap();
		properties.put(IdentityFormValueEvaluator.PARAMETER_FORM_DEFINITION, formDefinition.getId());
		properties.put(IdentityFormValueEvaluator.PARAMETER_FORM_ATTRIBUTES, formAttributeOne.getCode());
		properties.put(IdentityFormValueEvaluator.PARAMETER_SELF_ONLY, true);
		getHelper().createAuthorizationPolicy(
				roleReadIdentity.getId(), 
				CoreGroupPermission.FORMVALUE, 
				IdmIdentityFormValue.class, 
				IdentityFormValueEvaluator.class,
				properties,
				IdmBasePermission.READ);
		//
		// read self attribute one
		formInstance = getFormInstance(identityOne.getId(), identityOne.getUsername(), formDefinition.getCode());
		Assert.assertEquals(1, formInstance.getValues().size());
		Assert.assertEquals(formValueOne.getShortTextValue(), formInstance.getValues().get(0).getShortTextValue());
		Assert.assertEquals(1, formInstance.getFormDefinition().getFormAttributes().size());
		Assert.assertEquals(formAttributeOne.getCode(), formInstance.getFormDefinition().getFormAttributes().get(0).getCode());
		//
		// update is forbidden
		getMockMvc().perform(patch(getFormValuesUrl(identityOne.getId()))
        		.with(authentication(getAuthentication(identityOne.getUsername())))
        		.param(IdmFormAttributeFilter.PARAMETER_FORM_DEFINITION_CODE, formDefinition.getCode())
        		.content(getMapper().writeValueAsString(Lists.newArrayList(formValueOne)))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(status().isForbidden());
		getMockMvc().perform(patch(getFormValuesUrl(identityTwo.getId()))
        		.with(authentication(getAuthentication(identityOne.getUsername())))
        		.param(IdmFormAttributeFilter.PARAMETER_FORM_DEFINITION_CODE, formDefinition.getCode())
        		.content(getMapper().writeValueAsString(Lists.newArrayList(formValueOne)))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(status().isForbidden());
		//
		// add policy to edit attribute two for identity one
		properties = new ConfigurationMap();
		properties.put(IdentityFormValueEvaluator.PARAMETER_FORM_DEFINITION, formDefinition.getId());
		properties.put(IdentityFormValueEvaluator.PARAMETER_FORM_ATTRIBUTES, formAttributeTwo.getCode());
		properties.put(IdentityFormValueEvaluator.PARAMETER_SELF_ONLY, true);
		getHelper().createAuthorizationPolicy(
				roleReadIdentity.getId(), 
				CoreGroupPermission.FORMVALUE, 
				IdmIdentityFormValue.class, 
				IdentityFormValueEvaluator.class,
				properties,
				IdmBasePermission.READ, IdmBasePermission.UPDATE);
		//
		String updatedValue = getHelper().createName();
		formValueTwo.setValue(updatedValue);
		saveFormValues(identityOne.getId(), identityOne.getUsername(), formDefinition.getCode(), Lists.newArrayList(formValueTwo));
		formInstance = getFormInstance(identityOne.getId(), identityOne.getUsername(), formDefinition.getCode());
		Assert.assertEquals(2, formInstance.getValues().size());
		Assert.assertEquals(formValueOne.getShortTextValue(), formInstance.toSinglePersistentValue(formAttributeOne.getCode()));
		Assert.assertEquals(updatedValue, formInstance.toSinglePersistentValue(formAttributeTwo.getCode()));
		getMockMvc().perform(patch(getFormValuesUrl(identityTwo.getId()))
        		.with(authentication(getAuthentication(identityOne.getUsername())))
        		.param(IdmFormAttributeFilter.PARAMETER_FORM_DEFINITION_CODE, formDefinition.getCode())
        		.content(getMapper().writeValueAsString(Lists.newArrayList(formValueTwo)))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(status().isForbidden());
		formValueOne.setValue(updatedValue);
		getMockMvc().perform(patch(getFormValuesUrl(identityOne.getId()))
        		.with(authentication(getAuthentication(identityOne.getUsername())))
        		.param(IdmFormAttributeFilter.PARAMETER_FORM_DEFINITION_CODE, formDefinition.getCode())
        		.content(getMapper().writeValueAsString(Lists.newArrayList(formValueOne, formValueTwo)))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(status().isForbidden());
		getMockMvc().perform(patch(getFormValuesUrl(identityOther.getId()))
        		.with(authentication(getAuthentication(identityOne.getUsername())))
        		.param(IdmFormAttributeFilter.PARAMETER_FORM_DEFINITION_CODE, formDefinition.getCode())
        		.content(getMapper().writeValueAsString(Lists.newArrayList(formValueTwo)))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(status().isForbidden());
		//
		// add policy to edit attribute two for identity two
		properties = new ConfigurationMap();
		properties.put(IdentityFormValueEvaluator.PARAMETER_FORM_DEFINITION, formDefinition.getId());
		properties.put(IdentityFormValueEvaluator.PARAMETER_FORM_ATTRIBUTES, formAttributeTwo.getCode());
		properties.put(IdentityFormValueEvaluator.PARAMETER_OWNER_UPDATE, true);
		getHelper().createAuthorizationPolicy(
				roleUpdateIdentity.getId(), 
				CoreGroupPermission.FORMVALUE, 
				IdmIdentityFormValue.class, 
				IdentityFormValueEvaluator.class,
				properties,
				IdmBasePermission.READ, IdmBasePermission.UPDATE);
		//
		// create new values under identity two
		// identity two can now update values of attribute two for self and other (no for identity one)
		updatedValue = getHelper().createName();
		formValueOne.setValue(updatedValue);
		formValueTwo.setValue(updatedValue);
		getMockMvc().perform(patch(getFormValuesUrl(identityOne.getId()))
        		.with(authentication(getAuthentication(identityTwo.getUsername())))
        		.param(IdmFormAttributeFilter.PARAMETER_FORM_DEFINITION_CODE, formDefinition.getCode())
        		.content(getMapper().writeValueAsString(Lists.newArrayList(formValueTwo)))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(status().isForbidden());
		saveFormValues(identityTwo.getId(), identityTwo.getUsername(), formDefinition.getCode(), Lists.newArrayList(formValueTwo));
		formInstance = getFormInstance(identityTwo.getId(), identityTwo.getUsername(), formDefinition.getCode());
		Assert.assertEquals(updatedValue, formInstance.toSinglePersistentValue(formAttributeTwo.getCode()));
		saveFormValues(identityOther.getId(), identityTwo.getUsername(), formDefinition.getCode(), Lists.newArrayList(formValueTwo));
		formInstance = getFormInstance(identityOther.getId(), identityTwo.getUsername(), formDefinition.getCode());
		Assert.assertEquals(updatedValue, formInstance.toSinglePersistentValue(formAttributeTwo.getCode()));
		getMockMvc().perform(patch(getFormValuesUrl(identityOther.getId()))
        		.with(authentication(getAuthentication(identityTwo.getUsername())))
        		.param(IdmFormAttributeFilter.PARAMETER_FORM_DEFINITION_CODE, formDefinition.getCode())
        		.content(getMapper().writeValueAsString(Lists.newArrayList(formValueOne)))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(status().isForbidden());
		//
		// add permission to update attributes of other identity
		properties = new ConfigurationMap();
		properties.put(IdentityFormValueEvaluator.PARAMETER_FORM_DEFINITION, formDefinition.getId());
		properties.put(IdentityFormValueEvaluator.PARAMETER_FORM_ATTRIBUTES, formAttributeTwo.getCode());
		properties.put(IdentityFormValueEvaluator.PARAMETER_OWNER_READ, true);
		getHelper().createAuthorizationPolicy(
				roleReadIdentity.getId(), 
				CoreGroupPermission.FORMVALUE, 
				IdmIdentityFormValue.class, 
				IdentityFormValueEvaluator.class,
				properties,
				IdmBasePermission.READ, IdmBasePermission.UPDATE);
		//
		updatedValue = getHelper().createName();
		formValueOne.setValue(updatedValue);
		formValueTwo.setValue(updatedValue);
		saveFormValues(identityOther.getId(), identityOne.getUsername(), formDefinition.getCode(), Lists.newArrayList(formValueTwo));
		formInstance = getFormInstance(identityOther.getId(), identityTwo.getUsername(), formDefinition.getCode());
		Assert.assertEquals(updatedValue, formInstance.toSinglePersistentValue(formAttributeTwo.getCode()));
		getMockMvc().perform(patch(getFormValuesUrl(identityOther.getId()))
        		.with(authentication(getAuthentication(identityOne.getUsername())))
        		.param(IdmFormAttributeFilter.PARAMETER_FORM_DEFINITION_CODE, formDefinition.getCode())
        		.content(getMapper().writeValueAsString(Lists.newArrayList(formValueOne)))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(status().isForbidden());
	}
	
	@Test
	public void testSaveFormValueWithAttributeIdOnly() throws Exception {
		IdmFormAttributeDto formAttribute = new IdmFormAttributeDto(getHelper().createName());
		IdmFormDefinitionDto formDefinition = formService.createDefinition(prepareDto().getClass(), getHelper().createName(), Lists.newArrayList(formAttribute));
		formAttribute = formDefinition.getFormAttributes().get(0);
		//
		IdmIdentityDto owner = createDto();
		//
		String value = getHelper().createName();
		//
		// save values		
		getMockMvc().perform(patch(getFormValuesUrl(owner))
        		.with(authentication(getAuthentication(TestHelper.ADMIN_USERNAME)))
        		.param(IdmFormAttributeFilter.PARAMETER_FORM_DEFINITION_CODE, formDefinition.getCode())
        		.content("[{ \"formAttribute\": \"" + formAttribute.getId() + "\", \"shortTextValue\": \"" + value + "\" }]")
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(status().isOk());
		
		// get saved values
		IdmFormInstanceDto formInstance = getFormInstance(owner.getId(), TestHelper.ADMIN_USERNAME, formDefinition.getCode());
		Assert.assertEquals(owner.getId().toString(), formInstance.getOwnerId());
		Assert.assertEquals(1, formInstance.getValues().size());
		Assert.assertEquals(value, formInstance.getValues().get(0).getShortTextValue());
		Assert.assertEquals(formAttribute.getId(), formInstance.getValues().get(0).getFormAttribute());
	}
	
	@Test
	public void testSaveFormValueWithValueOnlyIsNotPossible() throws Exception {
		IdmFormAttributeDto formAttribute = new IdmFormAttributeDto(getHelper().createName());
		IdmFormDefinitionDto formDefinition = formService.createDefinition(prepareDto().getClass(), getHelper().createName(), Lists.newArrayList(formAttribute));
		formAttribute = formDefinition.getFormAttributes().get(0);
		//
		IdmIdentityDto owner = createDto();
		//
		String value = getHelper().createName();
		//
		// save values
		// if value is given only, then no value is saved => value is null => shortTextValue has to be given
		getMockMvc().perform(patch(getFormValuesUrl(owner))
        		.with(authentication(getAuthentication(TestHelper.ADMIN_USERNAME)))
        		.param(IdmFormAttributeFilter.PARAMETER_FORM_DEFINITION_CODE, formDefinition.getCode())
        		.content("[{ \"formAttribute\": \"" + formAttribute.getId() + "\", \"value\": \"" + value + "\" }]")
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(status().isOk());
		
		// get saved values
		IdmFormInstanceDto formInstance = getFormInstance(owner.getId(), TestHelper.ADMIN_USERNAME, formDefinition.getCode());
		Assert.assertEquals(owner.getId().toString(), formInstance.getOwnerId());
		Assert.assertEquals(0, formInstance.getValues().size());
	}
	
	@Test
	public void testProfile() throws UnsupportedEncodingException, IOException, Exception {
		IdmIdentityDto owner = getHelper().createIdentity((GuardedString) null);
		//
		// profile image
		getMockMvc().perform(MockMvcRequestBuilders.get(getDetailUrl(owner.getId()) + "/profile")
        		.with(authentication(getAdminAuthentication())))
				.andExpect(status().isNoContent());
		getMockMvc().perform(MockMvcRequestBuilders.get(getDetailUrl(owner.getId()) + "/profile/image")
        		.with(authentication(getAdminAuthentication())))
				.andExpect(status().isNoContent());
		//
		String fileName = "file.png";
		String content = "some image";
		String response = getMockMvc().perform(MockMvcRequestBuilders.multipart(getDetailUrl(owner.getId()) + "/profile/image")
				.file("data", IOUtils.toByteArray(IOUtils.toInputStream(content)))
        		.param("fileName", fileName)
        		.with(authentication(getAdminAuthentication())))
				.andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andReturn()
                .getResponse()
                .getContentAsString();
		IdmProfileDto createdProfile = (IdmProfileDto) getMapper().readValue(response, IdmProfileDto.class);
		//
		Assert.assertNotNull(createdProfile);
		Assert.assertNotNull(createdProfile.getId());
		Assert.assertNotNull(createdProfile.getImage());
		IdmAttachmentDto image = attachmentManager.get(createdProfile.getImage());
		Assert.assertEquals(content.length(), image.getFilesize().intValue());
		Assert.assertEquals(createdProfile.getId(), image.getOwnerId());
		Assert.assertEquals(attachmentManager.getOwnerType(createdProfile), image.getOwnerType());
		Assert.assertEquals(fileName, image.getName());
		InputStream is = attachmentManager.getAttachmentData(image.getId());
		try {
			Assert.assertEquals(content, IOUtils.toString(is));
		} finally {
			IOUtils.closeQuietly(is);
		}
		//
		// get profile
		response = getMockMvc().perform(MockMvcRequestBuilders.get(getDetailUrl(owner.getId()) + "/profile")
        		.with(authentication(getAdminAuthentication())))
				.andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andReturn()
                .getResponse()
                .getContentAsString();
		createdProfile = (IdmProfileDto) getMapper().readValue(response, IdmProfileDto.class);
		Assert.assertEquals(image.getId(), createdProfile.getImage());
		//
		// get profile image
		response = getMockMvc().perform(MockMvcRequestBuilders.get(getDetailUrl(owner.getId()) + "/profile/image")
        		.with(authentication(getAdminAuthentication())))
				.andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
		Assert.assertEquals(content, response);
		//
		// get profile permissions
		response = getMockMvc().perform(get(getDetailUrl(owner.getId()) + "/profile/permissions")
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
		//
		// convert embedded object to list of strings
		List<String> permissions = getMapper().readValue(response, new TypeReference<List<String>>(){});
		Assert.assertNotNull(permissions);
		Assert.assertFalse(permissions.isEmpty());
		Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.ADMIN.getName())));
		//
		// delete image		
		response = getMockMvc().perform(MockMvcRequestBuilders.delete(getDetailUrl(owner.getId()) + "/profile/image")
        		.with(authentication(getAdminAuthentication())))
				.andExpect(status().isOk())
				.andReturn()
                .getResponse()
                .getContentAsString();
		createdProfile = (IdmProfileDto) getMapper().readValue(response, IdmProfileDto.class);
		Assert.assertNull(createdProfile.getImage());
		//
		// get profile without image
		getMockMvc().perform(MockMvcRequestBuilders.get(getDetailUrl(owner.getId()) + "/profile/image")
        		.with(authentication(getAdminAuthentication())))
				.andExpect(status().isNoContent());
		//
		identityService.delete(owner);
		//
		// profile is deleted
		getMockMvc().perform(MockMvcRequestBuilders.get(getDetailUrl(owner.getId()) + "/profile/image")
        		.with(authentication(getAdminAuthentication())))
				.andExpect(status().isNoContent());
		// attachment is deleted
		Assert.assertNull(attachmentManager.get(image));
		Assert.assertNull(profileService.get(createdProfile));
	}
	
	@Test
	public void testPatchProfile() throws UnsupportedEncodingException, IOException, Exception {
		IdmIdentityDto owner = getHelper().createIdentity((GuardedString) null);
		//
		// create + patch profile
		String response = getMockMvc().perform(MockMvcRequestBuilders.patch(getDetailUrl(owner.getId()) + "/profile")
        		.with(authentication(getAdminAuthentication()))
				.content("{ \"preferredLanguage\": \"en\" }") // PATCH => lookout, mappar cannot be used (maps even null attrs).
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andReturn()
                .getResponse()
                .getContentAsString();
		IdmProfileDto createdProfile = (IdmProfileDto) getMapper().readValue(response, IdmProfileDto.class);
		Assert.assertEquals(owner.getId(), createdProfile.getIdentity());
		Assert.assertEquals("en", createdProfile.getPreferredLanguage());
	}
	
	@Test
	public void testFindByTreeNodeRecursively() {
		IdmTreeTypeDto treeTypeOne = getHelper().createTreeType();
		IdmTreeTypeDto treeTypeTwo = getHelper().createTreeType();
		IdmTreeNodeDto treeNodeOne = getHelper().createTreeNode(treeTypeOne, null);
		IdmTreeNodeDto treeNodeOneSub = getHelper().createTreeNode(treeTypeOne, treeNodeOne);
		IdmTreeNodeDto treeNodeTwo = getHelper().createTreeNode(treeTypeTwo, null);
		IdmTreeNodeDto treeNodeTwoSub = getHelper().createTreeNode(treeTypeTwo, treeNodeTwo);
		//
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		getHelper().createContract(identityOne, treeNodeOneSub);
		//
		// FIXME: map parameter values in filter into data
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.add("treeNodeId", treeNodeTwo.getId().toString());
		parameters.add("recursively", Boolean.TRUE.toString());
		List<IdmIdentityDto> identities = find(parameters);
		//
		Assert.assertTrue(identities.isEmpty());
		//
		IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);
		getHelper().createContract(identityTwo, treeNodeTwoSub);
		//
		identities = find(parameters);
		//
		Assert.assertEquals(1, identities.size());
		Assert.assertTrue(identities.stream().anyMatch(i -> i.getId().equals(identityTwo.getId())));
	}
	
	@Test
	public void testGetIncompatibleRolesWithoutRemovedInConcept() throws Exception {
		IdmIdentityDto applicant = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto roleOne = getHelper().createRole();
		IdmRoleDto roleTwo = getHelper().createRole();
		IdmRoleDto roleThree = getHelper().createRole();
		IdmRoleDto roleFour = getHelper().createRole();
		IdmRoleDto roleFive = getHelper().createRole();
		IdmRoleDto roleSix = getHelper().createRole();
		// assign roles
		getHelper().createIdentityRole(applicant, roleOne);
		getHelper().createIdentityRole(applicant, roleTwo);
		getHelper().createIdentityRole(applicant, roleThree);
		getHelper().createIdentityRole(applicant, roleFour);
		getHelper().createIdentityRole(applicant, roleFive);
		// create incompatible roles definition
		getHelper().createIncompatibleRole(roleOne, roleTwo);
		getHelper().createIncompatibleRole(roleThree, roleFour);
		getHelper().createIncompatibleRole(roleFive, roleSix);
		//
		String response = getMockMvc().perform(get(String.format("%s/incompatible-roles", getDetailUrl(applicant.getId())))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andReturn()
                .getResponse()
                .getContentAsString();
		//
		Set<IdmIncompatibleRoleDto> incompatibleRoles = toDtos(response, ResolvedIncompatibleRoleDto.class)
				.stream()
				.map(ResolvedIncompatibleRoleDto::getIncompatibleRole)
				.collect(Collectors.toSet());
		Assert.assertEquals(2, incompatibleRoles.size());
		Assert.assertTrue(incompatibleRoles
				.stream()
				.anyMatch(ir -> { 
					return ir.getSuperior().equals(roleOne.getId()) && ir.getSub().equals(roleTwo.getId());
				}));
		Assert.assertTrue(incompatibleRoles
				.stream()
				.anyMatch(ir -> { 
					return ir.getSuperior().equals(roleThree.getId()) && ir.getSub().equals(roleFour.getId());
				}));
	}
	
	/**
	 * Test search by ids - supported by default, id DataFilter is used (see #toPedicates in services - has to call super implementation)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFindByIds() {
		IdmIdentityDto createdDto = createDto();
		IdmIdentityDto createdDtoTwo = createDto();
		// mock dto
		createDto(prepareDto());
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.put(DataFilter.PARAMETER_ID, Lists.newArrayList(createdDto.getId().toString(), createdDtoTwo.getId().toString()));
		//
		List<IdmIdentityDto> results = find(parameters);
		//
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(createdDto.getId())));
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(createdDtoTwo.getId())));
		//
		// find quick alias
		results = findQuick(parameters);
		//
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(createdDto.getId())));
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(createdDtoTwo.getId())));
		//
		results = autocomplete(parameters);
		//
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(createdDto.getId())));
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(createdDtoTwo.getId())));
		//
		Assert.assertEquals(2, count(parameters));
	}
	
	@Test
	public void testFindByFormProjection() {
		IdmIdentityDto identity = prepareDto();
		identity.setFormProjection(createProjection().getId());
		IdmIdentityDto createdDto = createDto(identity);
		identity = prepareDto();
		identity.setFormProjection(createProjection().getId());
		createDto(identity); // other
		//
		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setFormProjection(createdDto.getFormProjection());
		List<IdmIdentityDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().allMatch(r -> r.getId().equals(createdDto.getId())));
		
	}
	
	@Test
	public void testFindWithoutWorkPosition() {
		String description = getHelper().createName();
		//
		// default contract => without position
		IdmIdentityDto identity = prepareDto(); 
		identity.setDescription(description);
		IdmIdentityDto identityOne = createDto(identity);
		Assert.assertEquals(IdentityState.CREATED, identityOne.getState());
		//
		// without contract
		identity = prepareDto();
		identity.setDescription(description);
		identity = createDto(identity);
		contractService.delete(getHelper().getPrimeContract(identity));
		IdmIdentityDto identityTwo = identityService.get(identity);
		Assert.assertEquals(IdentityState.NO_CONTRACT, identityTwo.getState());
		//
		// with default and contract with position
		identity = prepareDto(); 
		identity.setDescription(description);
		IdmIdentityDto identityThree = createDto(identity);
		getHelper().createContract(identityThree, getHelper().createTreeNode());
		//
		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setText(description);
		filter.setWithoutWorkPosition(Boolean.TRUE);
		List<IdmIdentityDto> results = find(filter);
		//
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(identityOne.getId())));
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(identityTwo.getId())));
		//
		filter.setWithoutWorkPosition(Boolean.FALSE);
		results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(identityThree.getId())));
	}
	
	@Test
	public void testFindByRolesLimitExceeded() throws Exception {
		int maximum = configurationService.getIntegerValue(
				FilterManager.PROPERTY_CHECK_FILTER_SIZE_MAXIMUM, 
				FilterManager.DEFAULT_CHECK_FILTER_SIZE_MAXIMUM
		);
		try {
			IdmRoleDto roleOne = getHelper().createRole();
			IdmRoleDto roleTwo = getHelper().createRole();
			IdmRoleDto roleThree = getHelper().createRole();
			
			configurationService.setValue(FilterManager.PROPERTY_CHECK_FILTER_SIZE_MAXIMUM, String.valueOf(2));
			IdmIdentityFilter filter = new IdmIdentityFilter();
			// ok
			filter.setRoles(Lists.newArrayList(roleOne.getId(), roleTwo.getId()));
			Assert.assertTrue(find(filter).isEmpty());
			// not ok
			filter.setRoles(Lists.newArrayList(roleOne.getId(), roleTwo.getId(), roleThree.getId()));
			getMockMvc().perform(get(getFindUrl(null))
	        		.with(authentication(getAdminAuthentication()))
	        		.params(toQueryParams(filter))
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isBadRequest());
		} finally {
			configurationService.setValue(FilterManager.PROPERTY_CHECK_FILTER_SIZE_MAXIMUM, String.valueOf(maximum));
		}
		
	}
	
	@Test
	public void testEnableNotFound() throws Exception {
		getMockMvc().perform(patch(String.format("%s/enable", getDetailUrl(UUID.randomUUID())))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isNotFound());
	}
	
	@Test
	public void testDisableNotFound() throws Exception {
		getMockMvc().perform(patch(String.format("%s/disable", getDetailUrl(UUID.randomUUID())))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isNotFound());
	}
	
	@Test
	public void testCheckUnresolvedRequestsNotFound() throws Exception {
		getMockMvc().perform(get(String.format("%s/check-unresolved-request", getDetailUrl(UUID.randomUUID())))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isNotFound());
	}
	
	@Test
	public void testEmptyCheckUnresolvedRequests() throws Exception {
		IdmIdentityDto identity = createDto();
		//
		String response = getMockMvc().perform(get(String.format("%s/check-unresolved-request", getDetailUrl(identity.getId())))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
		        .andReturn()
		        .getResponse()
		        .getContentAsString();
		//
		Assert.assertTrue(StringUtils.isEmpty(response));
	}
	
	protected IdmFormProjectionDto createProjection() {
		IdmFormProjectionDto dto = new IdmFormProjectionDto();
		dto.setCode(getHelper().createName());
		dto.setOwnerType(IdmIdentity.class.getCanonicalName());
		//
		return formProjectionService.save(dto);
	}
}
