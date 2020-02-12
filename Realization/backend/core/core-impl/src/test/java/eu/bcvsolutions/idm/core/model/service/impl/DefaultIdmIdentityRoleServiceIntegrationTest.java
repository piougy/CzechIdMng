package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.DuplicateRolesDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
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
import eu.bcvsolutions.idm.core.eav.entity.IdmFormValue_;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Assigned roles integration tests
 * - referential integrity
 * - role deduplicate on role requests
 * 
 * @see DefaultIdmIdentityRoleServiceUnitTest
 * @author Radek Tomiška
 * @author Ondrej Kopr
 */
public class DefaultIdmIdentityRoleServiceIntegrationTest extends AbstractIntegrationTest {

	private static String ATTRIBUTE_ONE = "attrOne";
	private static Long ATTRIBUTE_ONE_DEFAULT_VALUE = 1122337788l;
	
	@Autowired private ApplicationContext context;
	@Autowired private FormService formService;
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmRoleFormAttributeService roleFormAttributeService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	//
	private DefaultIdmIdentityRoleService service;

	@Before
	public void init() {
		service = context.getAutowireCapableBeanFactory().createBean(DefaultIdmIdentityRoleService.class);
	}
	
	@After
	public void logout() {
		automaticRoleAttributeService.find(null).forEach(autoRole -> {
			automaticRoleAttributeService.delete(autoRole);
		});
	}
	
	@Test
	@Transactional
	public void testReferentialIntegrityDirectRole() {
		IdmIdentityContractDto contract = getHelper().getPrimeContract(getHelper().createIdentity().getId());
		
		IdmRoleDto directRoleOne = getHelper().createRole(); 
		IdmRoleDto subRoleOne = getHelper().createRole();
		IdmRoleDto directRoleTwo = getHelper().createRole(); 
		//
		IdmIdentityRoleDto directIdentityRoleOne = new IdmIdentityRoleDto();
		directIdentityRoleOne.setIdentityContract(contract.getId());
		directIdentityRoleOne.setRole(directRoleOne.getId());
		directIdentityRoleOne = service.save(directIdentityRoleOne);
		//
		IdmIdentityRoleDto subIdentityRoleOne = new IdmIdentityRoleDto();
		subIdentityRoleOne.setIdentityContract(contract.getId());
		subIdentityRoleOne.setRole(subRoleOne.getId());
		subIdentityRoleOne.setDirectRole(directIdentityRoleOne.getId());
		subIdentityRoleOne = service.save(subIdentityRoleOne);
		//
		IdmIdentityRoleDto otherIdentityRoleOne = new IdmIdentityRoleDto();
		otherIdentityRoleOne.setIdentityContract(contract.getId());
		otherIdentityRoleOne.setRole(directRoleTwo.getId());
		otherIdentityRoleOne = service.save(otherIdentityRoleOne);
		//
		// check after create
		List<IdmIdentityRoleDto> assignedRoles = service.findAllByContract(contract.getId());
		Assert.assertEquals(3, assignedRoles.size());
		//
		// delete direct role
		service.delete(directIdentityRoleOne);
		assignedRoles = service.findAllByContract(contract.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(directRoleTwo.getId())));
	}

	@Test
	public void testDeduplicationWithConceptAndEav() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		IdmRoleDto role = createRoleWithAttributes(prepareAttributeOne());

		IdmIdentityRoleDto one = getHelper().createIdentityRole(identity, role, LocalDate.now().minusDays(10), LocalDate.now().plusDays(10));
		IdmIdentityRoleDto two = getHelper().createIdentityRole(identity, role, LocalDate.now().plusDays(15), LocalDate.now().plusDays(40));

		one = fillEavs(one);
		two = fillEavs(two);

		List<IdmFormInstanceDto> formInstances = service.getFormInstances(one);
		IdmFormValueDto valueFromOne = formInstances.get(0).getValues().get(0);
		IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(valueFromOne, IdmFormValue_.formAttribute, IdmFormAttributeDto.class);

		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		assertNull(duplicated);

		List<IdmConceptRoleRequestDto> concepts = new ArrayList<IdmConceptRoleRequestDto>();
		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setRole(role.getId());
		concept.setIdentityContract(contract.getId());
		concept.setValidFrom(LocalDate.now().minusDays(9));
		concept.setValidTill(LocalDate.now().plusDays(9));
		concept.setOperation(ConceptRoleRequestOperation.ADD);
		Map<String, BaseDto> embedded = concept.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		concept.setEmbedded(embedded);
		concepts.add(concept);
		
		List<IdmConceptRoleRequestDto> removeDuplicities = roleRequestService.removeDuplicities(concepts, identity.getId());
		assertFalse(removeDuplicities.isEmpty()); // EAVs for concept missing

		concepts = new ArrayList<IdmConceptRoleRequestDto>();

		IdmFormInstanceDto instance = new IdmFormInstanceDto();
		IdmFormValueDto value = new IdmFormValueDto(formAttributeDto);
		value.setValue(Long.valueOf(System.currentTimeMillis()));
		instance.setValues(Lists.newArrayList(value));
		concept.setEavs(Lists.newArrayList(instance));
		concepts.add(concept);

		removeDuplicities = roleRequestService.removeDuplicities(concepts, identity.getId());
		assertFalse(removeDuplicities.isEmpty()); // EAVs for concept is different

		concepts = new ArrayList<IdmConceptRoleRequestDto>();
		value = new IdmFormValueDto(formAttributeDto);
		value.setValue(ATTRIBUTE_ONE_DEFAULT_VALUE);
		instance.setValues(Lists.newArrayList(value));
		concept.setEavs(Lists.newArrayList(instance));
		concepts.add(concept);

		removeDuplicities = roleRequestService.removeDuplicities(concepts, identity.getId());
		assertTrue(removeDuplicities.isEmpty()); // EAVs are same
	}

	@Test
	public void testDeduplicationWithConcept() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		IdmRoleDto role = getHelper().createRole();

		IdmIdentityRoleDto one = getHelper().createIdentityRole(identity, role, LocalDate.now().minusDays(10), LocalDate.now().plusDays(10));
		IdmIdentityRoleDto two = getHelper().createIdentityRole(identity, role, LocalDate.now().plusDays(15), LocalDate.now().plusDays(40));

		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		assertNull(duplicated);

		List<IdmConceptRoleRequestDto> concepts = new ArrayList<IdmConceptRoleRequestDto>();
		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setRole(role.getId());
		concept.setIdentityContract(contract.getId());
		concept.setValidFrom(LocalDate.now().minusDays(9));
		concept.setValidTill(LocalDate.now().plusDays(9));
		concept.setOperation(ConceptRoleRequestOperation.ADD);
		Map<String, BaseDto> embedded = concept.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		concept.setEmbedded(embedded);
		concepts.add(concept);

		List<IdmConceptRoleRequestDto> removeDuplicities = roleRequestService.removeDuplicities(concepts, identity.getId());
		assertTrue(removeDuplicities.isEmpty()); // concept is duplicit with one

		concepts = new ArrayList<IdmConceptRoleRequestDto>();
		concept.setRole(UUID.randomUUID());
		concept.setDuplicate(null);
		concept.setDuplicates(null);
		concepts.add(concept);

		removeDuplicities = roleRequestService.removeDuplicities(concepts, identity.getId());
		assertFalse(removeDuplicities.isEmpty()); // Role is different

		concepts = new ArrayList<IdmConceptRoleRequestDto>();
		concept.setRole(role.getId());
		concept.setValidFrom(LocalDate.now().plusDays(50));
		concept.setValidTill(LocalDate.now().plusDays(60));
		concept.setDuplicate(null);
		concept.setDuplicates(null);
		concepts.add(concept);

		removeDuplicities = roleRequestService.removeDuplicities(concepts, identity.getId());
		assertFalse(removeDuplicities.isEmpty());
	}

	@Test
	public void testDeduplicationWithConceptWithRemovedRole() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		IdmRoleDto role = getHelper().createRole();

		IdmIdentityRoleDto one = getHelper().createIdentityRole(identity, role, null, null);


		List<IdmConceptRoleRequestDto> concepts = new ArrayList<IdmConceptRoleRequestDto>();
		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setRole(role.getId());
		concept.setIdentityContract(contract.getId());
		concept.setOperation(ConceptRoleRequestOperation.ADD);
		Map<String, BaseDto> embedded = concept.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		concept.setEmbedded(embedded);
		concepts.add(concept);

		List<IdmConceptRoleRequestDto> removeDuplicities = roleRequestService.removeDuplicities(concepts, identity.getId());
		assertTrue(removeDuplicities.isEmpty()); // concept is duplicit with one

		concepts = new ArrayList<IdmConceptRoleRequestDto>();
		concept.setDuplicate(null);
		concept.setDuplicates(null);
		concepts.add(concept);
		
		IdmConceptRoleRequestDto conceptWithremove = new IdmConceptRoleRequestDto();
		conceptWithremove.setOperation(ConceptRoleRequestOperation.REMOVE);
		conceptWithremove.setIdentityContract(contract.getId());
		conceptWithremove.setIdentityRole(one.getId());
		embedded = conceptWithremove.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		conceptWithremove.setEmbedded(embedded);
		concepts.add(conceptWithremove);

		removeDuplicities = roleRequestService.removeDuplicities(concepts, identity.getId());
		assertFalse(removeDuplicities.isEmpty()); // role will be removed
		assertEquals(2, removeDuplicities.size());
	}

	@Test
	public void deduplicationConceptSameIdentityRole() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());

		IdmRoleDto role = createRoleWithAttributes();

		getHelper().createIdentityRole(identity, role, null, null);
		 getHelper().createIdentityRole(identity, role, null, null);

		List<IdmConceptRoleRequestDto> duplicities = roleRequestService.removeDuplicities(new ArrayList<IdmConceptRoleRequestDto>(), identity.getId());
		assertTrue(duplicities.isEmpty()); // Deduplication with concept doesn't check current identity roles
	}

	@Test
	public void testDeduplicationConceptWithConcept() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		IdmRoleDto role = getHelper().createRole();

		List<IdmConceptRoleRequestDto> concepts = new ArrayList<IdmConceptRoleRequestDto>();
		IdmConceptRoleRequestDto conceptOne = new IdmConceptRoleRequestDto();
		conceptOne.setRole(role.getId());
		conceptOne.setIdentityContract(contract.getId());
		conceptOne.setOperation(ConceptRoleRequestOperation.ADD);
		Map<String, BaseDto> embedded = conceptOne.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		conceptOne.setEmbedded(embedded);
		concepts.add(conceptOne);
		
		IdmConceptRoleRequestDto conceptTwo = new IdmConceptRoleRequestDto();
		conceptTwo.setRole(role.getId());
		conceptTwo.setIdentityContract(contract.getId());
		conceptTwo.setOperation(ConceptRoleRequestOperation.ADD);
		embedded = conceptTwo.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		conceptTwo.setEmbedded(embedded);
		concepts.add(conceptTwo);

		List<IdmConceptRoleRequestDto> removeDuplicities = roleRequestService.removeDuplicities(concepts, identity.getId());
		assertFalse(removeDuplicities.isEmpty()); // concept is duplicit with another concept
		assertEquals(1, removeDuplicities.size());
		assertEquals(2, concepts.size());
	}

	@Test
	public void testDeduplicationConceptWithConcepts() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		IdmRoleDto role = getHelper().createRole();

		List<IdmConceptRoleRequestDto> concepts = new ArrayList<IdmConceptRoleRequestDto>();
		for (int index = 0; index < 9; index++) {
			IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
			concept.setRole(role.getId());
			concept.setIdentityContract(contract.getId());
			concept.setOperation(ConceptRoleRequestOperation.ADD);
			Map<String, BaseDto> embedded = concept.getEmbedded();
			embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
			concept.setEmbedded(embedded);
			concepts.add(concept);
		}
		
		assertEquals(9, concepts.size());

		List<IdmConceptRoleRequestDto> removeDuplicities = roleRequestService.removeDuplicities(concepts, identity.getId());
		assertFalse(removeDuplicities.isEmpty());
		assertEquals(1, removeDuplicities.size());
		assertEquals(9, concepts.size());
	}

	@Test
	public void testDeduplicationConceptWithConceptsAndIncrementDate() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		IdmRoleDto role = getHelper().createRole();

		List<IdmConceptRoleRequestDto> concepts = new ArrayList<IdmConceptRoleRequestDto>();
		for (int index = 10; index < 19; index++) {
			IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
			concept.setRole(role.getId());
			concept.setIdentityContract(contract.getId());
			concept.setOperation(ConceptRoleRequestOperation.ADD);
			concept.setValidTill(LocalDate.now().plusDays(index));
			Map<String, BaseDto> embedded = concept.getEmbedded();
			embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
			concept.setEmbedded(embedded);
			concepts.add(concept);
		}
		
		assertEquals(9, concepts.size());

		List<IdmConceptRoleRequestDto> removeDuplicities = roleRequestService.removeDuplicities(concepts, identity.getId());
		assertFalse(removeDuplicities.isEmpty());
		assertEquals(1, removeDuplicities.size());
		assertEquals(9, concepts.size());
		
		IdmConceptRoleRequestDto conceptRoleRequestDto = removeDuplicities.get(0);
		assertEquals(LocalDate.now().plusDays(18), conceptRoleRequestDto.getValidTill());
	}

	@Test
	public void testMarkDuplicatesOneConcept() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString)null);
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityContractDto contract = getHelper().createIdentityContact(identity);

		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setId(UUID.randomUUID());
		concept.setRole(role.getId());
		concept.setIdentityContract(contract.getId());
		Map<String, BaseDto> embedded = concept.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		concept.setEmbedded(embedded);
		concept.setOperation(ConceptRoleRequestOperation.ADD);
		concept.setValidFrom(LocalDate.now().minusDays(5));
		concept.setValidTill(LocalDate.now().plusDays(5));

		List<IdmConceptRoleRequestDto> duplicates = roleRequestService.markDuplicates(Lists.newArrayList(concept), Lists.newArrayList());
		assertEquals(1, duplicates.size());
		assertEquals(concept.getId(), duplicates.get(0).getId());
		assertTrue(duplicates.get(0) == concept);
	}

	@Test
	public void testMarkDuplicatesBetweenConcepts() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString)null);
		IdmRoleDto role = getHelper().createRole();
		IdmRoleDto secondRole = getHelper().createRole();
		IdmIdentityContractDto contract = getHelper().createIdentityContact(identity);
		UUID roleId = role.getId();
		UUID contractId = contract.getId();

		IdmConceptRoleRequestDto conceptOne = new IdmConceptRoleRequestDto();
		conceptOne.setId(UUID.randomUUID());
		conceptOne.setRole(roleId);
		conceptOne.setIdentityContract(contractId);
		Map<String, BaseDto> embedded = conceptOne.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		conceptOne.setEmbedded(embedded);
		conceptOne.setOperation(ConceptRoleRequestOperation.ADD);
		conceptOne.setValidFrom(LocalDate.now().minusDays(20));
		conceptOne.setValidTill(LocalDate.now().plusDays(20));

		IdmConceptRoleRequestDto conceptTwo = new IdmConceptRoleRequestDto();
		conceptTwo.setId(UUID.randomUUID());
		conceptTwo.setRole(roleId);
		embedded = conceptTwo.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		conceptTwo.setEmbedded(embedded);
		conceptTwo.setIdentityContract(contractId);
		conceptTwo.setOperation(ConceptRoleRequestOperation.ADD);
		conceptTwo.setValidFrom(LocalDate.now().minusDays(5));
		conceptTwo.setValidTill(LocalDate.now().plusDays(15));
		
		IdmConceptRoleRequestDto conceptThrid = new IdmConceptRoleRequestDto();
		conceptThrid.setId(UUID.randomUUID());
		conceptThrid.setRole(roleId);
		embedded = conceptThrid.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		conceptThrid.setEmbedded(embedded);
		conceptThrid.setIdentityContract(contractId);
		conceptThrid.setOperation(ConceptRoleRequestOperation.ADD);
		conceptThrid.setValidFrom(LocalDate.now().plusDays(50));
		conceptThrid.setValidTill(LocalDate.now().plusDays(100));
		
		IdmConceptRoleRequestDto conceptFour = new IdmConceptRoleRequestDto();
		conceptFour.setId(UUID.randomUUID());
		conceptFour.setRole(secondRole.getId());
		embedded = conceptFour.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		conceptFour.setEmbedded(embedded);
		conceptFour.setIdentityContract(contractId);
		conceptFour.setOperation(ConceptRoleRequestOperation.ADD);
		conceptFour.setValidFrom(LocalDate.now().minusDays(10));
		conceptFour.setValidTill(LocalDate.now().plusDays(2));

		List<IdmConceptRoleRequestDto> duplicates = roleRequestService.markDuplicates(
				Lists.newArrayList(conceptOne, conceptTwo, conceptThrid, conceptFour),
				Lists.newArrayList());
		assertEquals(4, duplicates.size());

		for (IdmConceptRoleRequestDto concept : duplicates) {
			if (concept.getId().equals(conceptOne.getId())) {
				assertFalse(concept.getDuplicate());
			} else if (concept.getId().equals(conceptTwo.getId())) {
				DuplicateRolesDto duplicateWithRoles = concept.getDuplicates();
				assertTrue(concept.getDuplicate());
				assertTrue(duplicateWithRoles.getIdentityRoles().isEmpty());
				assertFalse(duplicateWithRoles.getConcepts().isEmpty());
				assertEquals(1, duplicateWithRoles.getConcepts().size());
				UUID duplicatedId = duplicateWithRoles.getConcepts().get(0);
				assertEquals(conceptOne.getId(), duplicatedId);
			} else if (concept.getId().equals(conceptThrid.getId())) {
				assertFalse(concept.getDuplicate());
			} else if (concept.getId().equals(conceptFour.getId())) {
				assertFalse(concept.getDuplicate());
			} else  {
				fail();
			}
		}
	}

	@Test
	public void testMarkDuplicatesBetweenConceptsWithDelete() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString)null);
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityContractDto contract = getHelper().createIdentityContact(identity);
		UUID roleId = role.getId();
		UUID contractId = contract.getId();

		IdmConceptRoleRequestDto conceptOne = new IdmConceptRoleRequestDto();
		conceptOne.setId(UUID.randomUUID());
		conceptOne.setRole(roleId);
		conceptOne.setIdentityContract(contractId);
		Map<String, BaseDto> embedded = conceptOne.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		conceptOne.setEmbedded(embedded);
		conceptOne.setOperation(ConceptRoleRequestOperation.REMOVE);

		IdmConceptRoleRequestDto conceptTwo = new IdmConceptRoleRequestDto();
		conceptTwo.setId(UUID.randomUUID());
		conceptTwo.setRole(roleId);
		embedded = conceptTwo.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		conceptTwo.setEmbedded(embedded);
		conceptTwo.setIdentityContract(contractId);
		conceptTwo.setOperation(ConceptRoleRequestOperation.ADD);

		List<IdmConceptRoleRequestDto> duplicates = roleRequestService.markDuplicates(
				Lists.newArrayList(conceptOne, conceptTwo),
				Lists.newArrayList());
		assertEquals(2, duplicates.size());

		for (IdmConceptRoleRequestDto concept : duplicates) {
			if (concept.getId().equals(conceptOne.getId())) {
				assertFalse(concept.getDuplicate());
			} else if (concept.getId().equals(conceptTwo.getId())) {
				assertFalse(concept.getDuplicate());
			} else  {
				fail();
			}
		}
	}

	@Test
	public void testMarkDuplicatesBothSame() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString)null);
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityContractDto contract = getHelper().createIdentityContact(identity);
		UUID roleId = role.getId();
		UUID contractId = contract.getId();

		IdmConceptRoleRequestDto conceptOne = new IdmConceptRoleRequestDto();
		conceptOne.setId(UUID.randomUUID());
		conceptOne.setRole(roleId);
		conceptOne.setIdentityContract(contractId);
		Map<String, BaseDto> embedded = conceptOne.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		conceptOne.setEmbedded(embedded);
		conceptOne.setOperation(ConceptRoleRequestOperation.ADD);

		IdmConceptRoleRequestDto conceptTwo = new IdmConceptRoleRequestDto();
		conceptTwo.setId(UUID.randomUUID());
		conceptTwo.setRole(roleId);
		embedded = conceptTwo.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		conceptTwo.setEmbedded(embedded);
		conceptTwo.setIdentityContract(contractId);
		conceptTwo.setOperation(ConceptRoleRequestOperation.ADD);

		List<IdmConceptRoleRequestDto> duplicates = roleRequestService.markDuplicates(
				Lists.newArrayList(conceptOne, conceptTwo),
				Lists.newArrayList());
		assertEquals(2, duplicates.size());

		// Both concepts are same. Duplicates is controlled by order in list
		for (IdmConceptRoleRequestDto concept : duplicates) {
			if (concept.getId().equals(conceptOne.getId())) {
				assertFalse(concept.getDuplicate());
			} else if (concept.getId().equals(conceptTwo.getId())) {
				DuplicateRolesDto duplicateWithRoles = concept.getDuplicates();
				assertTrue(concept.getDuplicate());
				assertTrue(duplicateWithRoles.getIdentityRoles().isEmpty());
				assertFalse(duplicateWithRoles.getConcepts().isEmpty());
				assertEquals(1, duplicateWithRoles.getConcepts().size());
				UUID duplicatedId = duplicateWithRoles.getConcepts().get(0);
				assertEquals(conceptOne.getId(), duplicatedId);
			} else  {
				fail();
			}
		}
	}

	@Test
	public void testMarkDuplicatesBetweenConceptsEmpty() {
		List<IdmConceptRoleRequestDto> duplicates = roleRequestService.markDuplicates(
				Lists.newArrayList(),
				Lists.newArrayList());
		assertEquals(0, duplicates.size());
	}

	@Test
	public void testMarkDuplicatesOneConceptOneIdentityRole() {
		IdmIdentityDto identity = this.getHelper().createIdentity((GuardedString)null);
		IdmRoleDto role = this.getHelper().createRole();
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		IdmIdentityRoleDto identityRole = this.getHelper().createIdentityRole(contract, role, null, LocalDate.now().plusDays(5));

		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setRole(role.getId());
		concept.setIdentityContract(contract.getId());
		Map<String, BaseDto> embedded = concept.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		concept.setEmbedded(embedded);
		concept.setOperation(ConceptRoleRequestOperation.ADD);

		List<IdmConceptRoleRequestDto> duplicates = roleRequestService.markDuplicates(Lists.newArrayList(concept), Lists.newArrayList(identityRole));
		assertEquals(1, duplicates.size());

		DuplicateRolesDto duplicateWithRoles = duplicates.get(0).getDuplicates();
		assertFalse(concept.getDuplicate());
		assertTrue(duplicateWithRoles.getIdentityRoles().isEmpty());
		assertTrue(duplicateWithRoles.getConcepts().isEmpty());
	}

	@Test
	public void testMarkDuplicatesOneConceptOneIdentityRoleWithContractValidity() {
		IdmIdentityDto identity = this.getHelper().createIdentity((GuardedString)null);
		IdmRoleDto role = this.getHelper().createRole();
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		contract.setValidTill(LocalDate.now().plusDays(5));
		contract = identityContractService.save(contract);

		IdmIdentityRoleDto identityRole = this.getHelper().createIdentityRole(contract, role, null, LocalDate.now().plusDays(5));

		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setRole(role.getId());
		concept.setIdentityContract(contract.getId());
		Map<String, BaseDto> embedded = concept.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		concept.setEmbedded(embedded);
		concept.setOperation(ConceptRoleRequestOperation.ADD);

		List<IdmConceptRoleRequestDto> duplicates = roleRequestService.markDuplicates(Lists.newArrayList(concept), Lists.newArrayList(identityRole));
		assertEquals(1, duplicates.size());
		
		DuplicateRolesDto duplicateWithRoles = duplicates.get(0).getDuplicates();
		assertTrue(concept.getDuplicate());
		assertFalse(duplicateWithRoles.getIdentityRoles().isEmpty());
		assertTrue(duplicateWithRoles.getConcepts().isEmpty());
		assertEquals(1, duplicateWithRoles.getIdentityRoles().size());
		assertNull(duplicates.get(0).getId());

		assertEquals(identityRole.getId(), duplicateWithRoles.getIdentityRoles().get(0)); 
	}

	private IdmRoleDto createRoleWithAttributes(IdmFormAttributeDto... attrs) {
		IdmRoleDto role = getHelper().createRole();
		assertNull(role.getIdentityRoleAttributeDefinition());
	
		return createRoleAttributes(role, attrs);
	}

	private IdmRoleDto createRoleAttributes(IdmRoleDto role, IdmFormAttributeDto... attrs) {
		assertNull(role.getIdentityRoleAttributeDefinition());
	
		IdmFormDefinitionDto definition = formService.createDefinition(IdmIdentityRole.class, getHelper().createName(),
				ImmutableList.copyOf(attrs));
		role.setIdentityRoleAttributeDefinition(definition.getId());
		role = roleService.save(role);
		assertNotNull(role.getIdentityRoleAttributeDefinition());
		IdmRoleDto roleFinal = role;
		definition.getFormAttributes().forEach(attribute -> {
			roleFormAttributeService.addAttributeToSubdefintion(roleFinal, attribute);
		});

		return role;
	}

	private IdmFormAttributeDto prepareAttributeOne() {
		IdmFormAttributeDto one = new IdmFormAttributeDto(ATTRIBUTE_ONE);
		one.setPersistentType(PersistentType.LONG);
		one.setDefaultValue(String.valueOf(ATTRIBUTE_ONE_DEFAULT_VALUE));
		one.setRequired(false);
		return one;
	}

	private IdmIdentityRoleDto fillEavs(IdmIdentityRoleDto identityRole) {
		identityRole.setEavs(service.getFormInstances(identityRole));
		return identityRole;
	}
}
