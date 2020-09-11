package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.beans.IntrospectionException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.domain.RoleType;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleGuaranteeRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleFormAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Basic role service operations
 * TODO: move filter tests to rest test
 * 
 * @author Radek Tomiška
 * @author Marek Klement
 *
 */
@Transactional
public class DefaultIdmRoleServiceIntegrationTest extends AbstractRestTest {

	@Autowired private ApplicationContext context;
	@Autowired private IdmRoleCatalogueRoleService idmRoleCatalogueRoleService;
	@Autowired private IdmRoleGuaranteeService roleGuaranteeService;
	@Autowired private IdmRoleGuaranteeRoleService roleGuaranteeRoleService;
	@Autowired private IdmAuthorizationPolicyService authorizationPolicyService;
	@Autowired private RoleConfiguration roleConfiguration;
	@Autowired private FormService formService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmConceptRoleRequestService conceptRoleService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmRoleFormAttributeService roleFormAttributeService;
	//
	private final static String IP = "IP";
	private DefaultIdmRoleService roleService;

	@Before
	public void init() {
		 roleService = context.getAutowireCapableBeanFactory().createBean(DefaultIdmRoleService.class);
	}
	
	@Test
	public void testReferentialIntegrity() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString("heslo"));
		// role
		IdmRoleDto role = getHelper().createRole();
		getHelper().createRoleGuarantee(role, identity);
		//
		// guarantee by role
		IdmRoleGuaranteeRoleDto roleGuaranteeOne = new IdmRoleGuaranteeRoleDto();
		roleGuaranteeOne.setRole(role.getId());
		roleGuaranteeOne.setGuaranteeRole(getHelper().createRole().getId());
		roleGuaranteeRoleService.save(roleGuaranteeOne);
		IdmRoleGuaranteeRoleDto roleGuaranteeTwo = new IdmRoleGuaranteeRoleDto();
		roleGuaranteeTwo.setRole(getHelper().createRole().getId());
		roleGuaranteeTwo.setGuaranteeRole(role.getId());
		roleGuaranteeRoleService.save(roleGuaranteeTwo);
		//
		// after save
		IdmRoleGuaranteeFilter guaranteeFilter = new IdmRoleGuaranteeFilter();
		guaranteeFilter.setRole(role.getId());
		IdmRoleGuaranteeRoleFilter guaranteeRoleFilter = new IdmRoleGuaranteeRoleFilter();
		guaranteeRoleFilter.setRole(role.getId());
		IdmRoleGuaranteeRoleFilter guaranteeRoleRoleFilter = new IdmRoleGuaranteeRoleFilter();
		guaranteeRoleRoleFilter.setGuaranteeRole(role.getId());
		//
		Assert.assertNotNull(roleService.getByCode(role.getCode()));
		Assert.assertEquals(1, roleGuaranteeService.find(guaranteeFilter, null).getTotalElements());
		Assert.assertEquals(1, roleGuaranteeRoleService.find(guaranteeRoleFilter, null).getTotalElements());
		Assert.assertEquals(1, roleGuaranteeRoleService.find(guaranteeRoleFilter, null).getTotalElements());
		
		roleService.delete(role);
		//
		// after delete
		Assert.assertNull(roleService.getByCode(role.getCode()));
		Assert.assertEquals(0, roleGuaranteeService.find(guaranteeFilter, null).getTotalElements());
		Assert.assertEquals(0, roleGuaranteeRoleService.find(guaranteeRoleFilter, null).getTotalElements());
		Assert.assertEquals(0, roleGuaranteeRoleService.find(guaranteeRoleFilter, null).getTotalElements());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testReferentialIntegrityAssignedRoles() {
		// prepare data
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto role = getHelper().createRole();
		// assigned role
		getHelper().createIdentityRole(identity, role);
		//
		roleService.delete(role);
	}
	
	@Test(expected = ResultCodeException.class)
	public void testReferentialIntegrityHasRoleComposition() {
		// prepare data
		IdmRoleDto role = getHelper().createRole();
		IdmRoleDto subrole = getHelper().createRole();
		// assigned role
		getHelper().createRoleComposition(role, subrole);
		//
		roleService.delete(role);
	}
	
	@Test
	public void testReferentialIntegrityAuthorizationPolicies() {
		// prepare data
		IdmRoleDto role = getHelper().createRole();
		// policy
		getHelper().createBasePolicy(role.getId(), IdmBasePermission.ADMIN);
		//
		roleService.delete(role);
		//
		IdmAuthorizationPolicyFilter policyFilter = new IdmAuthorizationPolicyFilter();
		policyFilter.setRoleId(role.getId());
		Assert.assertEquals(0, authorizationPolicyService.find(policyFilter, null).getTotalElements());
	}

	@Test
	public void textFilterTest(){
		getHelper().createRole("SomeName001");
		getHelper().createRole("SomeName002");
		getHelper().createRole("SomeName003");
		getHelper().createRole("SomeName104");

		IdmRoleDto role5 = new IdmRoleDto();
		role5.setDescription("SomeName005");
		role5.setCode("SomeName105");
		role5 = roleService.save(role5);

		IdmRoleFilter filter = new IdmRoleFilter();
		filter.setText("SomeName00");
		Page<IdmRoleDto> result = roleService.find(filter,null);
		Assert.assertEquals("Wrong text filter", 4, result.getTotalElements());
		Assert.assertEquals("Wrong text filter description", true, result.getContent().contains(role5));
	}

	@Test
	public void typeFilterTest(){
		IdmRoleDto role = getHelper().createRole();
		IdmRoleDto roleTwo = getHelper().createRole(); // other

		RoleType type = RoleType.SYSTEM;
		role.setRoleType(type);
		IdmRoleDto roleOne = roleService.save(role);

		IdmRoleFilter filter = new IdmRoleFilter();
		filter.setRoleType(type);
		Page<IdmRoleDto> result = roleService.find(filter, null);
		Assert.assertTrue(result.stream().anyMatch(r -> r.getId().equals(roleOne.getId())));
		Assert.assertTrue(result.stream().allMatch(r -> !r.getId().equals(roleTwo.getId())));
	}

	@Test
	public void guaranteeFilterTest(){
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto role = getHelper().createRole();
		getHelper().createRoleGuarantee(role, identity);
		//
		IdmRoleFilter filter = new IdmRoleFilter();
		filter.setGuaranteeId(identity.getId());
		Page<IdmRoleDto> result = roleService.find(filter, null);
		Assert.assertEquals("Wrong guarantee", 1, result.getTotalElements());
		Assert.assertEquals("Wrong guarantee id", role.getId(), result.getContent().get(0).getId());
	}
	
	@Test
	public void guaranteeRoleFilterTest(){
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityOther = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto role = getHelper().createRole();
		IdmRoleDto guaranteeRole = getHelper().createRole();
		IdmRoleGuaranteeRoleDto roleGuaranteeRole = new IdmRoleGuaranteeRoleDto();
		roleGuaranteeRole.setRole(role.getId());
		roleGuaranteeRole.setGuaranteeRole(guaranteeRole.getId());
		roleGuaranteeRoleService.save(roleGuaranteeRole);
		getHelper().createIdentityRole(identity, guaranteeRole);
		//
		IdmRoleFilter filter = new IdmRoleFilter();
		filter.setGuaranteeId(identity.getId());
		Page<IdmRoleDto> result = roleService.find(filter, null);
		Assert.assertEquals("Wrong guarantee", 1, result.getTotalElements());
		Assert.assertEquals("Wrong guarantee id", role.getId(), result.getContent().get(0).getId());
		//
		filter.setGuaranteeId(identityOther.getId());
		result = roleService.find(filter, null);
	}

	@Test
	public void catalogueFilterTest(){
		try {
			loginAsAdmin();
		
			IdmRoleDto role = getHelper().createRole();
	
			IdmRoleCatalogueDto catalogue = getHelper().createRoleCatalogue();
			IdmRoleCatalogueRoleDto catalogueRole = new IdmRoleCatalogueRoleDto();
			catalogueRole.setRole(role.getId());
			catalogueRole.setRoleCatalogue(catalogue.getId());
			catalogueRole = idmRoleCatalogueRoleService.save(catalogueRole);
	
			IdmRoleFilter filter = new IdmRoleFilter();
			filter.setRoleCatalogueId(catalogue.getId());
			Page<IdmRoleDto> result = roleService.find(filter,null);
			Assert.assertEquals("Wrong catalogue", 1, result.getTotalElements());
			Assert.assertTrue("Wrong catalogue id #1", result.getContent().contains(role));
		} finally {
			logout();
		}
	}
	
	/**
	 * Test find role by all string fields
	 */
	@Test
	public void testCorrelableFilter() {
		IdmRoleDto role = getHelper().createRole();
		role.setExternalId(getHelper().createName());
		role.setCode(getHelper().createName());
		role.setDescription(getHelper().createName());
		IdmRoleDto roleFull = roleService.save(role);

		ArrayList<Field> fields = Lists.newArrayList(IdmRole_.class.getFields());
		IdmRoleFilter filter = new IdmRoleFilter();

		fields.forEach(field -> {
			if (Modifier.isStatic(field.getModifiers())) {
				// static filter properties cannot be used by simple correlable filter
			} else {
				filter.setProperty(field.getName());
	
				try {
					Object value = EntityUtils.getEntityValue(roleFull, field.getName());
					if (value == null || !(value instanceof String)) {
						return;
					}
					filter.setValue(value.toString());
					List<IdmRoleDto> identities = roleService.find(filter, null).getContent();
					Assert.assertTrue(identities.contains(roleFull));
	
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| IntrospectionException e) {
					e.printStackTrace();
				}
			}
		});

	}
	
	@Test
	public void testAutoFillCodeAndName() {
		IdmRoleDto role = new IdmRoleDto();
		role.setName(getHelper().createName());
		//
		role = roleService.save(role);
		Assert.assertEquals(role.getName(), role.getCode());
		//
		role = new IdmRoleDto();
		role.setCode(getHelper().createName());
		//
		role = roleService.save(role);
		Assert.assertEquals(role.getCode(), role.getName());
		//
		role = new IdmRoleDto();
		role.setCode(getHelper().createName());
		role.setName(getHelper().createName());
		//
		role = roleService.save(role);
		Assert.assertNotEquals(role.getCode(), role.getName());
	}
	
	@Test
	public void testGetAdminRole() {
		Assert.assertEquals(roleConfiguration.getAdminRole(), roleService.getAdminRole());
	}
	
	@Test
	public void testGetRolesByIds() {
		IdmRoleDto roleOne = getHelper().createRole();
		IdmRoleDto roleTwo = getHelper().createRole();
		//
		List<IdmRoleDto> roles = roleService.getRolesByIds(StringUtils.join(Lists.newArrayList(roleOne.getId(), roleTwo.getId()), ','));
		//
		Assert.assertEquals(2, roles.size());
		Assert.assertTrue(roles.stream().anyMatch(r -> r.getId().equals(roleOne.getId())));
		Assert.assertTrue(roles.stream().anyMatch(r -> r.getId().equals(roleTwo.getId())));
	}

	@Test
	public void testGetByCode() {
		String roleName = "role-name " + System.currentTimeMillis();
		String roleCode = "roleCode" + System.currentTimeMillis();

		IdmRoleDto role = new IdmRoleDto();
		role.setName(roleName);
		role.setCode(roleCode);
		role = roleService.save(role);
		
		IdmRoleDto roleDto = roleService.getByCode("notRoleCode" + System.currentTimeMillis());
		assertNull(roleDto);

		roleDto = roleService.getByCode(roleName);
		assertNull(roleDto);

		roleDto = roleService.getByCode(roleCode);
		assertNotNull(roleDto);
		assertEquals(role.getId(), roleDto.getId());
	}
	
	@Test
	public void testCreateRoleWithAttributes() {
		this.createRoleWithAttributes();
	}
	
	@Test(expected = ResultCodeException.class)
	public void testCreateIdentityRoleWithoutRequiredValue() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto role = createRoleWithAttributes();
		IdmIdentityContractDto identityContact = getHelper().createContract(identity);
		
		IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
		identityRole.setIdentityContract(identityContact.getId());
		identityRole.setRole(role.getId());
		identityRole = identityRoleService.save(identityRole); 
	}
	
	@Test
	public void testCreateIdentityRoleWithRequiredValue() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto role = createRoleWithAttributes();
		IdmIdentityContractDto identityContact = getHelper().createContract(identity);
		IdmFormDefinitionDto definition = formService.getDefinition(role.getIdentityRoleAttributeDefinition());
		IdmFormAttributeDto ipAttributeDto = definition.getFormAttributes().stream() //
				.filter(attribute -> IP.equals(attribute.getCode())) //
				.findFirst() //
				.get(); //

		// Add value
		IdmFormValueDto formValue = new IdmFormValueDto(ipAttributeDto);
		formValue.setStringValue(getHelper().createName());
		formValue.setPersistentType(PersistentType.TEXT);
		formValue.setFormAttribute(ipAttributeDto.getId());

		IdmFormInstanceDto formInstance = new IdmFormInstanceDto();
		formInstance.setFormDefinition(definition);
		formInstance.getValues().add(formValue);
		//
		IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
		identityRole.setIdentityContract(identityContact.getId());
		identityRole.setRole(role.getId());
		identityRole.getEavs().add(formInstance);

		identityRole = identityRoleService.save(identityRole);

		assertTrue(!identityRole.getEavs().isEmpty());
		formInstance = identityRoleService.getRoleAttributeValues(identityRole);
		assertEquals(definition, formInstance.getFormDefinition());
		assertEquals(formValue.getStringValue(), formInstance.getValues().get(0).getStringValue());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testCreateConceptRoleWithoutRequiredValue() throws Exception {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto role = createRoleWithAttributes();
		IdmIdentityContractDto identityContact = getHelper().createContract(identity);
		
		IdmRoleRequestDto roleRequest = getHelper().createRoleRequest(identityContact, role);
		
	    // Get request by id
		String response = getMockMvc().perform(get(getDetailRoleRequestUrl(roleRequest.getId()))
					.with(authentication(getAdminAuthentication()))
					.contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isOk())
	                .andReturn()
	                .getResponse()
	                .getContentAsString();
		
		IdmRoleRequestDto createdDto = (IdmRoleRequestDto) getMapper().readValue(response, roleRequest.getClass());
		// Request from REST doesn't contains concept (from version 9.7.0!)
		assertTrue(createdDto.getConceptRoles().isEmpty());
		roleRequestService.validate(createdDto);
		
		roleRequestService.validate(roleRequestService.get(createdDto.getId(), new IdmRoleRequestFilter(true)));
	}
	
	@Test
	public void testCreateConceptRoleWithRequiredValue() throws Exception {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto role = createRoleWithAttributes();
		IdmIdentityContractDto identityContact = getHelper().createContract(identity);
		IdmFormDefinitionDto definition = formService.getDefinition(role.getIdentityRoleAttributeDefinition());
		IdmFormAttributeDto ipAttributeDto = definition.getFormAttributes().stream() //
				.filter(attribute -> IP.equals(attribute.getCode())) //
				.findFirst() //
				.get(); //

		// Add value
		IdmFormValueDto formValue = new IdmFormValueDto(ipAttributeDto);
		formValue.setStringValue(getHelper().createName());
		formValue.setPersistentType(PersistentType.TEXT);
		formValue.setFormAttribute(ipAttributeDto.getId());

		IdmFormInstanceDto formInstance = new IdmFormInstanceDto();
		formInstance.setFormDefinition(definition);
		formInstance.getValues().add(formValue);
		// Create request
		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicant(identity.getId());
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		request.setExecuteImmediately(true);
		request = roleRequestService.save(request);
		// Create concept
		IdmConceptRoleRequestDto conceptRole = new IdmConceptRoleRequestDto();
		conceptRole.setIdentityContract(identityContact.getId());
		conceptRole.setRole(role.getId());
		conceptRole.setRoleRequest(request.getId());
		conceptRole.getEavs().add(formInstance);
		conceptRole = conceptRoleService.save(conceptRole);
		
		 // Get request by id
		String response = getMockMvc().perform(get(getDetailRoleRequestUrl(request.getId()))
					.with(authentication(getAdminAuthentication()))
					.contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isOk())
	                .andReturn()
	                .getResponse()
	                .getContentAsString();
		
		IdmRoleRequestDto restRequest = (IdmRoleRequestDto) getMapper().readValue(response, request.getClass());
		// Validate request
		roleRequestService.validate(restRequest);
		
		 // Get request by id
		String responseConcept = getMockMvc().perform(get(getDetailConceptRoleRequestUrl(conceptRole.getId()))
					.with(authentication(getAdminAuthentication()))
					.contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isOk())
	                .andReturn()
	                .getResponse()
	                .getContentAsString();
		
		IdmConceptRoleRequestDto restConcept = (IdmConceptRoleRequestDto) getMapper().readValue(responseConcept, conceptRole.getClass());

		assertTrue(!restConcept.getEavs().isEmpty());
		formInstance = conceptRoleService.getRoleAttributeValues(restConcept, false);
		assertEquals(formValue.getStringValue(), formInstance.getValues().get(0).getStringValue());
	}
	
	@Test
	public void testExecuteRoleRequestValue() throws Exception {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto role = createRoleWithAttributes();
		IdmIdentityContractDto identityContact = getHelper().createContract(identity);
		IdmFormDefinitionDto definition = formService.getDefinition(role.getIdentityRoleAttributeDefinition());
		IdmFormAttributeDto ipAttributeDto = definition.getFormAttributes().stream() //
				.filter(attribute -> IP.equals(attribute.getCode())) //
				.findFirst() //
				.get(); //

		// Add value
		IdmFormValueDto formValue = new IdmFormValueDto(ipAttributeDto);
		formValue.setStringValue(getHelper().createName());
		formValue.setPersistentType(PersistentType.TEXT);
		formValue.setFormAttribute(ipAttributeDto.getId());

		IdmFormInstanceDto formInstance = new IdmFormInstanceDto();
		formInstance.setFormDefinition(definition);
		formInstance.getValues().add(formValue);
		// Create request
		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicant(identity.getId());
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		request.setExecuteImmediately(true);
		request = roleRequestService.save(request);
		// Create concept
		IdmConceptRoleRequestDto conceptRole = new IdmConceptRoleRequestDto();
		conceptRole.setIdentityContract(identityContact.getId());
		conceptRole.setRole(role.getId());
		conceptRole.setOperation(ConceptRoleRequestOperation.ADD);
		conceptRole.setRoleRequest(request.getId());
		conceptRole.getEavs().add(formInstance);
		conceptRole = conceptRoleService.save(conceptRole);

		IdmRoleRequestDto roleRequestDto = roleRequestService.startRequestInternal(request.getId(), false, true);
		assertEquals(RoleRequestState.EXECUTED, roleRequestDto.getState());
		
		conceptRole = conceptRoleService.get(conceptRole.getId());
		assertEquals(RoleRequestState.EXECUTED, conceptRole.getState());
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityContractId(identityContact.getId());
		
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(identityRoleFilter, null).getContent();
		assertEquals(1, identityRoles.size());
		
		IdmIdentityRoleDto identityRoleDto = identityRoles.get(0);
		IdmFormInstanceDto formInstanceDto = identityRoleService.getRoleAttributeValues(identityRoleDto);
		assertNotNull(formInstanceDto);
		List<IdmFormValueDto> values = formInstanceDto.getValues();
		
		assertEquals(1, values.size());
		assertEquals(formValue.getValue(), values.get(0).getValue());
	}
	
	@Test
	public void testChangeIdentityRoleValue() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto role = createRoleWithAttributes();
		IdmIdentityContractDto identityContact = getHelper().createContract(identity);
		IdmFormDefinitionDto definition = formService.getDefinition(role.getIdentityRoleAttributeDefinition());
		IdmFormAttributeDto ipAttributeDto = definition.getFormAttributes().stream() //
				.filter(attribute -> IP.equals(attribute.getCode())) //
				.findFirst() //
				.get(); //

		// Add value
		IdmFormValueDto originalFormValue = new IdmFormValueDto(ipAttributeDto);
		originalFormValue.setStringValue(getHelper().createName());
		originalFormValue.setPersistentType(PersistentType.TEXT);
		originalFormValue.setFormAttribute(ipAttributeDto.getId());

		IdmFormInstanceDto formInstance = new IdmFormInstanceDto();
		formInstance.setFormDefinition(definition);
		formInstance.getValues().add(originalFormValue);
		//
		IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
		identityRole.setIdentityContract(identityContact.getId());
		identityRole.setRole(role.getId());
		identityRole.getEavs().add(formInstance);

		identityRole = identityRoleService.save(identityRole);

		assertTrue(!identityRole.getEavs().isEmpty());
		formInstance = identityRoleService.getRoleAttributeValues(identityRole);
		assertEquals(definition, formInstance.getFormDefinition());
		assertEquals(originalFormValue.getStringValue(), formInstance.getValues().get(0).getStringValue());
		
		// Identity-role with value is created
		// Now we will changed it
		IdmFormValueDto formValueChanged = new IdmFormValueDto(ipAttributeDto);
		formValueChanged.setStringValue(getHelper().createName());
		formValueChanged.setPersistentType(PersistentType.TEXT);
		formValueChanged.setFormAttribute(ipAttributeDto.getId());

		IdmFormInstanceDto formInstanceChanged = new IdmFormInstanceDto();
		formInstanceChanged.setFormDefinition(definition);
		formInstanceChanged.getValues().add(formValueChanged);
		// Create request
		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicant(identity.getId());
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		request.setExecuteImmediately(true);
		request = roleRequestService.save(request);
		// Create concept
		IdmConceptRoleRequestDto conceptRole = new IdmConceptRoleRequestDto();
		conceptRole.setRole(role.getId());
		conceptRole.setOperation(ConceptRoleRequestOperation.UPDATE);
		conceptRole.setIdentityContract(identityContact.getId());
		conceptRole.setIdentityRole(identityRole.getId());
		conceptRole.setRoleRequest(request.getId());
		conceptRole.getEavs().add(formInstanceChanged);
		conceptRole = conceptRoleService.save(conceptRole);
		formInstanceChanged = conceptRoleService.getRoleAttributeValues(conceptRole, true);
		List<IdmFormValueDto> valuesChanged = formInstanceChanged.getValues();
		assertEquals(1, valuesChanged.size());
		IdmFormValueDto valueChanged = valuesChanged.get(0);
		// Value was changed
		assertTrue(valueChanged.isChanged());
		assertNotNull(valueChanged.getOriginalValue());
		// We have original value
		IdmFormValueDto originalValue = valueChanged.getOriginalValue();
		// Original and new value is not equals
		assertNotEquals(valueChanged.getValue(), originalValue.getValue());
		assertEquals( originalFormValue.getValue(),  originalValue.getValue());
		
		IdmRoleRequestDto roleRequestDto = roleRequestService.startRequestInternal(request.getId(), false, true);
		assertEquals(RoleRequestState.EXECUTED, roleRequestDto.getState());
		
		conceptRole = conceptRoleService.get(conceptRole.getId());
		assertEquals(RoleRequestState.EXECUTED, conceptRole.getState());
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityContractId(identityContact.getId());
		
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(identityRoleFilter, null).getContent();
		assertEquals(1, identityRoles.size());
		
		IdmIdentityRoleDto identityRoleDto = identityRoles.get(0);
		IdmFormInstanceDto formInstanceDto = identityRoleService.getRoleAttributeValues(identityRoleDto);
		assertNotNull(formInstanceDto);
		List<IdmFormValueDto> values = formInstanceDto.getValues();
		
		assertEquals(1, values.size());
		assertEquals(formValueChanged.getValue(), values.get(0).getValue());
	}
	
	@Test
	public void testRemoveIdentityRoleValue() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto role = createRoleWithAttributes(false);
		IdmIdentityContractDto identityContact = getHelper().createContract(identity);
		IdmFormDefinitionDto definition = formService.getDefinition(role.getIdentityRoleAttributeDefinition());
		IdmFormAttributeDto ipAttributeDto = definition.getFormAttributes().stream() //
				.filter(attribute -> IP.equals(attribute.getCode())) //
				.findFirst() //
				.get(); //

		// Add value
		IdmFormValueDto originalFormValue = new IdmFormValueDto(ipAttributeDto);
		originalFormValue.setStringValue(getHelper().createName());
		originalFormValue.setPersistentType(PersistentType.TEXT);
		originalFormValue.setFormAttribute(ipAttributeDto.getId());

		IdmFormInstanceDto formInstance = new IdmFormInstanceDto();
		formInstance.setFormDefinition(definition);
		formInstance.getValues().add(originalFormValue);
		//
		IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
		identityRole.setIdentityContract(identityContact.getId());
		identityRole.setRole(role.getId());
		identityRole.getEavs().add(formInstance);

		identityRole = identityRoleService.save(identityRole);

		assertTrue(!identityRole.getEavs().isEmpty());
		formInstance = identityRoleService.getRoleAttributeValues(identityRole);
		assertEquals(definition, formInstance.getFormDefinition());
		assertEquals(originalFormValue.getStringValue(), formInstance.getValues().get(0).getStringValue());
		
		// Identity-role with value is created 
		// Now we will remove it -> set value to null
		IdmFormValueDto formValueChanged = new IdmFormValueDto(ipAttributeDto);
		formValueChanged.setStringValue(null);
		formValueChanged.setPersistentType(PersistentType.TEXT);
		formValueChanged.setFormAttribute(ipAttributeDto.getId());

		IdmFormInstanceDto formInstanceChanged = new IdmFormInstanceDto();
		formInstanceChanged.setFormDefinition(definition);
		formInstanceChanged.getValues().add(formValueChanged);
		// Create request
		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicant(identity.getId());
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		request.setExecuteImmediately(true);
		request = roleRequestService.save(request);
		// Create concept
		IdmConceptRoleRequestDto conceptRole = new IdmConceptRoleRequestDto();
		conceptRole.setRole(role.getId());
		conceptRole.setOperation(ConceptRoleRequestOperation.UPDATE);
		conceptRole.setIdentityContract(identityContact.getId());
		conceptRole.setIdentityRole(identityRole.getId());
		conceptRole.setRoleRequest(request.getId());
		conceptRole.getEavs().add(formInstanceChanged);
		conceptRole = conceptRoleService.save(conceptRole);
		formInstanceChanged = conceptRoleService.getRoleAttributeValues(conceptRole, true);
		List<IdmFormValueDto> valuesChanged = formInstanceChanged.getValues();
		assertEquals(1, valuesChanged.size());
		IdmFormValueDto valueChanged = valuesChanged.get(0);
		// Value was changed
		assertTrue(valueChanged.isChanged());
		assertNotNull(valueChanged.getOriginalValue());
		// We have original value
		IdmFormValueDto originalValue = valueChanged.getOriginalValue();
		// Original and new value is not equals
		assertNotEquals(valueChanged.getValue(), originalValue.getValue());
		assertEquals( originalFormValue.getValue(),  originalValue.getValue());
		
		IdmRoleRequestDto roleRequestDto = roleRequestService.startRequestInternal(request.getId(), false, true);
		assertEquals(RoleRequestState.EXECUTED, roleRequestDto.getState());
		
		conceptRole = conceptRoleService.get(conceptRole.getId());
		assertEquals(RoleRequestState.EXECUTED, conceptRole.getState());
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityContractId(identityContact.getId());
		
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(identityRoleFilter, null).getContent();
		assertEquals(1, identityRoles.size());
		
		IdmIdentityRoleDto identityRoleDto = identityRoles.get(0);
		IdmFormInstanceDto formInstanceDto = identityRoleService.getRoleAttributeValues(identityRoleDto);
		assertNotNull(formInstanceDto);
		List<IdmFormValueDto> values = formInstanceDto.getValues();
		
		assertEquals(0, values.size());
	}
	
	
	private String getDetailRoleRequestUrl(Serializable backendId) {
		return String.format("%s/%s", BaseDtoController.BASE_PATH + "/role-requests", backendId);
	}
	
	private String getDetailConceptRoleRequestUrl(Serializable backendId) {
		return String.format("%s/%s", BaseDtoController.BASE_PATH + "/concept-role-requests", backendId);
	}
	
	private IdmRoleDto createRoleWithAttributes() {
		return this.createRoleWithAttributes(true);
	}
	
	private IdmRoleDto createRoleWithAttributes(boolean ipRequired ) {
		IdmRoleDto role = getHelper().createRole();
		assertNull(role.getIdentityRoleAttributeDefinition());
		
		IdmFormAttributeDto ipAttribute = new IdmFormAttributeDto(IP);
		ipAttribute.setPersistentType(PersistentType.TEXT);
		ipAttribute.setRequired(ipRequired);
		
		IdmFormDefinitionDto definition = formService.createDefinition(IdmIdentityRole.class, ImmutableList.of(ipAttribute));
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
