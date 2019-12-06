package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import java.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleFormAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFormAttributeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRequestIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleFormAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
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
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Integration tests with request-identity-role service
 * 
 * @author Vít Švanda
 *
 */
public class IdmRequestIdentityRoleServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private IdmRequestIdentityRoleService requestIdentityRoleService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private FormService formService;
	@Autowired
	private IdmRoleFormAttributeService roleFormAttributeService;

	private final static String IP = "IP";
	private final static String NUMBER_OF_FINGERS = "NUMBER_OF_FINGERS";

	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	@Transactional
	public void testAssignRole() {
		IdmIdentityDto identity = this.getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		IdmRoleDto role = this.getHelper().createRole();

		// Create request for new identity-role
		IdmRequestIdentityRoleDto dto = new IdmRequestIdentityRoleDto();
		dto.setIdentityContract(contract.getId());
		dto.setRole(role.getId());
		dto.setValidFrom(LocalDate.now().minusDays(1));
		dto.setValidTill(LocalDate.now().plusDays(10));

		IdmRequestIdentityRoleDto createdRequestIdentityRole = requestIdentityRoleService.save(dto);

		Assert.assertNotNull(createdRequestIdentityRole);
		// Request must been created
		Assert.assertNotNull(createdRequestIdentityRole.getRoleRequest());
		Assert.assertEquals(role.getId(), createdRequestIdentityRole.getRole());
		Assert.assertEquals(contract.getId(), createdRequestIdentityRole.getIdentityContract());

		IdmRoleRequestDto request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest());
		Assert.assertNotNull(request);
		// Concepts are empty, because the request does not return them be default
		Assert.assertEquals(0, request.getConceptRoles().size());
		request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest(), new IdmRoleRequestFilter(true));
		Assert.assertNotNull(request);
		// Concepts are not empty now
		Assert.assertEquals(1, request.getConceptRoles().size());
		// Applicant must be ours identity
		Assert.assertEquals(contract.getIdentity(), request.getApplicant());

		IdmConceptRoleRequestDto concept = request.getConceptRoles().get(0);
		Assert.assertEquals(contract.getId(), concept.getIdentityContract());
		Assert.assertEquals(role.getId(), concept.getRole());
		Assert.assertEquals(createdRequestIdentityRole.getValidFrom(), concept.getValidFrom());
		Assert.assertEquals(createdRequestIdentityRole.getValidTill(), concept.getValidTill());

		this.getHelper().executeRequest(request, false, true);
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(1, identityRoles.size());
		Assert.assertEquals(role.getId(), identityRoles.get(0).getRole());
		Assert.assertEquals(createdRequestIdentityRole.getValidFrom(), identityRoles.get(0).getValidFrom());
		Assert.assertEquals(createdRequestIdentityRole.getValidTill(), identityRoles.get(0).getValidTill());
	}

	@Test
	@Transactional
	public void testAssignRoles() {
		IdmIdentityDto identity = this.getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		IdmRoleDto role = this.getHelper().createRole();
		IdmRoleDto roleTwo = this.getHelper().createRole();
		IdmRoleDto roleThree = this.getHelper().createRole();
		Set<UUID> roles = Sets.newSet(role.getId(), roleTwo.getId(), roleThree.getId());

		// Create request for new identity-role
		IdmRequestIdentityRoleDto dto = new IdmRequestIdentityRoleDto();
		dto.setIdentityContract(contract.getId());
		dto.setRole(role.getId());
		dto.setRoles(Sets.newSet(roleTwo.getId(), roleThree.getId()));
		dto.setValidFrom(LocalDate.now().minusDays(1));
		dto.setValidTill(LocalDate.now().plusDays(10));

		IdmRequestIdentityRoleDto createdRequestIdentityRole = requestIdentityRoleService.save(dto);

		Assert.assertNotNull(createdRequestIdentityRole);
		// Request must been created
		Assert.assertNotNull(createdRequestIdentityRole.getRoleRequest());
		Assert.assertEquals(contract.getId(), createdRequestIdentityRole.getIdentityContract());

		IdmRoleRequestDto request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest());
		Assert.assertNotNull(request);
		// Concepts are empty, because the request does not return them be default
		Assert.assertEquals(0, request.getConceptRoles().size());
		request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest(), new IdmRoleRequestFilter(true));
		Assert.assertNotNull(request);
		// Concepts are not empty now
		Assert.assertEquals(3, request.getConceptRoles().size());
		// Applicant must be ours identity
		Assert.assertEquals(contract.getIdentity(), request.getApplicant());

		request.getConceptRoles().forEach(concept -> {
			Assert.assertEquals(contract.getId(), concept.getIdentityContract());
			Assert.assertTrue(roles.contains(concept.getRole()));
			Assert.assertEquals(createdRequestIdentityRole.getValidFrom(), concept.getValidFrom());
			Assert.assertEquals(createdRequestIdentityRole.getValidTill(), concept.getValidTill());
		});

		this.getHelper().executeRequest(request, false, true);
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(3, identityRoles.size());
		identityRoles.forEach(identityRole -> {
			Assert.assertTrue(roles.contains(identityRole.getRole()));
			Assert.assertEquals(createdRequestIdentityRole.getValidFrom(), identityRole.getValidFrom());
			Assert.assertEquals(createdRequestIdentityRole.getValidTill(), identityRole.getValidTill());
		});
	}

	@Test
	@Transactional
	public void testAssignRemove() {
		IdmIdentityDto identity = this.getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		IdmRoleDto role = this.getHelper().createRole();
		this.getHelper().createIdentityRole(contract, role);

		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(1, identityRoles.size());

		// Create request for remove identity-role
		IdmRequestIdentityRoleDto dto = new IdmRequestIdentityRoleDto();
		dto.setIdentityContract(contract.getId());
		dto.setIdentityRole(identityRoles.get(0).getId());
		dto.setId(dto.getIdentityRole());

		IdmRequestIdentityRoleDto createdRequestIdentityRole = requestIdentityRoleService
				.deleteRequestIdentityRole(dto);

		Assert.assertNotNull(createdRequestIdentityRole);
		// Request must been created
		Assert.assertNotNull(createdRequestIdentityRole.getRoleRequest());
		Assert.assertEquals(role.getId(), createdRequestIdentityRole.getRole());
		Assert.assertEquals(contract.getId(), createdRequestIdentityRole.getIdentityContract());

		IdmRoleRequestDto request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest(),
				new IdmRoleRequestFilter(true));
		Assert.assertNotNull(request);
		// Concepts are not empty now
		Assert.assertEquals(1, request.getConceptRoles().size());
		// Applicant must be ours identity
		Assert.assertEquals(contract.getIdentity(), request.getApplicant());

		IdmConceptRoleRequestDto concept = request.getConceptRoles().get(0);
		Assert.assertEquals(contract.getId(), concept.getIdentityContract());
		Assert.assertEquals(role.getId(), concept.getRole());
		Assert.assertEquals(ConceptRoleRequestOperation.REMOVE, concept.getOperation());

		this.getHelper().executeRequest(request, false, true);
		identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(0, identityRoles.size());
	}

	@Test
	@Transactional
	public void testUpdateAssignRole() {
		IdmIdentityDto identity = this.getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		IdmRoleDto role = this.getHelper().createRole();
		this.getHelper().createIdentityRole(contract, role, LocalDate.now().minusDays(1), null);

		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(1, identityRoles.size());

		// Create request for updated identity-role
		IdmRequestIdentityRoleDto dto = new IdmRequestIdentityRoleDto();
		dto.setIdentityContract(contract.getId());
		dto.setIdentityRole(identityRoles.get(0).getId());
		dto.setId(dto.getIdentityRole());
		dto.setValidFrom(LocalDate.now().minusDays(10));
		dto.setValidTill(LocalDate.now().plusDays(10));

		IdmRequestIdentityRoleDto createdRequestIdentityRole = requestIdentityRoleService.save(dto);

		Assert.assertNotNull(createdRequestIdentityRole);
		// Request must been created
		Assert.assertNotNull(createdRequestIdentityRole.getRoleRequest());
		Assert.assertEquals(role.getId(), createdRequestIdentityRole.getRole());
		Assert.assertEquals(contract.getId(), createdRequestIdentityRole.getIdentityContract());

		IdmRoleRequestDto request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest(),
				new IdmRoleRequestFilter(true));
		Assert.assertNotNull(request);
		// Concepts are not empty now
		Assert.assertEquals(1, request.getConceptRoles().size());
		// Applicant must be ours identity
		Assert.assertEquals(contract.getIdentity(), request.getApplicant());

		IdmConceptRoleRequestDto concept = request.getConceptRoles().get(0);
		Assert.assertEquals(contract.getId(), concept.getIdentityContract());
		Assert.assertEquals(role.getId(), concept.getRole());
		Assert.assertEquals(ConceptRoleRequestOperation.UPDATE, concept.getOperation());
		Assert.assertEquals(createdRequestIdentityRole.getValidFrom(), concept.getValidFrom());
		Assert.assertEquals(createdRequestIdentityRole.getValidTill(), concept.getValidTill());

		this.getHelper().executeRequest(request, false, true);
		identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(1, identityRoles.size());
		Assert.assertEquals(createdRequestIdentityRole.getValidFrom(), identityRoles.get(0).getValidFrom());
		Assert.assertEquals(createdRequestIdentityRole.getValidTill(), identityRoles.get(0).getValidTill());
	}

	@Test
	@Transactional
	public void testUpdateAddingConcept() {
		IdmIdentityDto identity = this.getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		IdmRoleDto role = this.getHelper().createRole();

		// Create request for new identity-role
		IdmRequestIdentityRoleDto dto = new IdmRequestIdentityRoleDto();
		dto.setIdentityContract(contract.getId());
		dto.setRole(role.getId());

		IdmRequestIdentityRoleDto createdRequestIdentityRole = requestIdentityRoleService.save(dto);

		// We want to update created concept -> update validity
		createdRequestIdentityRole.setValidFrom(LocalDate.now().minusDays(1));
		createdRequestIdentityRole.setValidTill(LocalDate.now().plusDays(10));
		createdRequestIdentityRole = requestIdentityRoleService.save(createdRequestIdentityRole);

		Assert.assertNotNull(createdRequestIdentityRole);
		// Request must been created
		Assert.assertNotNull(createdRequestIdentityRole.getRoleRequest());
		Assert.assertEquals(role.getId(), createdRequestIdentityRole.getRole());
		Assert.assertEquals(contract.getId(), createdRequestIdentityRole.getIdentityContract());

		IdmRoleRequestDto request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest(),
				new IdmRoleRequestFilter(true));
		Assert.assertNotNull(request);
		// Concepts are not empty now
		Assert.assertEquals(1, request.getConceptRoles().size());
		// Applicant must be ours identity
		Assert.assertEquals(contract.getIdentity(), request.getApplicant());

		IdmConceptRoleRequestDto concept = request.getConceptRoles().get(0);
		Assert.assertEquals(contract.getId(), concept.getIdentityContract());
		Assert.assertEquals(role.getId(), concept.getRole());
		Assert.assertEquals(createdRequestIdentityRole.getValidFrom(), concept.getValidFrom());
		Assert.assertEquals(createdRequestIdentityRole.getValidTill(), concept.getValidTill());
		Assert.assertEquals(ConceptRoleRequestOperation.ADD, concept.getOperation());

		this.getHelper().executeRequest(request, false, true);
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(1, identityRoles.size());
		Assert.assertEquals(role.getId(), identityRoles.get(0).getRole());
		Assert.assertEquals(LocalDate.now().minusDays(1), identityRoles.get(0).getValidFrom());
		Assert.assertEquals(LocalDate.now().plusDays(10), identityRoles.get(0).getValidTill());
	}

	@Test
	@Transactional
	public void testUpdateUpdatingConcept() {
		IdmIdentityDto identity = this.getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		IdmRoleDto role = this.getHelper().createRole();
		this.getHelper().createIdentityRole(contract, role, LocalDate.now().minusDays(1), null);

		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(1, identityRoles.size());

		// Create request for updated identity-role
		IdmRequestIdentityRoleDto dto = new IdmRequestIdentityRoleDto();
		dto.setIdentityContract(contract.getId());
		dto.setIdentityRole(identityRoles.get(0).getId());
		dto.setId(dto.getIdentityRole());
		dto.setValidFrom(LocalDate.now().plusDays(5));
		dto.setValidTill(LocalDate.now().minusDays(10));

		IdmRequestIdentityRoleDto createdRequestIdentityRole = requestIdentityRoleService.save(dto);
		Assert.assertEquals(LocalDate.now().plusDays(5), createdRequestIdentityRole.getValidFrom());
		Assert.assertEquals(LocalDate.now().minusDays(10), createdRequestIdentityRole.getValidTill());

		// We want to update created concept -> update validity
		createdRequestIdentityRole.setValidFrom(LocalDate.now().minusDays(1));
		createdRequestIdentityRole.setValidTill(LocalDate.now().plusDays(10));
		createdRequestIdentityRole = requestIdentityRoleService.save(createdRequestIdentityRole);

		Assert.assertNotNull(createdRequestIdentityRole);
		// Request must been created
		Assert.assertNotNull(createdRequestIdentityRole.getRoleRequest());
		Assert.assertEquals(role.getId(), createdRequestIdentityRole.getRole());
		Assert.assertEquals(contract.getId(), createdRequestIdentityRole.getIdentityContract());

		IdmRoleRequestDto request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest(),
				new IdmRoleRequestFilter(true));
		Assert.assertNotNull(request);
		// Concepts are not empty now
		Assert.assertEquals(1, request.getConceptRoles().size());
		// Applicant must be ours identity
		Assert.assertEquals(contract.getIdentity(), request.getApplicant());

		IdmConceptRoleRequestDto concept = request.getConceptRoles().get(0);
		Assert.assertEquals(contract.getId(), concept.getIdentityContract());
		Assert.assertEquals(role.getId(), concept.getRole());
		Assert.assertEquals(ConceptRoleRequestOperation.UPDATE, concept.getOperation());
		Assert.assertEquals(createdRequestIdentityRole.getValidFrom(), concept.getValidFrom());
		Assert.assertEquals(createdRequestIdentityRole.getValidTill(), concept.getValidTill());

		this.getHelper().executeRequest(request, false, true);
		identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(1, identityRoles.size());
		Assert.assertEquals(LocalDate.now().minusDays(1), identityRoles.get(0).getValidFrom());
		Assert.assertEquals(LocalDate.now().plusDays(10), identityRoles.get(0).getValidTill());
	}

	@Test
	@Transactional
	public void testRemoveAddingConcept() {
		IdmIdentityDto identity = this.getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		IdmRoleDto role = this.getHelper().createRole();

		// Create request for new identity-role
		IdmRequestIdentityRoleDto dto = new IdmRequestIdentityRoleDto();
		dto.setIdentityContract(contract.getId());
		dto.setRole(role.getId());
		dto.setValidFrom(LocalDate.now().minusDays(1));
		dto.setValidTill(LocalDate.now().plusDays(10));

		IdmRequestIdentityRoleDto createdRequestIdentityRole = requestIdentityRoleService.save(dto);

		Assert.assertNotNull(createdRequestIdentityRole);
		// Request must been created
		Assert.assertNotNull(createdRequestIdentityRole.getRoleRequest());
		Assert.assertEquals(role.getId(), createdRequestIdentityRole.getRole());
		Assert.assertEquals(contract.getId(), createdRequestIdentityRole.getIdentityContract());

		IdmRoleRequestDto request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest(),
				new IdmRoleRequestFilter(true));
		Assert.assertNotNull(request);
		// Concepts are not empty now
		Assert.assertEquals(1, request.getConceptRoles().size());
		// Applicant must be ours identity
		Assert.assertEquals(contract.getIdentity(), request.getApplicant());

		// Delete adding concept
		requestIdentityRoleService.deleteRequestIdentityRole(createdRequestIdentityRole);

		request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest(), new IdmRoleRequestFilter(true));
		Assert.assertNotNull(request);
		// Concepts are empty now
		Assert.assertEquals(0, request.getConceptRoles().size());
	}

	@Test
	@Transactional
	public void testFind() {
		IdmIdentityDto identity = this.getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		IdmRoleDto assignedRole = this.getHelper().createRole();
		IdmIdentityRoleDto identityRole = this.getHelper().createIdentityRole(contract, assignedRole);
		IdmRoleDto role = this.getHelper().createRole();

		IdmRequestIdentityRoleFilter filter = new IdmRequestIdentityRoleFilter();
		filter.setIdentityId(identity.getId());

		// We expecting only one already assigned identity-role
		List<IdmRequestIdentityRoleDto> requestIdentityRoles = requestIdentityRoleService.find(filter, null)
				.getContent();
		Assert.assertEquals(1, requestIdentityRoles.size());
		Assert.assertEquals(identityRole.getId(), requestIdentityRoles.get(0).getId());

		// Create request for new identity-role
		IdmRequestIdentityRoleDto dto = new IdmRequestIdentityRoleDto();
		dto.setIdentityContract(contract.getId());
		dto.setRole(role.getId());
		dto.setValidFrom(LocalDate.now().minusDays(1));
		dto.setValidTill(LocalDate.now().plusDays(10));

		IdmRequestIdentityRoleDto createdRequestIdentityRole = requestIdentityRoleService.save(dto);
		Assert.assertNotNull(createdRequestIdentityRole);
		// Request must been created
		Assert.assertNotNull(createdRequestIdentityRole.getRoleRequest());
		// Filter will be filtering by this request
		filter.setRoleRequestId(createdRequestIdentityRole.getRoleRequest());

		// We expecting two items, one assigned identity-role and one adding concept
		requestIdentityRoles = requestIdentityRoleService.find(filter, null).getContent();
		Assert.assertEquals(2, requestIdentityRoles.size());

		IdmRequestIdentityRoleDto addingConcept = requestIdentityRoles.stream()
				.filter(requestIdentityRole -> ConceptRoleRequestOperation.ADD == requestIdentityRole.getOperation())
				.findFirst().orElse(null);
		Assert.assertNotNull(addingConcept);
		Assert.assertEquals(createdRequestIdentityRole.getRoleRequest(), addingConcept.getRoleRequest());
		Assert.assertEquals(role.getId(), addingConcept.getRole());
		Assert.assertEquals(dto.getValidFrom(), addingConcept.getValidFrom());
		Assert.assertEquals(dto.getValidTill(), addingConcept.getValidTill());

		// Create request for remove identity-role
		IdmRequestIdentityRoleDto dtoForRemove = new IdmRequestIdentityRoleDto();
		dtoForRemove.setRoleRequest(createdRequestIdentityRole.getRoleRequest());
		dtoForRemove.setIdentityRole(identityRole.getId());
		dtoForRemove.setId(identityRole.getId());

		// Remove existing identity-role -> new removing concept
		IdmRequestIdentityRoleDto deleteRequestIdentityRole = requestIdentityRoleService
				.deleteRequestIdentityRole(dtoForRemove);
		Assert.assertEquals(createdRequestIdentityRole.getRoleRequest(), deleteRequestIdentityRole.getRoleRequest());

		// We expecting two items, one adding concept and one removing concept
		requestIdentityRoles = requestIdentityRoleService.find(filter, null).getContent();
		Assert.assertEquals(2, requestIdentityRoles.size());

		IdmRequestIdentityRoleDto removingConcept = requestIdentityRoles.stream()
				.filter(requestIdentityRole -> ConceptRoleRequestOperation.REMOVE == requestIdentityRole.getOperation())
				.findFirst().orElse(null);
		Assert.assertNotNull(removingConcept);
		Assert.assertEquals(createdRequestIdentityRole.getRoleRequest(), removingConcept.getRoleRequest());
		Assert.assertEquals(identityRole.getId(), removingConcept.getIdentityRole());
		Assert.assertNotEquals(removingConcept.getId(), removingConcept.getIdentityRole());

	}

	@Test
	public void testFindAddingConceptWithEAVs() {

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

		// Create request identity-role
		IdmRequestIdentityRoleDto createdRequestIdentityRole = new IdmRequestIdentityRoleDto();
		createdRequestIdentityRole.setIdentityContract(contract.getId());
		// Change the valid from
		createdRequestIdentityRole.setValidFrom(LocalDate.now());
		createdRequestIdentityRole.setRole(role.getId());

		// Create role attribute value in concept

		IdmFormDefinitionDto formDefinitionDto = roleService.getFormAttributeSubdefinition(role);
		IdmFormInstanceDto formInstanceDto = new IdmFormInstanceDto();

		IdmFormAttributeDto attribute = formDefinitionDto.getMappedAttributeByCode(NUMBER_OF_FINGERS);
		IdmFormValueDto formValueDto = new IdmFormValueDto(attribute);
		formValueDto.setValue(BigDecimal.TEN);
		List<IdmFormValueDto> values = Lists.newArrayList(formValueDto);
		formInstanceDto.setValues(values);
		List<IdmFormInstanceDto> forms = Lists.newArrayList(formInstanceDto);
		createdRequestIdentityRole.setEavs(forms);
		createdRequestIdentityRole = requestIdentityRoleService.save(createdRequestIdentityRole);

		IdmRoleRequestDto request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest(),
				new IdmRoleRequestFilter(true));
		Assert.assertNotNull(request);

		IdmRequestIdentityRoleFilter filterRequestIdentityRole = new IdmRequestIdentityRoleFilter();
		filterRequestIdentityRole.setIdentityId(identity.getId());
		filterRequestIdentityRole.setRoleRequestId(request.getId());
		// Include EAV attributes
		filterRequestIdentityRole.setIncludeEav(true);

		// Check EAV value in the request-identity-role
		List<IdmRequestIdentityRoleDto> requestIdentityRoles = requestIdentityRoleService
				.find(filterRequestIdentityRole, null).getContent();
		Assert.assertEquals(1, requestIdentityRoles.size());
		Assert.assertEquals(role.getId(), requestIdentityRoles.get(0).getRole());
		Assert.assertEquals(1, requestIdentityRoles.get(0).getEavs().size());
		Assert.assertEquals(1, requestIdentityRoles.get(0).getEavs().get(0).getValues().size());
		Serializable value = requestIdentityRoles.get(0).getEavs().get(0).getValues().get(0).getValue();
		Assert.assertEquals(BigDecimal.TEN.longValue(), ((BigDecimal)value).longValue());
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
