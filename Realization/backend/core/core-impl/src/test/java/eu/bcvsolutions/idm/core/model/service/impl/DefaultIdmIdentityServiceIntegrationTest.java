package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitDemoData;
import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleGuaranteeRepository;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Basic identity service operations
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmIdentityServiceIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private TestHelper helper;
	@Autowired private ApplicationContext context;
	@Autowired private FormService formService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmContractGuaranteeService contractGuaranteeService;
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmRoleGuaranteeRepository roleGuaranteeRepository;
	@Autowired private IdmPasswordService passwordService;
	@Autowired private IdmConceptRoleRequestService conceptRequestService;
	//
	private IdmIdentityService identityService;

	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_USER_1);
		identityService = context.getAutowireCapableBeanFactory().createBean(DefaultIdmIdentityService.class);
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testReferentialIntegrity() {
		IdmIdentityDto identity = helper.createIdentity();
		String username = identity.getUsername();
		// eav
		IdmFormDefinitionDto formDefinition = formService.getDefinition(IdmIdentity.class);
		IdmFormValueDto value1 = new IdmFormValueDto(formDefinition.getMappedAttributeByCode(InitDemoData.FORM_ATTRIBUTE_PASSWORD));
		value1.setValue("one");
		formService.saveValues(identity.getId(), IdmIdentity.class, formDefinition, Lists.newArrayList(value1));
		// role with guarantee
		IdmRoleDto role = new IdmRoleDto();
		String roleName = "test_r_" + System.currentTimeMillis();
		role.setName(roleName);
		IdmRoleGuaranteeDto roleGuarantee = new IdmRoleGuaranteeDto();
		roleGuarantee.setRole(role.getId());
		roleGuarantee.setGuarantee(identity.getId());
		role.setGuarantees(Lists.newArrayList(roleGuarantee));
		role = roleService.save(role);
		// contract
		IdmIdentityContractDto contract = helper.createIdentityContact(identity);
		// contract guarantee
		IdmIdentityContractDto contract2 = helper.createIdentityContact(identityService.getByUsername(InitTestData.TEST_USER_1));
		
		contractGuaranteeService.save(new IdmContractGuaranteeDto(contract2.getId(), identity.getId()));
		// assigned role
		helper.createIdentityRole(contract, role);
		IdmIdentityRoleFilter identityRolefilter = new IdmIdentityRoleFilter();
		identityRolefilter.setIdentityId(identity.getId());

		assertEquals(1, role.getGuarantees().size());
		assertNotNull(identityService.getByUsername(username));
		assertNotNull(passwordService.findOneByIdentity(identity.getId()));
		assertEquals(1, formService.getValues(identity).size());
		assertEquals(username, roleGuaranteeRepository.findAllByRole_Id(role.getId()).get(0).getGuarantee().getUsername());
		assertEquals(1, identityRoleService.find(identityRolefilter, null).getTotalElements());
		assertEquals(2, identityContractService.findAllByIdentity(identity.getId()).size()); // + default contract is created
		IdmContractGuaranteeFilter filter = new IdmContractGuaranteeFilter();
		filter.setIdentityContractId(contract2.getId());
		List<IdmContractGuaranteeDto> guarantees = contractGuaranteeService.find(filter, null).getContent();
		assertEquals(1, guarantees.size());
		assertEquals(identity.getId(), guarantees.get(0).getGuarantee());
		//
		identityService.delete(identity);
		role = roleService.get(role.getId());
		//
		assertEquals(0, role.getGuarantees().size());
		assertNull(identityService.getByUsername(username));
		assertNull(passwordService.findOneByIdentity(identity.getId()));
		assertEquals(0, identityContractService.findAllByIdentity(identity.getId()).size());
		assertEquals(0, identityRoleService.find(identityRolefilter, null).getTotalElements());
		assertEquals(0, contractGuaranteeService.find(filter, null).getTotalElements());
		// TODO: transactions?
		// assertEquals(0, roleGuaranteeRepository.findAllByRole_Id(role.getId()).size());
	}
	
	@Test
	public void testReferentialRoleRequestIntegrity() {
		IdmIdentityDto identity = helper.createIdentity();
		String username = identity.getUsername();
		
		// role with guarantee
		IdmRoleDto role = new IdmRoleDto();
		String roleName = "test_r_" + System.currentTimeMillis();
		role.setName(roleName);
		role = roleService.save(role);
		// assigned role
		IdmRoleRequestDto request = helper.assignRoles(helper.getPrimeContract(identity.getId()), false, role);
		IdmConceptRoleRequestFilter conceptFilter = new IdmConceptRoleRequestFilter();
		conceptFilter.setRoleRequestId(request.getId());
		
		IdmIdentityRoleFilter identityRolefilter = new IdmIdentityRoleFilter();
		identityRolefilter.setIdentityId(identity.getId());

		assertNotNull(identityService.getByUsername(username));
		assertEquals(1, identityRoleService.find(identityRolefilter, null).getTotalElements());
		assertEquals(1, conceptRequestService.find(conceptFilter, null).getTotalElements());
		
		IdmConceptRoleRequestDto concept = conceptRequestService.find(conceptFilter, null).getContent().get(0);
		concept.setWfProcessId("test_wf_" + System.currentTimeMillis());
		conceptRequestService.save(concept);
		//
		identityService.delete(identity);
		role = roleService.get(role.getId());
		//
		assertNull(identityService.getByUsername(username));
		assertNull(passwordService.findOneByIdentity(identity.getId()));
		assertEquals(0, identityContractService.findAllByIdentity(identity.getId()).size());
		assertEquals(0, identityRoleService.find(identityRolefilter, null).getTotalElements());
		assertEquals(0, conceptRequestService.find(conceptFilter, null).getTotalElements());
		roleService.delete(role);
		assertNull(roleService.get(role.getId()));
	}

	/**
	 * When identity is created, then default contract have to be created too.
	 */
	@Test
	public void testCreateDefaultContract() {
		IdmIdentityDto identity = new IdmIdentityDto();
		String username = "contract_test_" + System.currentTimeMillis();
		identity.setUsername(username);
		identity.setPassword(new GuardedString("heslo")); // confidential storage
		identity.setFirstName("Test");
		identity.setLastName("Identity");
		identity = identityService.save(identity);
		//
		List<IdmIdentityContractDto> contracts = identityContractService.findAllByIdentity(identity.getId());
		assertEquals(1, contracts.size());
		//
		IdmIdentityContractDto defaultContract = identityContractService.prepareMainContract(identity.getId());
		assertEquals(defaultContract.getIdentity(), contracts.get(0).getIdentity());
		assertEquals(defaultContract.getPosition(), contracts.get(0).getPosition());
		assertEquals(defaultContract.getWorkPosition(), contracts.get(0).getWorkPosition());
	}
	
	/**
	 * When identity is created with the event property
	 * IdmIdentityContractService.SKIP_CREATION_OF_DEFAULT_POSITION = TRUE, then
	 * default contract haven't to be created.
	 */
	@Test
	public void testSkipDefaultContract() {
		IdmIdentityDto identity = new IdmIdentityDto();
		String username = "contract_test_" + System.currentTimeMillis();
		identity.setUsername(username);
		identity.setPassword(new GuardedString("heslo")); // confidential storage
		identity.setFirstName("Test");
		identity.setLastName("Identity");

		EntityEvent<IdmIdentityDto> event = new IdentityEvent(IdentityEventType.CREATE, identity, ImmutableMap.of(
				// In the identity sync are creation of the default contract skipped.
				IdmIdentityContractService.SKIP_CREATION_OF_DEFAULT_POSITION, Boolean.TRUE));

		identity = identityService.publish(event).getContent();
		//
		List<IdmIdentityContractDto> contracts = identityContractService.findAllByIdentity(identity.getId());
		assertEquals(0, contracts.size());
	}
	
	@Transactional
	@Test(expected = ConstraintViolationException.class)
	public void testIdentityJSR303Validations() {
		IdmIdentityDto identity = new IdmIdentityDto();
		String username = "validation_test_" + System.currentTimeMillis();
		identity.setUsername(username);
		identity.setLastName("Identity");
		identity.setEmail("email_wrong");
		identity = identityService.save(identity);
	}
	
	@Test
	@Transactional
	public void testSaveIdentityWithoutLastname() {
		IdmIdentityDto identity = new IdmIdentityDto();
		String username = "validation_test_" + System.currentTimeMillis();
		identity.setUsername(username);
		identity = identityService.save(identity);
		//
		Assert.assertNotNull(identity.getId());
	}
	
	@Test
	@Transactional
	public void testModifiedAfterUpdateIdentity() {
		IdmIdentityDto identity = helper.createIdentity();
		Assert.assertNull(identity.getModified());
		//
		identity.setDescription("update");
		identity = identityService.save(identity);
		Assert.assertNotNull(identity.getModified());
	}
	
	@Test
	@Transactional
	public void testDisableAndEnableIdentity() {
		IdmIdentityDto identity = helper.createIdentity();
		Assert.assertFalse(identity.isDisabled());
		Assert.assertEquals(IdentityState.CREATED, identity.getState());
		identity = identityService.get(identity.getId());
		// default contract is aplied
		Assert.assertEquals(IdentityState.VALID, identity.getState());
		//
		identity = identityService.disable(identity.getId());
		//
		Assert.assertTrue( identity.isDisabled());
		Assert.assertEquals(IdentityState.DISABLED_MANUALLY, identity.getState());
		//
		identity = identityService.enable(identity.getId());
		Assert.assertFalse(identity.isDisabled());
		Assert.assertEquals(IdentityState.VALID, identity.getState());
	}
	
	@Test
	@Transactional
	public void testEnableIdentityByContract() {
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityContractDto contract = helper.getPrimeContract(identity.getId());
		contract.setValidFrom(new LocalDate().plusDays(1));
		identityContractService.save(contract);
		identity = identityService.get(identity.getId());
		Assert.assertTrue(identity.isDisabled());
		Assert.assertEquals(IdentityState.FUTURE_CONTRACT, identity.getState());
		//
		contract.setValidFrom(new LocalDate());
		identityContractService.save(contract);
		//
		identity = identityService.get(identity.getId());
		Assert.assertFalse(identity.isDisabled());
		Assert.assertEquals(IdentityState.VALID, identity.getState());
	}
	
	@Test
	@Transactional
	public void testIdentityFutureContract() {
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityContractDto contract = helper.getPrimeContract(identity.getId());
		identityContractService.delete(contract);
		identity = identityService.get(identity.getId());
		Assert.assertTrue(identity.isDisabled());
		Assert.assertEquals(IdentityState.NO_CONTRACT, identity.getState());
		//
		contract = helper.createIdentityContact(identity, null, new LocalDate().plusDays(1), null);
		//
		identity = identityService.get(identity.getId());
		Assert.assertTrue(identity.isDisabled());
		Assert.assertEquals(IdentityState.FUTURE_CONTRACT, identity.getState());
	}
	
	@Test
	@Transactional
	public void testFindByRole() {
		IdmIdentityDto identity = helper.createIdentity();
		IdmRoleDto role = helper.createRole();
		helper.createIdentityRole(identity, role);
		helper.createIdentityRole(identity, role);
		helper.createIdentityRole(identity, role, new LocalDate().minusDays(1), new LocalDate().plusDays(1));
		IdmIdentityContractDto contract = helper.createIdentityContact(identity);
		helper.createIdentityRole(contract, role);
		helper.createIdentityRole(contract, role);
		helper.createIdentityRole(contract, role, new LocalDate().minusDays(1), new LocalDate().plusDays(1));
		//
		List<IdmIdentityDto> identities = identityService.findAllByRole(role.getId());
		//
		Assert.assertEquals(1, identities.size());
		Assert.assertEquals(identity.getId(), identities.get(0).getId());
		//
		identities = identityService.findValidByRole(role.getId());
		//
		Assert.assertEquals(1, identities.size());
		Assert.assertEquals(identity.getId(), identities.get(0).getId());
	}
	
	@Test
	@Transactional
	public void testFindValidByRole() {
		IdmIdentityDto validIdentity = helper.createIdentity();
		IdmRoleDto role = helper.createRole();
		helper.createIdentityRole(validIdentity, role, new LocalDate().plusDays(1), null);
		IdmIdentityContractDto contract = helper.createIdentityContact(validIdentity, null, new LocalDate().minusDays(1), null);
		helper.createIdentityRole(contract, role);
		helper.createIdentityRole(contract, role);
		helper.createIdentityRole(contract, role, new LocalDate().minusDays(1), new LocalDate().plusDays(1));
		//
		// disabled identity
		IdmIdentityDto identityDisabled = helper.createIdentity();
		identityDisabled.setState(IdentityState.DISABLED);
		identityDisabled = identityService.save(identityDisabled);
		helper.createIdentityRole(identityDisabled, role);
		//
		// left identity
		IdmIdentityDto identityLeft = helper.createIdentity();
		identityLeft.setState(IdentityState.LEFT);
		identityLeft = identityService.save(identityLeft);
		helper.createIdentityRole(identityLeft, role);
		//
		// disabled contract
		IdmIdentityDto identityDisabledContract = helper.createIdentity();
		IdmIdentityContractDto disabledContract = helper.getPrimeContract(identityDisabledContract.getId());
		disabledContract.setState(ContractState.DISABLED);
		identityContractService.save(disabledContract);
		helper.createIdentityRole(identityDisabledContract, role);
		//
		// expired contract
		IdmIdentityDto identityInvalidContract = helper.createIdentity();
		IdmIdentityContractDto invalidContract = helper.getPrimeContract(identityInvalidContract.getId());
		invalidContract.setValidFrom(new LocalDate().plusDays(1));
		identityContractService.save(invalidContract);
		helper.createIdentityRole(identityInvalidContract, role);
		//
		// excluded contract
		IdmIdentityDto identityExcludedContract = helper.createIdentity();
		IdmIdentityContractDto excludedContract = helper.getPrimeContract(identityExcludedContract.getId());
		excludedContract.setState(ContractState.EXCLUDED);
		identityContractService.save(excludedContract);
		helper.createIdentityRole(identityExcludedContract, role);
		//
		List<IdmIdentityDto> identities = identityService.findValidByRole(role.getId());
		//
		Assert.assertEquals(1, identities.size());
		Assert.assertEquals(validIdentity.getId(), identities.get(0).getId());
	}
}
