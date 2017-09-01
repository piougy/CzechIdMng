package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitDemoData;
import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.ContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdentityRoleFilter;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityFormValue;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleGuaranteeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
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
	@Autowired private IdmIdentityRepository identityRepository;
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testReferentialIntegrity() {
		IdmIdentityDto identity = new IdmIdentityDto();
		String username = "delete_test_" + System.currentTimeMillis();
		identity.setUsername(username);
		identity.setPassword(new GuardedString("heslo"));
		identity.setFirstName("Test");
		identity.setLastName("Identity");
		identity = identityService.save(identity);
		// eav
		IdmFormDefinition formDefinition = formService.getDefinition(IdmIdentity.class);
		AbstractFormValue value1 = new IdmIdentityFormValue(
				formDefinition.getMappedAttributeByCode(InitDemoData.FORM_ATTRIBUTE_PASSWORD));
		value1.setValue("one");
		formService.saveValues(identity.getId(), IdmIdentity.class, formDefinition, Lists.newArrayList(value1));
		// role guarantee
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
		IdentityRoleFilter identityRolefilter = new IdentityRoleFilter();
		identityRolefilter.setIdentityId(identity.getId());

		assertEquals(1, role.getGuarantees().size());
		assertNotNull(identityService.getByUsername(username));
		assertNotNull(passwordService.findOneByIdentity(identity.getId()));
		assertEquals(1, formService.getValues(identityRepository.findOne(identity.getId())).size());
		assertEquals(username, roleGuaranteeRepository.findAllByRole_Id(role.getId()).get(0).getGuarantee().getUsername());
		assertEquals(1, identityRoleService.find(identityRolefilter, null).getTotalElements());
		assertEquals(2, identityContractService.findAllByIdentity(identity.getId()).size()); // + default contract is created
		ContractGuaranteeFilter filter = new ContractGuaranteeFilter();
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

}
