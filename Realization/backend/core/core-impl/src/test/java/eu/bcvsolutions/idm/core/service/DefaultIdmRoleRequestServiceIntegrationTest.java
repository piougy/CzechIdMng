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
import eu.bcvsolutions.idm.core.AbstractCoreWorkflowIntegrationTest;
import eu.bcvsolutions.idm.core.TestHelper;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.exception.RoleRequestException;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.utils.IdmAuthorityUtils;

/**
 * Test for change permissions via Role request.
 * 
 * @author svandav
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DefaultIdmRoleRequestServiceIntegrationTest extends AbstractCoreWorkflowIntegrationTest {

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
	private ModuleService moduleService;
	@Autowired
	private IdmRoleService roleService;
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
		roleA = helper.createRole();
		roleA.setPriority(100);
		roleService.save(roleA);
		
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "true");

		// prepare identity and contract
		preapareContractAndIdentity(USER_TEST_A);
		preapareContractAndIdentity(USER_TEST_B);
	}

	private void preapareContractAndIdentity(String username) {
		IdmIdentityDto identity = createIdentity(username);
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setValidFrom(new LocalDate().minusDays(1));
		contract.setValidTill(new LocalDate().plusMonths(1));
		contract.setMain(true);
		contract.setDescription(username);
		identityContractService.save(contract);
	}

	private IdmIdentityDto createIdentity(String name) {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername(name);
		identity.setFirstName("Test");
		identity.setLastName("Identity");
		identity = identityService.save(identity);
		return identity;
	}

	@Test
	@Transactional()
	public void addPermissionViaRoleRequestTest() {
		IdmIdentityDto testA = identityService.getByUsername(USER_TEST_A);
		IdmIdentityContractDto contractA = identityContractService.getPrimeContract(testA.getId());

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

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());

		Assert.assertEquals(RoleRequestState.EXECUTED, request.getState());
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(testA.getId());
		Assert.assertEquals(1, identityRoles.size());
		Assert.assertEquals(validFrom, identityRoles.get(0).getValidFrom());
		Assert.assertEquals(validTill, identityRoles.get(0).getValidTill());
		Assert.assertEquals(contractA.getId(), identityRoles.get(0).getIdentityContract());
		Assert.assertEquals(roleA.getId(), identityRoles.get(0).getRole());

	}

	@Test
	@Transactional()
	public void changePermissionViaRoleRequestTest() {
		this.addPermissionViaRoleRequestTest();
		IdmIdentityDto testA = identityService.getByUsername(USER_TEST_A);
		IdmIdentityContractDto contractA = identityContractService.getPrimeContract(testA.getId());

		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicant(testA.getId());
		request.setExecuteImmediately(true);
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		request = roleRequestService.save(request);

		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(testA.getId());
		Assert.assertEquals(1, identityRoles.size());

		LocalDate validFrom = new LocalDate().minusDays(1);
		IdmConceptRoleRequestDto conceptA = new IdmConceptRoleRequestDto();
		conceptA.setRoleRequest(request.getId());
		conceptA.setRole(identityRoles.get(0).getRole());
		conceptA.setOperation(ConceptRoleRequestOperation.UPDATE);
		conceptA.setValidFrom(validFrom);
		conceptA.setValidTill(null);
		conceptA.setIdentityContract(contractA.getId());
		conceptA.setIdentityRole(identityRoles.get(0).getId());
		conceptA = conceptRoleRequestService.save(conceptA);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());

		Assert.assertEquals(RoleRequestState.EXECUTED, request.getState());
		identityRoles = identityRoleService.findAllByIdentity(testA.getId());
		Assert.assertEquals(1, identityRoles.size());
		Assert.assertEquals(validFrom, identityRoles.get(0).getValidFrom());
		Assert.assertEquals(null, identityRoles.get(0).getValidTill());
		Assert.assertEquals(contractA.getId(), identityRoles.get(0).getIdentityContract());
		Assert.assertEquals(roleA.getId(), identityRoles.get(0).getRole());

	}

	@Test
	@Transactional()
	public void removePermissionViaRoleRequestTest() {
		this.addPermissionViaRoleRequestTest();
		IdmIdentityDto testA = identityService.getByUsername(USER_TEST_A);
		IdmIdentityContractDto contractA = identityContractService.getPrimeContract(testA.getId());

		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicant(testA.getId());
		request.setExecuteImmediately(true);
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		request = roleRequestService.save(request);

		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(testA.getId());
		Assert.assertEquals(1, identityRoles.size());

		IdmConceptRoleRequestDto conceptA = new IdmConceptRoleRequestDto();
		conceptA.setRoleRequest(request.getId());
		conceptA.setRole(identityRoles.get(0).getRole());
		conceptA.setOperation(ConceptRoleRequestOperation.REMOVE);
		conceptA.setIdentityContract(contractA.getId());
		conceptA.setIdentityRole(identityRoles.get(0).getId());
		conceptA = conceptRoleRequestService.save(conceptA);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());

		Assert.assertEquals(RoleRequestState.EXECUTED, request.getState());
		identityRoles = identityRoleService.findAllByIdentity(testA.getId());
		Assert.assertEquals(0, identityRoles.size());

	}

	@Test(expected = RoleRequestException.class)
	@Transactional()
	public void noSameApplicantExceptionTest() {
		IdmIdentityDto testA = identityService.getByUsername(USER_TEST_A);
		IdmIdentityDto testB = identityService.getByUsername(USER_TEST_B);
		IdmIdentityContractDto contractB = identityContractService.getPrimeContract(testB.getId());

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
		roleRequestService.startRequestInternal(request.getId(), true);

	}
	
	@Test()
	@Transactional()
	public void duplicatedRequestExceptionTest() {
		loginAsAdmin(USER_TEST_A);
		
		IdmIdentityDto testA = identityService.getByUsername(USER_TEST_A);
		IdmIdentityContractDto contractA = identityContractService.getPrimeContract(testA.getId());

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

		roleRequestService.startRequestInternal(requestA.getId(), true);
		requestA = roleRequestService.get(requestA.getId());
		Assert.assertEquals(RoleRequestState.IN_PROGRESS, requestA.getState());

		IdmRoleRequestDto requestB = roleRequestService.save(request);
		conceptA.setRoleRequest(requestB.getId());
		conceptRoleRequestService.save(conceptA);
		
		// We expect duplication exception
		roleRequestService.startRequestInternal(requestB.getId(), true);
		requestB = roleRequestService.get(requestB.getId());
		Assert.assertEquals(RoleRequestState.DUPLICATED, requestB.getState());
		Assert.assertEquals(requestA.getId(), requestB.getDuplicatedToRequest());
		
		
		// We change only description (remove duplicity)
		requestB.setDescription("-----");
		roleRequestService.save(requestB);
		
		// We expect correct start
		roleRequestService.startRequestInternal(requestB.getId(), true);
		requestB = roleRequestService.get(requestB.getId());
		Assert.assertEquals(RoleRequestState.IN_PROGRESS, requestB.getState());
		Assert.assertEquals(null, requestB.getDuplicatedToRequest());
		
	}

	@Test(expected = RoleRequestException.class)
	@Transactional()
	public void notRightForExecuteImmediatelyExceptionTest() {
		this.logout();
		// Log as user without right for immediately execute role request (without approval)
		Collection<GrantedAuthority> authorities = IdmAuthorityUtils.toAuthorities(moduleService.getAvailablePermissions()).stream().filter(authority -> {
			return !CoreGroupPermission.ROLE_REQUEST_EXECUTE.equals(authority.getAuthority())
					&& !CoreGroupPermission.ROLE_REQUEST_ADMIN.equals(authority.getAuthority())
					&& !IdmGroupPermission.APP_ADMIN.equals(authority.getAuthority());
		}).collect(Collectors.toList());
		SecurityContextHolder.getContext().setAuthentication(new IdmJwtAuthentication(new IdmIdentityDto(USER_TEST_A), null, authorities, "test"));
		
		IdmIdentityDto testA = identityService.getByUsername(USER_TEST_A);
		IdmIdentityContractDto contractA = identityContractService.getPrimeContract(testA.getId());

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

		// We expect exception state (we don`t have right for execute without approval)
		roleRequestService.startRequestInternal(request.getId(), true);

	}

}
