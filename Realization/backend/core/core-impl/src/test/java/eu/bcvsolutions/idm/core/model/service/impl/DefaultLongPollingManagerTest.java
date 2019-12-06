package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.Assert;
import org.springframework.web.context.request.async.DeferredResult;

import eu.bcvsolutions.idm.core.AbstractCoreWorkflowIntegrationTest;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.CheckLongPollingResult;
import eu.bcvsolutions.idm.core.model.service.api.LongPollingManager;
import eu.bcvsolutions.idm.core.rest.DeferredResultWrapper;
import eu.bcvsolutions.idm.core.rest.LongPollingSubscriber;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Test for long polling manager
 *
 * @author Vít Švanda
 *
 */
public class DefaultLongPollingManagerTest extends AbstractCoreWorkflowIntegrationTest {

	@Autowired
	protected TestHelper helper;
	@Autowired
	private LongPollingManager longPollingManager;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	@Lazy
	private ConfigurationService configurationService;

	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testDisableLongPolling() {
		// Long polling is enabled by default
		assertTrue("Long polling should be enabled by default!", longPollingManager.isLongPollingEnabled());
		configurationService.setValue(LongPollingManager.LONG_POLLING_ENABLED_KEY, "false");
		assertFalse(longPollingManager.isLongPollingEnabled());
	}

	@Test
	public void testGetLastTimeStamp() {
		IdmIdentityDto identityOne = new IdmIdentityDto();

		identityOne.setCreated(ZonedDateTime.now().minusMinutes(10));
		identityOne.setModified(ZonedDateTime.now().minusMinutes(100));
		ZonedDateTime lastTimeStamp = longPollingManager.getLastTimeStamp(identityOne);
		assertEquals(identityOne.getCreated().plus(1, ChronoUnit.MILLIS), lastTimeStamp);

		identityOne.setCreated(ZonedDateTime.now().minusMinutes(100));
		identityOne.setModified(ZonedDateTime.now().minusMinutes(10));
		lastTimeStamp = longPollingManager.getLastTimeStamp(identityOne);
		assertEquals(identityOne.getModified().plus(1, ChronoUnit.MILLIS), lastTimeStamp);

		identityOne.setCreated(ZonedDateTime.now().minusMinutes(100));
		identityOne.setModified(null);
		lastTimeStamp = longPollingManager.getLastTimeStamp(identityOne);
		assertEquals(identityOne.getCreated().plus(1, ChronoUnit.MILLIS), lastTimeStamp);

		identityOne.setCreated(null);
		identityOne.setModified(ZonedDateTime.now().minusMinutes(100));
		lastTimeStamp = longPollingManager.getLastTimeStamp(identityOne);
		assertEquals(identityOne.getModified().plus(1, ChronoUnit.MILLIS), lastTimeStamp);
	}

	@Test
	public void testRegisteredSubscribers() {
		DefaultLongPollingManager defaultPollingManager = (DefaultLongPollingManager) longPollingManager;
		// Clear deferred result and subscribers
		defaultPollingManager.getSuspendedRequests().clear();
		defaultPollingManager.getRegistredSubscribers().clear();

		IdmIdentityDto identityOne = this.getHelper().createIdentity();

		DeferredResult<OperationResultDto> result = new DeferredResult<OperationResultDto>(10000l,
				new OperationResultDto(OperationState.NOT_EXECUTED));
		DeferredResultWrapper wrapper = new DeferredResultWrapper(identityOne.getId(), identityOne.getClass(), result);
		wrapper.onCheckResultCallback(new CheckLongPollingResult() {

			@Override
			public void checkDeferredResult(DeferredResult<OperationResultDto> result,
					LongPollingSubscriber subscriber) {
				checkDeferredRequest(result, subscriber);
			}
		});

		longPollingManager.addSuspendedResult(wrapper);

		Map<UUID, LongPollingSubscriber> registredSubscirbers = defaultPollingManager.getRegistredSubscribers();

		assertEquals(1, registredSubscirbers.size());
		assertTrue(registredSubscirbers.containsKey(identityOne.getId()));

		longPollingManager.checkDeferredRequests(IdmIdentityDto.class);

		// None subscriber will be cleared ... threshold time stamp is too small
		longPollingManager.clearUnUseSubscribers(ZonedDateTime.now().minusMinutes(1));
		registredSubscirbers = defaultPollingManager.getRegistredSubscribers();
		assertEquals(1, registredSubscirbers.size());

		longPollingManager.clearUnUseSubscribers(ZonedDateTime.now().plusMinutes(1));
		registredSubscirbers = defaultPollingManager.getRegistredSubscribers();
		assertEquals(0, registredSubscirbers.size());

		// Clear deferred result and subscribers
		defaultPollingManager.getSuspendedRequests().clear();
		defaultPollingManager.getRegistredSubscribers().clear();
	}

	@Test
	public void testExecuteDeferredResultOnCreateRoleRequest() throws InterruptedException {
		DefaultLongPollingManager defaultPollingManager = (DefaultLongPollingManager) longPollingManager;
		// Clear deferred result and subscribers
		defaultPollingManager.getSuspendedRequests().clear();
		defaultPollingManager.getRegistredSubscribers().clear();

		IdmIdentityDto identityOne = this.getHelper().createIdentity();

		DeferredResult<OperationResultDto> result = new DeferredResult<OperationResultDto>(10000l,
				new OperationResultDto(OperationState.NOT_EXECUTED));
		DeferredResultWrapper wrapper = new DeferredResultWrapper(identityOne.getId(), identityOne.getClass(), result);
		wrapper.onCheckResultCallback(new CheckLongPollingResult() {

			@Override
			public void checkDeferredResult(DeferredResult<OperationResultDto> result,
					LongPollingSubscriber subscriber) {
				checkDeferredRequest(result, subscriber);
			}
		});

		Queue<DeferredResultWrapper> suspendedRequests = defaultPollingManager.getSuspendedRequests();
		assertEquals(0, suspendedRequests.size());

		longPollingManager.addSuspendedResult(wrapper);

		suspendedRequests = defaultPollingManager.getSuspendedRequests();
		assertEquals(1, suspendedRequests.size());

		longPollingManager.checkDeferredRequests(IdmIdentityDto.class);
		suspendedRequests = defaultPollingManager.getSuspendedRequests();
		assertEquals(1, suspendedRequests.size());

		// Create role-request -> must be detected
		this.getHelper().createRoleRequest(identityOne, this.getHelper().createRole());

		// Check must be called twice, because first detect the change and second remove
		// ended deferred result (from some reason is not invoked method
		// result.onCompleted)
		longPollingManager.checkDeferredRequests(IdmIdentityDto.class);
		longPollingManager.checkDeferredRequests(IdmIdentityDto.class);

		suspendedRequests = defaultPollingManager.getSuspendedRequests();
		assertEquals(0, suspendedRequests.size());

		// Clear deferred result and subscribers
		defaultPollingManager.getSuspendedRequests().clear();
		defaultPollingManager.getRegistredSubscribers().clear();
	}

	@Test
	public void testExecuteDeferredResultOnUpdateRoleRequest() {
		DefaultLongPollingManager defaultPollingManager = (DefaultLongPollingManager) longPollingManager;
		// Clear deferred result and subscribers
		defaultPollingManager.getSuspendedRequests().clear();
		defaultPollingManager.getRegistredSubscribers().clear();

		IdmIdentityDto identityOne = this.getHelper().createIdentity();
		IdmRoleRequestDto roleRequest = this.getHelper().createRoleRequest(identityOne, this.getHelper().createRole());

		DeferredResult<OperationResultDto> result = new DeferredResult<OperationResultDto>(10000l,
				new OperationResultDto(OperationState.NOT_EXECUTED));
		DeferredResultWrapper wrapper = new DeferredResultWrapper(identityOne.getId(), identityOne.getClass(), result);
		wrapper.onCheckResultCallback(new CheckLongPollingResult() {

			@Override
			public void checkDeferredResult(DeferredResult<OperationResultDto> result,
					LongPollingSubscriber subscriber) {
				checkDeferredRequest(result, subscriber);
			}
		});

		Queue<DeferredResultWrapper> suspendedRequests = defaultPollingManager.getSuspendedRequests();
		assertEquals(0, suspendedRequests.size());

		longPollingManager.addSuspendedResult(wrapper);

		suspendedRequests = defaultPollingManager.getSuspendedRequests();
		assertEquals(1, suspendedRequests.size());

		longPollingManager.checkDeferredRequests(IdmIdentityDto.class);
		suspendedRequests = defaultPollingManager.getSuspendedRequests();
		assertEquals(1, suspendedRequests.size());

		// Update of role-request -> must be detected
		roleRequest.setState(RoleRequestState.IN_PROGRESS);
		roleRequestService.save(roleRequest);

		// Check must be called twice, because first detect the change and second remove
		// ended deferred result (from some reason is not invoked method
		// result.onCompleted)
		longPollingManager.checkDeferredRequests(IdmIdentityDto.class);
		longPollingManager.checkDeferredRequests(IdmIdentityDto.class);

		suspendedRequests = defaultPollingManager.getSuspendedRequests();
		assertEquals(0, suspendedRequests.size());

		// Clear deferred result and subscribers
		defaultPollingManager.getSuspendedRequests().clear();
		defaultPollingManager.getRegistredSubscribers().clear();
	}

	@Test
	public void testExecuteDeferredResultOnDeleteRoleRequest() {
		DefaultLongPollingManager defaultPollingManager = (DefaultLongPollingManager) longPollingManager;
		// Clear deferred result and subscribers
		defaultPollingManager.getSuspendedRequests().clear();
		defaultPollingManager.getRegistredSubscribers().clear();

		IdmIdentityDto identityOne = this.getHelper().createIdentity();
		IdmRoleRequestDto roleRequest = this.getHelper().createRoleRequest(identityOne, this.getHelper().createRole());

		DeferredResult<OperationResultDto> result = new DeferredResult<OperationResultDto>(10000l,
				new OperationResultDto(OperationState.NOT_EXECUTED));
		DeferredResultWrapper wrapper = new DeferredResultWrapper(identityOne.getId(), identityOne.getClass(), result);
		wrapper.onCheckResultCallback(new CheckLongPollingResult() {

			@Override
			public void checkDeferredResult(DeferredResult<OperationResultDto> result,
					LongPollingSubscriber subscriber) {
				checkDeferredRequest(result, subscriber);
			}
		});

		Queue<DeferredResultWrapper> suspendedRequests = defaultPollingManager.getSuspendedRequests();
		assertEquals(0, suspendedRequests.size());

		longPollingManager.addSuspendedResult(wrapper);

		suspendedRequests = defaultPollingManager.getSuspendedRequests();
		assertEquals(1, suspendedRequests.size());

		longPollingManager.checkDeferredRequests(IdmIdentityDto.class);
		suspendedRequests = defaultPollingManager.getSuspendedRequests();
		assertEquals(1, suspendedRequests.size());

		// Delete of the role-request -> must be detected
		roleRequestService.delete(roleRequest);

		// Check must be called twice, because first detect the change and second remove
		// ended deferred result (from some reason is not invoked method
		// result.onCompleted)
		longPollingManager.checkDeferredRequests(IdmIdentityDto.class);
		longPollingManager.checkDeferredRequests(IdmIdentityDto.class);

		suspendedRequests = defaultPollingManager.getSuspendedRequests();
		assertEquals(0, suspendedRequests.size());

		// Clear deferred result and subscribers
		defaultPollingManager.getSuspendedRequests().clear();
		defaultPollingManager.getRegistredSubscribers().clear();
	}

	private void checkDeferredRequest(DeferredResult<OperationResultDto> deferredResult,
			LongPollingSubscriber subscriber) {
		Assert.notNull(deferredResult, "Deffered result is required.");
		Assert.notNull(subscriber.getEntityId(), "Entity identifier is required.");

		IdmRoleRequestFilter filter = new IdmRoleRequestFilter();
		filter.setApplicantId(subscriber.getEntityId());

		longPollingManager.baseCheckDeferredResult(deferredResult, subscriber, filter, roleRequestService, true);
	}

}
