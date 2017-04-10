package eu.bcvsolutions.idm.core.service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.TestHelper;
import eu.bcvsolutions.idm.core.api.dto.IdentityDto;
import eu.bcvsolutions.idm.core.model.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.model.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.model.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for change permissions via Role request.
 * 
 * @author svandav
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DefaultIdmRoleRequestServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	protected TestHelper helper;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	private SecurityService securityService;
	@Autowired
	private IdmRoleService roleService;;
	@Autowired
	private IdmConfigurationService configurationService;

	//
	private IdmRole roleA;

	private static final String USER_TEST_A = "testA";
	private static final String USER_TEST_B = "testB";
	private static final String APPROVE_BY_MANAGER_ENABLE = "idm.sec.core.wf.approval.manager.enabled";

	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_USER_1);
		prepareIdentityAndRoles();
	}

	@After
	public void logout() {
		super.logout();
	}

	private void prepareIdentityAndRoles() {
		// create roles
		roleA = helper.createRole("A");
		roleA.setPriority(100);
		roleService.save(roleA);
		
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "true");

		// prepare identity and contract
		preapareContractAndIdentity(USER_TEST_A);
		preapareContractAndIdentity(USER_TEST_B);
	}

	private void preapareContractAndIdentity(String username) {
		IdmIdentity identity = createIdentity(username);
		IdmIdentityContract contract = new IdmIdentityContract();
		contract.setIdentity(identity);
		contract.setValidFrom(new LocalDate().minusDays(1));
		contract.setValidTill(new LocalDate().plusMonths(1));
		contract.setMain(true);
		contract.setDescription(username);
		identityContractService.save(contract);
	}

	private IdmIdentity createIdentity(String name) {
		IdmIdentity identity = new IdmIdentity();
		identity.setUsername(name);
		// identity.setPassword(new GuardedString("heslo"));
		identity.setFirstName("Test");
		identity.setLastName("Identity");
		identity = identityService.save(identity);
		return identity;
	}

	@Test
	@Transactional()
	public void addPermissionViaRoleRequestTest() {
		IdmIdentity testA = identityService.getByUsername(USER_TEST_A);
		IdmIdentityContract contractA = identityContractService.getPrimeContract(testA);

		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicant(testA.getId());
		request.setExecuteImmediately(true);
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		request.setState(RoleRequestState.EXECUTED); // can not be saved (after
														// create must be
														// CONCEPT)
		request = roleRequestService.save(request);

		Assert.assertEquals(RoleRequestState.CONCEPT, request.getState());

		LocalDate validFrom = new LocalDate().minusDays(1);
		LocalDate validTill = new LocalDate().plusMonths(1);
		IdmConceptRoleRequestDto conceptA = new IdmConceptRoleRequestDto();
		conceptA.setRoleRequest(request.getId());
		conceptA.setState(RoleRequestState.EXECUTED); // can not be saved (after
														// create must be
														// CONCEPT)
		conceptA.setOperation(ConceptRoleRequestOperation.ADD);
		conceptA.setRole(roleA.getId());
		conceptA.setValidFrom(validFrom);
		conceptA.setValidTill(validTill);
		conceptA.setIdentityContract(contractA.getId());
		conceptA = conceptRoleRequestService.save(conceptA);

		Assert.assertEquals(RoleRequestState.CONCEPT, conceptA.getState());

		roleRequestService.startRequest(request.getId());
		request = roleRequestService.getDto(request.getId());

		Assert.assertEquals(RoleRequestState.EXECUTED, request.getState());
		List<IdmIdentityRole> identityRoles = identityRoleService.getRoles(testA);
		Assert.assertEquals(1, identityRoles.size());
		Assert.assertEquals(validFrom, identityRoles.get(0).getValidFrom());
		Assert.assertEquals(validTill, identityRoles.get(0).getValidTill());
		Assert.assertEquals(contractA, identityRoles.get(0).getIdentityContract());
		Assert.assertEquals(roleA, identityRoles.get(0).getRole());

	}

	@Test
	@Transactional()
	public void changePermissionViaRoleRequestTest() {
		this.addPermissionViaRoleRequestTest();
		IdmIdentity testA = identityService.getByUsername(USER_TEST_A);
		IdmIdentityContract contractA = identityContractService.getPrimeContract(testA);

		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicant(testA.getId());
		request.setExecuteImmediately(true);
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		request = roleRequestService.save(request);

		List<IdmIdentityRole> identityRoles = identityRoleService.getRoles(testA);
		Assert.assertEquals(1, identityRoles.size());

		LocalDate validFrom = new LocalDate().minusDays(1);
		IdmConceptRoleRequestDto conceptA = new IdmConceptRoleRequestDto();
		conceptA.setRoleRequest(request.getId());
		conceptA.setRole(identityRoles.get(0).getRole().getId());
		conceptA.setOperation(ConceptRoleRequestOperation.UPDATE);
		conceptA.setValidFrom(validFrom);
		conceptA.setValidTill(null);
		conceptA.setIdentityContract(contractA.getId());
		conceptA.setIdentityRole(identityRoles.get(0).getId());
		conceptA = conceptRoleRequestService.save(conceptA);

		roleRequestService.startRequest(request.getId());
		request = roleRequestService.getDto(request.getId());

		Assert.assertEquals(RoleRequestState.EXECUTED, request.getState());
		identityRoles = identityRoleService.getRoles(testA);
		Assert.assertEquals(1, identityRoles.size());
		Assert.assertEquals(validFrom, identityRoles.get(0).getValidFrom());
		Assert.assertEquals(null, identityRoles.get(0).getValidTill());
		Assert.assertEquals(contractA, identityRoles.get(0).getIdentityContract());
		Assert.assertEquals(roleA, identityRoles.get(0).getRole());

	}

	@Test
	@Transactional()
	public void removePermissionViaRoleRequestTest() {
		this.addPermissionViaRoleRequestTest();
		IdmIdentity testA = identityService.getByUsername(USER_TEST_A);
		IdmIdentityContract contractA = identityContractService.getPrimeContract(testA);

		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicant(testA.getId());
		request.setExecuteImmediately(true);
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		request = roleRequestService.save(request);

		List<IdmIdentityRole> identityRoles = identityRoleService.getRoles(testA);
		Assert.assertEquals(1, identityRoles.size());

		IdmConceptRoleRequestDto conceptA = new IdmConceptRoleRequestDto();
		conceptA.setRoleRequest(request.getId());
		conceptA.setRole(identityRoles.get(0).getRole().getId());
		conceptA.setOperation(ConceptRoleRequestOperation.REMOVE);
		conceptA.setIdentityContract(contractA.getId());
		conceptA.setIdentityRole(identityRoles.get(0).getId());
		conceptA = conceptRoleRequestService.save(conceptA);

		roleRequestService.startRequest(request.getId());
		request = roleRequestService.getDto(request.getId());

		Assert.assertEquals(RoleRequestState.EXECUTED, request.getState());
		identityRoles = identityRoleService.getRoles(testA);
		Assert.assertEquals(0, identityRoles.size());

	}

	@Test()
	@Transactional()
	public void noSameApplicantExceptionTest() {
		IdmIdentity testA = identityService.getByUsername(USER_TEST_A);
		IdmIdentity testB = identityService.getByUsername(USER_TEST_B);
		IdmIdentityContract contractB = identityContractService.getPrimeContract(testB);

		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicant(testA.getId());
		request.setExecuteImmediately(true);
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto conceptA = new IdmConceptRoleRequestDto();
		conceptA.setRoleRequest(request.getId());
		conceptA.setOperation(ConceptRoleRequestOperation.ADD);
		conceptA.setRole(roleA.getId());
		conceptA.setIdentityContract(contractB.getId()); // Contract from
															// applicant B
		conceptA = conceptRoleRequestService.save(conceptA);

		// excepted ROLE_REQUEST_APPLICANTS_NOT_SAME exception
		roleRequestService.startRequest(request.getId());
		request = roleRequestService.getDto(request.getId());

		Assert.assertEquals(RoleRequestState.EXCEPTION, request.getState());

	}
	
	@Test()
	@Transactional()
	public void duplicatedRequestExceptionTest() {
		loginAsAdmin(USER_TEST_A);
		
		IdmIdentity testA = identityService.getByUsername(USER_TEST_A);
		IdmIdentityContract contractA = identityContractService.getPrimeContract(testA);

		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicant(testA.getId());
		request.setExecuteImmediately(false);
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		IdmRoleRequestDto requestA = roleRequestService.save(request);

		Assert.assertEquals(RoleRequestState.CONCEPT, requestA.getState());

		LocalDate validFrom = new LocalDate().minusDays(1);
		LocalDate validTill = new LocalDate().plusMonths(1);
		IdmConceptRoleRequestDto conceptA = new IdmConceptRoleRequestDto();
		conceptA.setRoleRequest(requestA.getId());
		conceptA.setOperation(ConceptRoleRequestOperation.ADD);
		conceptA.setRole(roleA.getId());
		conceptA.setValidFrom(validFrom);
		conceptA.setValidTill(validTill);
		conceptA.setIdentityContract(contractA.getId());
		conceptRoleRequestService.save(conceptA);

		roleRequestService.startRequest(requestA.getId());
		requestA = roleRequestService.getDto(requestA.getId());
		Assert.assertEquals(RoleRequestState.IN_PROGRESS, requestA.getState());

		IdmRoleRequestDto requestB = roleRequestService.save(request);
		conceptA.setRoleRequest(requestB.getId());
		conceptRoleRequestService.save(conceptA);
		
		// We expect duplication exception
		roleRequestService.startRequest(requestB.getId());
		requestB = roleRequestService.getDto(requestB.getId());
		Assert.assertEquals(RoleRequestState.DUPLICATED, requestB.getState());
		Assert.assertEquals(requestA.getId(), requestB.getDuplicatedToRequest());
		
		
		// We change only description (remove duplicity)
		requestB.setDescription("-----");
		roleRequestService.save(requestB);
		
		// We expect correct start
		roleRequestService.startRequest(requestB.getId());
		requestB = roleRequestService.getDto(requestB.getId());
		Assert.assertEquals(RoleRequestState.IN_PROGRESS, requestB.getState());
		Assert.assertEquals(null, requestB.getDuplicatedToRequest());
		
	}
	
	@Test
	@Transactional()
	public void notRightForExecuteImmediatelyExceptionTest() {
		this.logout();
		// Log as user without right for immediately execute role request (without approval)
		Collection<GrantedAuthority> authorities = securityService.getAllAvailableAuthorities().stream().filter(authority -> {
			return !CoreGroupPermission.ROLE_REQUEST_EXECUTE.equals(authority.getAuthority());
		}).collect(Collectors.toList());
		SecurityContextHolder.getContext().setAuthentication(new IdmJwtAuthentication(new IdentityDto(USER_TEST_A), null, authorities, "test"));
		
		IdmIdentity testA = identityService.getByUsername(USER_TEST_A);
		IdmIdentityContract contractA = identityContractService.getPrimeContract(testA);

		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicant(testA.getId());
		request.setExecuteImmediately(true);
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		request = roleRequestService.save(request);

		Assert.assertEquals(RoleRequestState.CONCEPT, request.getState());

		IdmConceptRoleRequestDto conceptA = new IdmConceptRoleRequestDto();
		conceptA.setRoleRequest(request.getId());
		conceptA.setOperation(ConceptRoleRequestOperation.ADD);
		conceptA.setRole(roleA.getId());
		conceptA.setIdentityContract(contractA.getId());
		conceptA = conceptRoleRequestService.save(conceptA);

		Assert.assertEquals(RoleRequestState.CONCEPT, conceptA.getState());

		roleRequestService.startRequest(request.getId());
		request = roleRequestService.getDto(request.getId());
		
		// We expect exception state (we don`t have right for execute without approval)
		Assert.assertEquals(RoleRequestState.EXCEPTION, request.getState());

	}

}
