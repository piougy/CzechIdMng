package eu.bcvsolutions.idm.vs.bulk.action.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import eu.bcvsolutions.idm.InitApplicationData;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;
import eu.bcvsolutions.idm.vs.TestHelper;
import eu.bcvsolutions.idm.vs.domain.VsOperationType;
import eu.bcvsolutions.idm.vs.domain.VsRequestState;
import eu.bcvsolutions.idm.vs.dto.VsAccountDto;
import eu.bcvsolutions.idm.vs.dto.VsRequestDto;
import eu.bcvsolutions.idm.vs.dto.VsSystemDto;
import eu.bcvsolutions.idm.vs.dto.filter.VsRequestFilter;
import eu.bcvsolutions.idm.vs.entity.VsRequest;
import eu.bcvsolutions.idm.vs.service.api.VsAccountService;
import eu.bcvsolutions.idm.vs.service.api.VsRequestService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test for cancel of VS request bulk action.
 *
 * @author Vít Švanda
 */
public class VsRequestCancelBulkActionTest extends AbstractBulkActionTest {

	@Autowired
	private TestHelper helper;
	@Autowired
	private VsRequestService requestService;
	@Autowired
	private VsAccountService accountService;

	@Before
	public void init() {
		loginAsAdmin(InitApplicationData.ADMIN_USERNAME);
	}


	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testCancelRequest() {

		IdmIdentityDto implementer = helper.createIdentity();
		IdmIdentityDto identity = helper.createIdentity();
		IdmRoleDto roleForVs = helper.createRole();
		SysSystemDto system = this.createVirtualSystem(implementer, null);
		helper.createRoleSystem(roleForVs, system);
		helper.createIdentityRole(identity, roleForVs);

		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(system.getId());
		requestFilter.setUid(identity.getUsername());
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto request = requests.get(0);
		Assert.assertEquals(identity.getUsername(), request.getUid());
		Assert.assertEquals(VsOperationType.CREATE, request.getOperationType());
		Assert.assertEquals(VsRequestState.IN_PROGRESS, request.getState());

		VsAccountDto account = accountService.findByUidSystem(identity.getUsername(), system.getId());
		Assert.assertNull("Account must be null, because request was not realized yet!", account);
		// We try cancel the request
		IdmBulkActionDto bulkAction = this.findBulkAction(VsRequest.class, VsRequestCancelBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(request.getId()));

		String reason = helper.createName();
		Map<String, Object> properties = new HashMap<>();
		properties.put(VsRequestCancelBulkAction.REASON, reason);
		bulkAction.setProperties(properties);

		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1L, null, null);

		request = requestService.get(request.getId());
		Assert.assertEquals(VsRequestState.CANCELED, request.getState());
		Assert.assertEquals(reason, request.getReason());
		account = accountService.findByUidSystem(identity.getUsername(), system.getId());
		Assert.assertNull("Account must be null, because request was canceled!", account);
	}

	@Test(expected = ResultCodeException.class)
	public void testCancelRequestWithoutReason() {

		IdmIdentityDto implementer = helper.createIdentity();
		IdmIdentityDto identity = helper.createIdentity();
		IdmRoleDto roleForVs = helper.createRole();
		SysSystemDto system = this.createVirtualSystem(implementer, null);
		helper.createRoleSystem(roleForVs, system);
		helper.createIdentityRole(identity, roleForVs);

		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(system.getId());
		requestFilter.setUid(identity.getUsername());
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto request = requests.get(0);
		Assert.assertEquals(identity.getUsername(), request.getUid());
		Assert.assertEquals(VsOperationType.CREATE, request.getOperationType());
		Assert.assertEquals(VsRequestState.IN_PROGRESS, request.getState());

		VsAccountDto account = accountService.findByUidSystem(identity.getUsername(), system.getId());
		Assert.assertNull("Account must be null, because request was not realized yet!", account);
		// We try cancel the request
		IdmBulkActionDto bulkAction = this.findBulkAction(VsRequest.class, VsRequestCancelBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(request.getId()));

		// Reason is null -> request cannot be cancelled.
		Map<String, Object> properties = new HashMap<>();
		properties.put(VsRequestCancelBulkAction.REASON, null);
		bulkAction.setProperties(properties);

		bulkActionManager.processAction(bulkAction);
	}

	public SysSystemDto createVirtualSystem(IdmIdentityDto userImplementerName, List<String> attributes) {
		VsSystemDto config = new VsSystemDto();
		config.setName(helper.createName());
		config.setImplementers(ImmutableList.of(userImplementerName.getId()));
		if (attributes != null) {
			config.setAttributes(attributes);
		}
		SysSystemDto system = helper.createVirtualSystem(config);
		Assert.assertNotNull(system);
		return system;
	}
}
