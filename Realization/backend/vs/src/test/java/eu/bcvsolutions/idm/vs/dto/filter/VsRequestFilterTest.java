package eu.bcvsolutions.idm.vs.dto.filter;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.vs.TestHelper;
import eu.bcvsolutions.idm.vs.domain.VsRequestState;
import eu.bcvsolutions.idm.vs.dto.VsRequestDto;
import eu.bcvsolutions.idm.vs.service.api.VsRequestService;

public class VsRequestFilterTest extends AbstractIntegrationTest{
	
	@Autowired
	private VsRequestService requestService;
	
	@Autowired
	private TestHelper helper;

	@Before
	public void login() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void dateTest() {
		SysSystemDto virtualSystem = helper.createVirtualSystem("Vs" + System.currentTimeMillis());
		IdmRoleDto roleOne = helper.createRole("VsRole" + System.currentTimeMillis());
		IdmIdentityDto identity = helper.createIdentity("TestUser" + System.currentTimeMillis());
		
		// Assign system to role
		helper.createRoleSystem(roleOne, virtualSystem);
		helper.assignRoles(helper.getPrimeContract(identity.getId()), false, roleOne);
		
		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(virtualSystem.getId());
		requestFilter.setUid(identity.getUsername());
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		
		requestFilter.setCreatedAfter(new DateTime().minusSeconds(10));
		requestFilter.setCreatedBefore(new DateTime());
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		
		requestFilter.setCreatedAfter(new DateTime().plusMinutes(10));
		requestFilter.setCreatedBefore(new DateTime().plusMinutes(11));
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(0, requests.size());
		
		requestFilter.setCreatedAfter(new DateTime().minusMinutes(10));
		requestFilter.setCreatedBefore(new DateTime().minusMinutes(9));
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(0, requests.size());
	}

	@Test
	public void systemTest() {
		SysSystemDto virtualSystem = helper.createVirtualSystem("Vs" + System.currentTimeMillis());
		IdmRoleDto roleOne = helper.createRole("VsRole" + System.currentTimeMillis());
		IdmIdentityDto identity = helper.createIdentity("TestUser" + System.currentTimeMillis());
		IdmIdentityDto identity2 = helper.createIdentity("TestUser2" + System.currentTimeMillis());
		IdmIdentityDto identity3 = helper.createIdentity("TestUser3" + System.currentTimeMillis());
		IdmIdentityDto identity4 = helper.createIdentity("TestUser4" + System.currentTimeMillis());
		
		// Assign system to role
		helper.createRoleSystem(roleOne, virtualSystem);
		helper.assignRoles(helper.getPrimeContract(identity.getId()), false, roleOne);
		helper.assignRoles(helper.getPrimeContract(identity2.getId()), false, roleOne);
		
		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(virtualSystem.getId());
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(2, requests.size());
		
		helper.assignRoles(helper.getPrimeContract(identity3.getId()), false, roleOne);
		helper.assignRoles(helper.getPrimeContract(identity4.getId()), false, roleOne);
		
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(4, requests.size());
		
		requestFilter.setUid(identity.getUsername());
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());// identity uid filter test

	}
	
	@Test
	public void filterTest() {
		SysSystemDto virtualSystem = helper.createVirtualSystem("Vs" + System.currentTimeMillis());
		IdmRoleDto roleOne = helper.createRole("VsRole" + System.currentTimeMillis());
		IdmIdentityDto identity = helper.createIdentity("TestUser" + System.currentTimeMillis());
		IdmIdentityDto identity2 = helper.createIdentity("TestUser2" + System.currentTimeMillis());
		IdmIdentityDto identity3 = helper.createIdentity("TestUser3" + System.currentTimeMillis());
		IdmIdentityDto identity4 = helper.createIdentity("TestUser4" + System.currentTimeMillis());
		
		// Assign system to role
		helper.createRoleSystem(roleOne, virtualSystem);
		helper.assignRoles(helper.getPrimeContract(identity.getId()), false, roleOne);
		helper.assignRoles(helper.getPrimeContract(identity2.getId()), false, roleOne);
		helper.assignRoles(helper.getPrimeContract(identity3.getId()), false, roleOne);
		helper.assignRoles(helper.getPrimeContract(identity4.getId()), false, roleOne);
		
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(virtualSystem.getId());
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(4, requests.size());

		VsRequestDto request = requests.get(0);
		requestService.realize(request);
		Assert.assertEquals(VsRequestState.REALIZED, request.getState());
		
		requestFilter.setOnlyArchived(true);
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		
		requestFilter.setOnlyArchived(null);
		requestFilter.setState(VsRequestState.IN_PROGRESS);
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(3, requests.size());
		
		requestFilter.setConnectorKey(request.getConnectorKey());
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(3, requests.size());
	}
	
}
