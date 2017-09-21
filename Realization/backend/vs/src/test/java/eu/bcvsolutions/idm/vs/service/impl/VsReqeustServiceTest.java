package eu.bcvsolutions.idm.vs.service.impl;

import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;

import com.google.common.collect.ImmutableList;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.LoginService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.vs.TestHelper;
import eu.bcvsolutions.idm.vs.domain.VirtualSystemGroupPermission;
import eu.bcvsolutions.idm.vs.domain.VsOperationType;
import eu.bcvsolutions.idm.vs.domain.VsRequestState;
import eu.bcvsolutions.idm.vs.entity.VsRequest;
import eu.bcvsolutions.idm.vs.evaluator.VsRequestByImplementerEvaluator;
import eu.bcvsolutions.idm.vs.repository.filter.VsRequestFilter;
import eu.bcvsolutions.idm.vs.service.api.VsAccountService;
import eu.bcvsolutions.idm.vs.service.api.VsRequestService;
import eu.bcvsolutions.idm.vs.service.api.VsSystemImplementerService;
import eu.bcvsolutions.idm.vs.service.api.dto.VsAccountDto;
import eu.bcvsolutions.idm.vs.service.api.dto.VsRequestDto;
import eu.bcvsolutions.idm.vs.service.api.dto.VsSystemDto;

/**
 * Virtual system request test
 * 
 * @author Svanda
 *
 */
@Component
public class VsReqeustServiceTest extends AbstractIntegrationTest {

	public static final String USER_ONE_NAME = "vsUserOne";
	public static final String USER_IMPLEMENTER_NAME = "vsUserImplementer";
	public static final String ROLE_ONE_NAME = "vsRoleOne";

	@Autowired
	private TestHelper helper;
	@Autowired
	private VsRequestService requestService;
	@Autowired
	private VsAccountService accountService;
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private LoginService loginService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmRoleService roleService;
	private VsReqeustServiceTest itself;

	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}

	@After
	public void logout() {
		this.getItself().deleteAll(USER_ONE_NAME, USER_IMPLEMENTER_NAME, ROLE_ONE_NAME);
		super.logout();
	}

	@Test
	@Transactional
	public void createAndRealizeRequestTest() {

		// We need do commit, without this will be provisioning not executed
		SysSystemDto system = this.getItself().assignRoleSystemInNewTransaction(USER_ONE_NAME, USER_IMPLEMENTER_NAME,
				ROLE_ONE_NAME);
		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(system.getId());
		requestFilter.setUid(USER_ONE_NAME);
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto request = requests.get(0);
		Assert.assertEquals(USER_ONE_NAME, request.getUid());
		Assert.assertEquals(VsOperationType.CREATE, request.getOperationType());
		Assert.assertEquals(VsRequestState.IN_PROGRESS, request.getState());

		VsAccountDto account = accountService.findByUidSystem(USER_ONE_NAME, system.getId());
		Assert.assertNull("Account must be null, because request was not realized yet!", account);
		// We try realize the request
		super.logout();
		loginService.login(new LoginDto(USER_IMPLEMENTER_NAME, new GuardedString("password")));
		request = requestService.realize(request);
		Assert.assertEquals(VsRequestState.REALIZED, request.getState());
		account = accountService.findByUidSystem(USER_ONE_NAME, system.getId());
		Assert.assertNotNull("Account cannot be null, because request was realized!", account);
	}

	@Test
	@Transactional
	public void createAndCancelRequestTest() {
		String reason = "cancel \"request\" reason!";

		// We need do commit, without this will be provisioning not executed
		SysSystemDto system = this.getItself().assignRoleSystemInNewTransaction(USER_ONE_NAME, USER_IMPLEMENTER_NAME,
				ROLE_ONE_NAME);

		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(system.getId());
		requestFilter.setUid(USER_ONE_NAME);
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto request = requests.get(0);
		Assert.assertEquals(USER_ONE_NAME, request.getUid());
		Assert.assertEquals(VsOperationType.CREATE, request.getOperationType());
		Assert.assertEquals(VsRequestState.IN_PROGRESS, request.getState());

		VsAccountDto account = accountService.findByUidSystem(USER_ONE_NAME, system.getId());
		Assert.assertNull("Account must be null, because request was not realized yet!", account);
		// We try cancel the request
		super.logout();
		loginService.login(new LoginDto(USER_IMPLEMENTER_NAME, new GuardedString("password")));
		request = requestService.cancel(request, reason);
		Assert.assertEquals(VsRequestState.CANCELED, request.getState());
		Assert.assertEquals(reason, request.getReason());
		account = accountService.findByUidSystem(USER_ONE_NAME, system.getId());
		Assert.assertNull("Account must be null, because request was canceled!", account);
	}

	@Test(expected = ForbiddenEntityException.class)
	@Transactional
	public void realizeRequestWithouRightTest() {
		String reason = "cancel \"request\" reason!";

		// We need do commit, without this will be provisioning not executed
		SysSystemDto system = this.getItself().assignRoleSystemInNewTransaction(USER_ONE_NAME, USER_IMPLEMENTER_NAME,
				ROLE_ONE_NAME);

		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(system.getId());
		requestFilter.setUid(USER_ONE_NAME);
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto request = requests.get(0);
		Assert.assertEquals(USER_ONE_NAME, request.getUid());
		Assert.assertEquals(VsOperationType.CREATE, request.getOperationType());
		Assert.assertEquals(VsRequestState.IN_PROGRESS, request.getState());

		VsAccountDto account = accountService.findByUidSystem(USER_ONE_NAME, system.getId());
		Assert.assertNull("Account must be null, because request was not realized yet!", account);
		// We try cancel the request
		super.logout();
		loginService.login(new LoginDto(USER_ONE_NAME, new GuardedString("password")));
		request = requestService.cancel(request, reason);
	}

	@Test
	@Transactional
	public void createMoreRequestsTest() {
		String changed = "changed";

		// We need do commit, without this will be provisioning not executed
		SysSystemDto system = this.getItself().assignRoleSystemInNewTransaction(USER_ONE_NAME, USER_IMPLEMENTER_NAME,
				ROLE_ONE_NAME);
		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(system.getId());
		requestFilter.setUid(USER_ONE_NAME);
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto request = requests.get(0);
		Assert.assertEquals(USER_ONE_NAME, request.getUid());
		Assert.assertEquals(VsOperationType.CREATE, request.getOperationType());
		Assert.assertEquals(VsRequestState.IN_PROGRESS, request.getState());

		VsAccountDto account = accountService.findByUidSystem(USER_ONE_NAME, system.getId());
		Assert.assertNull("Account must be null, because request was not realized yet!", account);

		IdmIdentityDto userOne = identityService.getByUsername(USER_ONE_NAME);
		userOne.setFirstName(changed);
		userOne.setLastName(changed);
		this.getItself().saveInNewTransaction(userOne, identityService);
		// Duplicated save ... not invoke provisioning
		this.getItself().saveInNewTransaction(userOne, identityService);

		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(2, requests.size());
		VsRequestDto changeRequest = requests.stream().filter(
				req -> VsRequestState.IN_PROGRESS == req.getState() && VsOperationType.UPDATE == req.getOperationType())
				.findFirst().orElse(null);
		Assert.assertNotNull("Request with change not found!", changeRequest);
	}

	@Test
	@Transactional
	public void realizeUpdateAndDeleteRequestsTest() {
		String changed = "changed";

		// We need do commit, without this will be provisioning not executed
		SysSystemDto system = this.getItself().assignRoleSystemInNewTransaction(USER_ONE_NAME, USER_IMPLEMENTER_NAME,
				ROLE_ONE_NAME);
		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(system.getId());
		requestFilter.setUid(USER_ONE_NAME);
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto request = requests.get(0);
		Assert.assertEquals(USER_ONE_NAME, request.getUid());
		Assert.assertEquals(VsOperationType.CREATE, request.getOperationType());
		Assert.assertEquals(VsRequestState.IN_PROGRESS, request.getState());

		VsAccountDto account = accountService.findByUidSystem(USER_ONE_NAME, system.getId());
		Assert.assertNull("Account must be null, because request was not realized yet!", account);

		IdmIdentityDto userOne = identityService.getByUsername(USER_ONE_NAME);
		userOne.setFirstName(changed);
		userOne.setLastName(changed);
		this.getItself().saveInNewTransaction(userOne, identityService);
		// Delete identity
		this.getItself().deleteInNewTransaction(userOne, identityService);

		// Test read rights (none requests can be returned for UserOne)
		IdmIdentityDto userTwo = helper.createIdentity("vsUserTwo");
		super.logout();
		loginService.login(new LoginDto(userTwo.getUsername(), new GuardedString("password")));
		requests = requestService.find(requestFilter, null, IdmBasePermission.READ).getContent();
		Assert.assertEquals("We found request without correct rights!", 0, requests.size());
		
		// Test read rights (3 requests must be returned for UserImplementer)
		super.logout();
		loginService.login(new LoginDto(USER_IMPLEMENTER_NAME, new GuardedString("password")));
		requests = requestService.find(requestFilter, null, IdmBasePermission.READ).getContent();
		Assert.assertEquals(3, requests.size());
		VsRequestDto changeRequest = requests.stream().filter(
				req -> VsRequestState.IN_PROGRESS == req.getState() && VsOperationType.UPDATE == req.getOperationType())
				.findFirst().orElse(null);
		Assert.assertNotNull("Request with change not found!", changeRequest);
		VsRequestDto deleteRequest = requests.stream().filter(
				req -> VsRequestState.IN_PROGRESS == req.getState() && VsOperationType.DELETE == req.getOperationType())
				.findFirst().orElse(null);
		Assert.assertNotNull("Request with delete not found!", deleteRequest);
		VsRequestDto createRequest = requests.stream().filter(
				req -> VsRequestState.IN_PROGRESS == req.getState() && VsOperationType.CREATE == req.getOperationType())
				.findFirst().orElse(null);
		Assert.assertNotNull("Request with create not found!", createRequest);

		// Realize create request
		request = requestService.realize(createRequest);
		// Realize update request
		request = requestService.realize(changeRequest);
		// Realize delete request
		request = requestService.realize(deleteRequest);

		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(3, requests.size());
		boolean foundNotRealized = requests.stream().filter(req -> VsRequestState.REALIZED != req.getState())
				.findFirst().isPresent();
		Assert.assertTrue("Found not realized requests!", !foundNotRealized);

	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public <T extends BaseDto> T saveInNewTransaction(final T entity, final ReadWriteDtoService<T, ?> service) {
		return service.save(entity);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public <T extends BaseDto> void deleteInNewTransaction(final T entity, final ReadWriteDtoService<T, ?> service) {
		service.delete(entity);
	}

	/**
	 * Method for create role, user and assign role in own new transaction. We
	 * need do commit, without this will be provisioning not executed
	 * 
	 * @param USER_ONE_NAME
	 * @param USER_IMPLEMENTER_NAME
	 * @param ROLE_ONE_NAME
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public SysSystemDto assignRoleSystemInNewTransaction(String userOneName, String userImplementerName,
			String roleOneName) {
		IdmIdentityDto userImplementer = helper.createIdentity(userImplementerName);
		IdmIdentityDto userOne = helper.createIdentity(userOneName);
		userOne.setPassword(new GuardedString("password"));
		IdmRoleDto roleOne = helper.createRole(roleOneName);

		// Create policy for vs evaluator and user role
		helper.createSpecificPolicy(roleService.getByCode("userRole").getId(), VirtualSystemGroupPermission.VSREQUEST,
				VsRequest.class, VsRequestByImplementerEvaluator.class.getName(), IdmBasePermission.ADMIN);

		VsSystemDto config = new VsSystemDto();
		config.setName("vsSystemOne" + new Date().getTime());
		config.setImplementers(ImmutableList.of(userImplementer.getId()));
		SysSystemDto system = helper.createVirtualSystem(config);
		Assert.assertNotNull(system);
		// Assign system to role
		helper.createRoleSystem(roleOne, system);
		helper.assignRoles(helper.getPrimeContract(userOne.getId()), false, roleOne);
		return system;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void deleteAll(String userOneName, String userImplementerName, String roleOneName) {
		if (identityService.getByUsername(userOneName) != null) {
			identityService.delete(identityService.getByUsername(userOneName));
		}
		if (identityService.getByUsername(userImplementerName) != null) {
			identityService.delete(identityService.getByUsername(userImplementerName));
		}
		if (roleService.getByCode(roleOneName) != null) {
			roleService.delete(roleService.getByCode(roleOneName));
		}
	}

	public VsReqeustServiceTest getItself() {
		if (this.itself == null) {
			this.itself = this.applicationContext.getBean(VsReqeustServiceTest.class);
		}
		return this.itself;
	}

}
