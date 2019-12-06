package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import java.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleFormAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFormAttributeFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleFormAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleFormAttribute_;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Basic role form attribute service test
 * 
 * @author Vít Švanda
 */
public class DefaultIdmRoleFormAttributeServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private FormService formService;
	@Autowired
	private IdmRoleFormAttributeService roleFormAttributeService;
	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;

	private final static String IP = "IP";
	private final static String NUMBER_OF_FINGERS = "NUMBER_OF_FINGERS";

	@Test
	public void testCreateRoleFormAttributeByFormAttribute() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		list.forEach(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			Assert.assertEquals(roleFormAttributeDto.getDefaultValue(), formAttributeDto.getDefaultValue());
			Assert.assertEquals(roleFormAttributeDto.isRequired(), formAttributeDto.isRequired());
		});

	}

	@Test(expected = ResultCodeException.class)
	public void testChangeOfSuperdefinitionNotAllowed() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto definitionTwo = formService.createDefinition(IdmIdentityRole.class,
				getHelper().createName(), ImmutableList.of());
		role.setIdentityRoleAttributeDefinition(definitionTwo.getId());
		// Save role - change of definition is not allowed (if exists some
		// role-form-attribute) -> throw exception
		roleService.save(role);
	}

	@Test
	public void testChangeOfSuperdefinitionAllowed() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		// Before change of definition we will delete all attributes in sub-definition
		// first
		list.forEach(attribute -> {
			roleFormAttributeService.delete(attribute);
		});

		IdmFormDefinitionDto definitionTwo = formService.createDefinition(IdmIdentityRole.class,
				getHelper().createName(), ImmutableList.of());
		role.setIdentityRoleAttributeDefinition(definitionTwo.getId());
		// Save role - change of definition is allowed (none role-form-attribute exists)
		roleService.save(role);
	}

	@Test
	public void testSubDefinition() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());

		// Delete shortText attribute from the sub-definition
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(NUMBER_OF_FINGERS);
		}).forEach(roleFormAttributeDto -> roleFormAttributeService.delete(roleFormAttributeDto));

		formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(1, formAttributeSubdefinition.getFormAttributes().size());
		Assert.assertEquals(IP, formAttributeSubdefinition.getFormAttributes().get(0).getCode());

	}
	
	@Test
	public void testChangeValidityViaRoleRequest() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());

		// Delete IP attribute from the sub-definition
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(IP);
		}).forEach(roleFormAttributeDto -> roleFormAttributeService.delete(roleFormAttributeDto));

		formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(1, formAttributeSubdefinition.getFormAttributes().size());
		Assert.assertEquals(NUMBER_OF_FINGERS, formAttributeSubdefinition.getFormAttributes().get(0).getCode());
		
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		getHelper().assignRoles(contract, role);
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityContractId(contract.getId());
		
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(identityRoleFilter, null).getContent();
		assertEquals(1, identityRoles.size());
		IdmIdentityRoleDto identityRole = identityRoles.get(0);
		
		// Create request
		IdmRoleRequestDto request = getHelper().createRoleRequest(identity);
		// Create change role-concept
		IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
		conceptRoleRequest.setRoleRequest(request.getId());
		conceptRoleRequest.setIdentityContract(contract.getId());
		// Change the valid from
		conceptRoleRequest.setValidFrom(LocalDate.now());
		conceptRoleRequest.setRole(role.getId());
		conceptRoleRequest.setIdentityRole(identityRole.getId());
		conceptRoleRequest.setOperation(ConceptRoleRequestOperation.UPDATE);
		conceptRoleRequest = conceptRoleRequestService.save(conceptRoleRequest);
		
		request = getHelper().executeRequest(request, false, true);
		// Check request
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		// Check identity-role
		identityRole = identityRoleService.get(identityRole.getId());
		assertEquals(conceptRoleRequest.getValidFrom(), identityRole.getValidFrom());

	}
	
	@Test
	public void testCreateRoleAttributeValueViaRoleRequest() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());

		// Delete IP attribute from the sub-definition
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(IP);
		}).forEach(roleFormAttributeDto -> roleFormAttributeService.delete(roleFormAttributeDto));

		formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(1, formAttributeSubdefinition.getFormAttributes().size());
		Assert.assertEquals(NUMBER_OF_FINGERS, formAttributeSubdefinition.getFormAttributes().get(0).getCode());
		
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityContractId(contract.getId());
		
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(identityRoleFilter, null).getContent();
		assertEquals(0, identityRoles.size());
		
		// Create request
		IdmRoleRequestDto request = getHelper().createRoleRequest(identity);
		// Create change role-concept
		IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
		conceptRoleRequest.setRoleRequest(request.getId());
		conceptRoleRequest.setIdentityContract(contract.getId());
		// Change the valid from
		conceptRoleRequest.setValidFrom(LocalDate.now());
		conceptRoleRequest.setRole(role.getId());
		conceptRoleRequest.setOperation(ConceptRoleRequestOperation.ADD);
		conceptRoleRequest = conceptRoleRequestService.save(conceptRoleRequest);
		conceptRoleRequest.getEavs().clear();
		
		// Create role attribute value in concept
		IdmFormInstanceDto formInstanceDto = conceptRoleRequestService.getRoleAttributeValues(conceptRoleRequest, false);
		Assert.assertNotNull(formInstanceDto);
		Assert.assertNotNull(formInstanceDto.getFormDefinition());
		Assert.assertEquals(0, formInstanceDto.getValues().size());
		IdmFormAttributeDto attribute = formInstanceDto.getMappedAttributeByCode(NUMBER_OF_FINGERS);
		IdmFormValueDto formValueDto = new IdmFormValueDto(attribute);
		formValueDto.setValue(BigDecimal.TEN);
		List<IdmFormValueDto> values = Lists.newArrayList(formValueDto);
		formInstanceDto.setValues(values);
		List<IdmFormInstanceDto> forms = Lists.newArrayList(formInstanceDto);
		conceptRoleRequest.setEavs(forms);
		conceptRoleRequest = conceptRoleRequestService.save(conceptRoleRequest);
		
		conceptRoleRequest.getEavs().clear();
		formInstanceDto = conceptRoleRequestService.getRoleAttributeValues(conceptRoleRequest, false);
		Assert.assertEquals(1, formInstanceDto.getValues().size());
		Serializable value = formInstanceDto.toSinglePersistentValue(NUMBER_OF_FINGERS);
		Assert.assertEquals(BigDecimal.TEN.longValue(), ((BigDecimal)value).longValue());
		
		request = getHelper().executeRequest(request, false, true);
		// Check request
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		
		identityRoles = identityRoleService.find(identityRoleFilter, null).getContent();
		assertEquals(1, identityRoles.size());
		IdmIdentityRoleDto identityRole = identityRoles.get(0);
		// Check identity-role
		identityRole = identityRoleService.get(identityRole.getId());
		assertEquals(conceptRoleRequest.getValidFrom(), identityRole.getValidFrom());
		
		// Check role attribute value in identity-role
		identityRole.getEavs().clear();
		IdmFormInstanceDto identityRoleFormInstanceDto = identityRoleService.getRoleAttributeValues(identityRole);
		Assert.assertEquals(1, identityRoleFormInstanceDto.getValues().size());
		value = identityRoleFormInstanceDto.toSinglePersistentValue(NUMBER_OF_FINGERS);
		Assert.assertEquals(BigDecimal.TEN.longValue(), ((BigDecimal)value).longValue());
	}
	
	@Test
	public void testDeleteRoleAttributeValueViaRoleRequest() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());

		// Delete IP attribute from the sub-definition
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(IP);
		}).forEach(roleFormAttributeDto -> roleFormAttributeService.delete(roleFormAttributeDto));

		formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(1, formAttributeSubdefinition.getFormAttributes().size());
		Assert.assertEquals(NUMBER_OF_FINGERS, formAttributeSubdefinition.getFormAttributes().get(0).getCode());

		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityContractId(contract.getId());

		List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(identityRoleFilter, null).getContent();
		assertEquals(0, identityRoles.size());

		// Create request
		IdmRoleRequestDto request = getHelper().createRoleRequest(identity);
		// Create change role-concept
		IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
		conceptRoleRequest.setRoleRequest(request.getId());
		conceptRoleRequest.setIdentityContract(contract.getId());
		// Change the valid from
		conceptRoleRequest.setValidFrom(LocalDate.now());
		conceptRoleRequest.setRole(role.getId());
		conceptRoleRequest.setOperation(ConceptRoleRequestOperation.ADD);
		conceptRoleRequest = conceptRoleRequestService.save(conceptRoleRequest);
		conceptRoleRequest.getEavs().clear();

		// Create role attribute value in concept
		IdmFormInstanceDto formInstanceDto = conceptRoleRequestService.getRoleAttributeValues(conceptRoleRequest,
				false);
		Assert.assertNotNull(formInstanceDto);
		Assert.assertNotNull(formInstanceDto.getFormDefinition());
		Assert.assertEquals(0, formInstanceDto.getValues().size());
		IdmFormAttributeDto attribute = formInstanceDto.getMappedAttributeByCode(NUMBER_OF_FINGERS);
		IdmFormValueDto formValueDto = new IdmFormValueDto(attribute);
		formValueDto.setValue(BigDecimal.TEN);
		List<IdmFormValueDto> values = Lists.newArrayList(formValueDto);
		formInstanceDto.setValues(values);
		List<IdmFormInstanceDto> forms = Lists.newArrayList(formInstanceDto);
		conceptRoleRequest.setEavs(forms);
		conceptRoleRequest = conceptRoleRequestService.save(conceptRoleRequest);

		conceptRoleRequest.getEavs().clear();
		formInstanceDto = conceptRoleRequestService.getRoleAttributeValues(conceptRoleRequest, false);
		Assert.assertEquals(1, formInstanceDto.getValues().size());
		Serializable value = formInstanceDto.toSinglePersistentValue(NUMBER_OF_FINGERS);
		Assert.assertEquals(BigDecimal.TEN.longValue(), ((BigDecimal) value).longValue());

		request = getHelper().executeRequest(request, false, true);
		// Check request
		assertEquals(RoleRequestState.EXECUTED, request.getState());

		identityRoles = identityRoleService.find(identityRoleFilter, null).getContent();
		assertEquals(1, identityRoles.size());
		IdmIdentityRoleDto identityRole = identityRoles.get(0);
		// Check identity-role
		identityRole = identityRoleService.get(identityRole.getId());
		assertEquals(conceptRoleRequest.getValidFrom(), identityRole.getValidFrom());

		// Check role attribute value in identity-role
		identityRole.getEavs().clear();
		IdmFormInstanceDto identityRoleFormInstanceDto = identityRoleService.getRoleAttributeValues(identityRole);
		Assert.assertEquals(1, identityRoleFormInstanceDto.getValues().size());
		value = identityRoleFormInstanceDto.toSinglePersistentValue(NUMBER_OF_FINGERS);
		Assert.assertEquals(BigDecimal.TEN.longValue(), ((BigDecimal) value).longValue());
		
		// Create request for delete attribute value
		IdmRoleRequestDto requestUpdate = getHelper().createRoleRequest(identity);
		// Create change role-concept
		IdmConceptRoleRequestDto updateConceptRoleRequest = new IdmConceptRoleRequestDto();
		updateConceptRoleRequest.setRoleRequest(requestUpdate.getId());
		updateConceptRoleRequest.setIdentityContract(contract.getId());
		// Change the valid from
		updateConceptRoleRequest.setValidFrom(LocalDate.now());
		updateConceptRoleRequest.setRole(role.getId());
		updateConceptRoleRequest.setIdentityRole(identityRole.getId());
		updateConceptRoleRequest.setOperation(ConceptRoleRequestOperation.UPDATE);
		updateConceptRoleRequest = conceptRoleRequestService.save(updateConceptRoleRequest);
		updateConceptRoleRequest.getEavs().clear();
		
		/**
		 * Erase role attribute value in concept
		 */
		formInstanceDto = conceptRoleRequestService.getRoleAttributeValues(updateConceptRoleRequest, false);
		Assert.assertNotNull(formInstanceDto);
		Assert.assertNotNull(formInstanceDto.getFormDefinition());
		Assert.assertEquals(0, formInstanceDto.getValues().size());
		attribute = formInstanceDto.getMappedAttributeByCode(NUMBER_OF_FINGERS);
		formValueDto = new IdmFormValueDto(attribute);
		formValueDto.setValue(null);
		values = Lists.newArrayList(formValueDto);
		formInstanceDto.setValues(values);
		forms = Lists.newArrayList(formInstanceDto);
		updateConceptRoleRequest.setEavs(forms);
		updateConceptRoleRequest = conceptRoleRequestService.save(updateConceptRoleRequest);
		updateConceptRoleRequest.getEavs().clear();
		formInstanceDto = conceptRoleRequestService.getRoleAttributeValues(updateConceptRoleRequest, false);
		// No form-value can exists
		Assert.assertEquals(0, formInstanceDto.getValues().size());
		
		requestUpdate = getHelper().executeRequest(requestUpdate, false, true);
		// Check request
		assertEquals(RoleRequestState.EXECUTED, requestUpdate.getState());
		
		identityRoles = identityRoleService.find(identityRoleFilter, null).getContent();
		assertEquals(1, identityRoles.size());
		identityRole = identityRoles.get(0);
		// Check identity-role
		identityRole = identityRoleService.get(identityRole.getId());
		assertEquals(updateConceptRoleRequest.getValidFrom(), identityRole.getValidFrom());
		
		// Check role attribute value in identity-role
		identityRole.getEavs().clear();
		identityRoleFormInstanceDto = identityRoleService.getRoleAttributeValues(identityRole);
		Assert.assertEquals(0, identityRoleFormInstanceDto.getValues().size());
	}
	
	
	@Test
	public void testOverrideDefaultAttributeHasMaxValidation() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());

		// Delete IP attribute from the sub-definition
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(IP);
		}).forEach(roleFormAttributeDto -> roleFormAttributeService.delete(roleFormAttributeDto));

		formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(1, formAttributeSubdefinition.getFormAttributes().size());
		IdmFormAttributeDto numberOfFingersAttribute = formAttributeSubdefinition.getFormAttributes().get(0);
		Assert.assertEquals(NUMBER_OF_FINGERS, numberOfFingersAttribute.getCode());
		
		// Change validation max from 10 to 11
		list.stream() //
				.filter(roleFormAttribute -> roleFormAttribute.getFormAttribute()
						.equals(numberOfFingersAttribute.getId()))
				.forEach(roleFormAttribute -> { //
					roleFormAttribute.setMax(BigDecimal.valueOf(11));
					roleFormAttributeService.save(roleFormAttribute);
				});
		
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityContractId(contract.getId());
		
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(identityRoleFilter, null).getContent();
		assertEquals(0, identityRoles.size());
		
		// Create request
		IdmRoleRequestDto request = getHelper().createRoleRequest(identity);
		// Create change role-concept
		IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
		conceptRoleRequest.setRoleRequest(request.getId());
		conceptRoleRequest.setIdentityContract(contract.getId());
		// Change the valid from
		conceptRoleRequest.setValidFrom(LocalDate.now());
		conceptRoleRequest.setRole(role.getId());
		conceptRoleRequest.setOperation(ConceptRoleRequestOperation.ADD);
		conceptRoleRequest = conceptRoleRequestService.save(conceptRoleRequest);
		conceptRoleRequest.getEavs().clear();
		
		// Create role attribute value in concept
		IdmFormInstanceDto formInstanceDto = conceptRoleRequestService.getRoleAttributeValues(conceptRoleRequest, false);
		Assert.assertNotNull(formInstanceDto);
		Assert.assertNotNull(formInstanceDto.getFormDefinition());
		Assert.assertEquals(0, formInstanceDto.getValues().size());
		IdmFormAttributeDto attribute = formInstanceDto.getMappedAttributeByCode(NUMBER_OF_FINGERS);
		IdmFormValueDto formValueDto = new IdmFormValueDto(attribute);
		formValueDto.setValue(BigDecimal.valueOf(11));
		List<IdmFormValueDto> values = Lists.newArrayList(formValueDto);
		formInstanceDto.setValues(values);
		List<IdmFormInstanceDto> forms = Lists.newArrayList(formInstanceDto);
		conceptRoleRequest.setEavs(forms);
		conceptRoleRequest = conceptRoleRequestService.save(conceptRoleRequest);
		
		conceptRoleRequest.getEavs().clear();
		formInstanceDto = conceptRoleRequestService.getRoleAttributeValues(conceptRoleRequest, false);
		Assert.assertEquals(1, formInstanceDto.getValues().size());
		Serializable value = formInstanceDto.toSinglePersistentValue(NUMBER_OF_FINGERS);
		Assert.assertEquals(BigDecimal.valueOf(11).longValue(), ((BigDecimal)value).longValue());
		
		request = getHelper().executeRequest(request, false, true);
		// Check request
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		
		identityRoles = identityRoleService.find(identityRoleFilter, null).getContent();
		assertEquals(1, identityRoles.size());
		IdmIdentityRoleDto identityRole = identityRoles.get(0);
		// Check identity-role
		identityRole = identityRoleService.get(identityRole.getId());
		assertEquals(conceptRoleRequest.getValidFrom(), identityRole.getValidFrom());
		
		// Check role attribute value in identity-role
		identityRole.getEavs().clear();
		IdmFormInstanceDto identityRoleFormInstanceDto = identityRoleService.getRoleAttributeValues(identityRole);
		Assert.assertEquals(1, identityRoleFormInstanceDto.getValues().size());
		value = identityRoleFormInstanceDto.toSinglePersistentValue(NUMBER_OF_FINGERS);
		Assert.assertEquals(BigDecimal.valueOf(11).longValue(), ((BigDecimal)value).longValue());
	}

	@Test
	public void testSubDefinitionOverrideDefaultValue() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());

		// Set unique defaultValue to IP attribute in the sub-definition
		String uniqueDefaultValue = this.getHelper().createName();
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(IP);
		}).forEach(roleFormAttributeDto -> {
			roleFormAttributeDto.setDefaultValue(uniqueDefaultValue);
			roleFormAttributeService.save(roleFormAttributeDto);
		});

		// Load sub-definition by role
		formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());
		IdmFormAttributeDto ipFormAttribute = formAttributeSubdefinition.getFormAttributes().stream()
				.filter(attributeDto -> {
					return attributeDto.getCode().equals(IP);
				}).findFirst().orElse(null);

		Assert.assertNotNull(ipFormAttribute);
		Assert.assertEquals(uniqueDefaultValue, ipFormAttribute.getDefaultValue());
	}
	
	

	@Test
	public void testSubDefinitionOverrideValidationRequired() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());

		// Set required validation on false to IP attribute in the sub-definition
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(IP);
		}).forEach(roleFormAttributeDto -> {
			Assert.assertTrue(roleFormAttributeDto.isRequired());
			roleFormAttributeDto.setRequired(false);
			roleFormAttributeService.save(roleFormAttributeDto);
		});

		// Load sub-definition by role
		formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());
		IdmFormAttributeDto ipFormAttribute = formAttributeSubdefinition.getFormAttributes().stream()
				.filter(attributeDto -> {
					return attributeDto.getCode().equals(IP);
				}).findFirst().orElse(null);

		Assert.assertNotNull(ipFormAttribute);
		Assert.assertFalse(ipFormAttribute.isRequired());
	}
	
	@Test
	public void testSubDefinitionOverrideValidationUnique() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());

		// Set unique validation on false to IP attribute in the sub-definition
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(IP);
		}).forEach(roleFormAttributeDto -> {
			Assert.assertFalse(roleFormAttributeDto.isUnique());
			roleFormAttributeDto.setUnique(true);
			roleFormAttributeService.save(roleFormAttributeDto);
		});

		// Load sub-definition by role
		formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());
		IdmFormAttributeDto ipFormAttribute = formAttributeSubdefinition.getFormAttributes().stream()
				.filter(attributeDto -> {
					return attributeDto.getCode().equals(IP);
				}).findFirst().orElse(null);

		Assert.assertNotNull(ipFormAttribute);
		Assert.assertTrue(ipFormAttribute.isUnique());
	}
	
	@Test
	public void testSubDefinitionOverrideValidationReqex() {
		String regex = "regex";

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());

		// Set regex validation on false to IP attribute in the sub-definition
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(IP);
		}).forEach(roleFormAttributeDto -> {
			Assert.assertNull(roleFormAttributeDto.getRegex());
			roleFormAttributeDto.setRegex(regex);
			roleFormAttributeService.save(roleFormAttributeDto);
		});

		// Load sub-definition by role
		formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());
		IdmFormAttributeDto ipFormAttribute = formAttributeSubdefinition.getFormAttributes().stream()
				.filter(attributeDto -> {
					return attributeDto.getCode().equals(IP);
				}).findFirst().orElse(null);

		Assert.assertNotNull(ipFormAttribute);
		Assert.assertEquals(regex, ipFormAttribute.getRegex());
	}
	
	@Test
	public void testSubDefinitionOverrideValidationMessage() {
		String validationMessage = getHelper().createName();

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());

		// Set validation message on false to IP attribute in the sub-definition
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(IP);
		}).forEach(roleFormAttributeDto -> {
			Assert.assertNull(roleFormAttributeDto.getValidationMessage());
			roleFormAttributeDto.setValidationMessage(validationMessage);
			roleFormAttributeService.save(roleFormAttributeDto);
		});

		// Load sub-definition by role
		formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());
		IdmFormAttributeDto ipFormAttribute = formAttributeSubdefinition.getFormAttributes().stream()
				.filter(attributeDto -> {
					return attributeDto.getCode().equals(IP);
				}).findFirst().orElse(null);

		Assert.assertNotNull(ipFormAttribute);
		Assert.assertEquals(validationMessage, ipFormAttribute.getValidationMessage());
	}
	
	@Test
	public void testSubDefinitionOverrideValidationMin() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());

		// Delete IP attribute from the sub-definition
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(IP);
		}).forEach(roleFormAttributeDto -> roleFormAttributeService.delete(roleFormAttributeDto));

		// Set MIN
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(NUMBER_OF_FINGERS);
		}).forEach(roleFormAttributeDto -> {
			Assert.assertNull(roleFormAttributeDto.getMin());
			roleFormAttributeDto.setMin(BigDecimal.valueOf(111));
			roleFormAttributeService.save(roleFormAttributeDto);
		});

		// Load sub-definition by role
		formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(1, formAttributeSubdefinition.getFormAttributes().size());
		IdmFormAttributeDto numberAttribute = formAttributeSubdefinition.getFormAttributes().stream()
				.filter(attributeDto -> {
					return attributeDto.getCode().equals(NUMBER_OF_FINGERS);
				}).findFirst().orElse(null);

		Assert.assertNotNull(numberAttribute);
		Assert.assertEquals(111, numberAttribute.getMin().intValue());
	}
	
	@Test
	public void testSubDefinitionOverrideValidationMax() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());

		// Delete IP attribute from the sub-definition
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(IP);
		}).forEach(roleFormAttributeDto -> roleFormAttributeService.delete(roleFormAttributeDto));
		
		// Set Max
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(NUMBER_OF_FINGERS);
		}).forEach(roleFormAttributeDto -> {
			Assert.assertEquals(BigDecimal.TEN.longValue(), roleFormAttributeDto.getMax().longValue());
			roleFormAttributeDto.setMax(BigDecimal.valueOf(111));
			roleFormAttributeService.save(roleFormAttributeDto);
		});

		// Load sub-definition by role
		formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(1, formAttributeSubdefinition.getFormAttributes().size());
		IdmFormAttributeDto numberAttribute = formAttributeSubdefinition.getFormAttributes().stream()
				.filter(attributeDto -> {
					return attributeDto.getCode().equals(NUMBER_OF_FINGERS);
				}).findFirst().orElse(null);

		Assert.assertNotNull(numberAttribute);
		Assert.assertEquals(111, numberAttribute.getMax().intValue()); 
	}
	
	@Test(expected = ResultCodeException.class)
	public void testIntegrityDeleteAttributeDefinition() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());
		// Find attribute definition
		IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(list.get(0),
				IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
		Assert.assertNotNull(formAttributeDto);

		// Definition of this attribute is using in the sub-definition -> exception must
		// be throws
		formService.deleteAttribute(formAttributeDto);
	}

	private IdmRoleDto createRoleWithAttributes() {
		IdmRoleDto role = getHelper().createRole();
		assertNull(role.getIdentityRoleAttributeDefinition());

		IdmFormAttributeDto ipAttribute = new IdmFormAttributeDto(IP);
		ipAttribute.setPersistentType(PersistentType.TEXT);
		ipAttribute.setRequired(true);
		ipAttribute.setDefaultValue(getHelper().createName());

		IdmFormAttributeDto numberOfFingersAttribute = new IdmFormAttributeDto(NUMBER_OF_FINGERS);
		numberOfFingersAttribute.setPersistentType(PersistentType.DOUBLE);
		numberOfFingersAttribute.setRequired(false);
		numberOfFingersAttribute.setMax(BigDecimal.TEN);
		
		IdmFormDefinitionDto definition = formService.createDefinition(IdmIdentityRole.class, getHelper().createName(),
				ImmutableList.of(ipAttribute, numberOfFingersAttribute));
		role.setIdentityRoleAttributeDefinition(definition.getId());
		role = roleService.save(role);
		assertNotNull(role.getIdentityRoleAttributeDefinition());
		IdmRoleDto roleFinal = role;
		definition.getFormAttributes().forEach(attribute -> {
			roleFormAttributeService.addAttributeToSubdefintion(roleFinal, attribute);
		});

		return role;
	}
}
