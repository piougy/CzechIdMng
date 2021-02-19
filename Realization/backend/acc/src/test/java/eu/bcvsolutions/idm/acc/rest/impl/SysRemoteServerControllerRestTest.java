package eu.bcvsolutions.idm.acc.rest.impl;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.dto.SysConnectorServerDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRemoteServerFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Controller tests:
 * - CRUD
 * - filters
 * 
 * @author Radek Tomiška
 *
 */
public class SysRemoteServerControllerRestTest extends AbstractReadWriteDtoControllerRestTest<SysConnectorServerDto> {

	@Autowired private SysRemoteServerController controller;
	
	@Override
	protected AbstractReadWriteDtoController<SysConnectorServerDto, ?> getController() {
		return controller;
	}

	@Override
	protected SysConnectorServerDto prepareDto() {
		SysConnectorServerDto dto = new SysConnectorServerDto();
		dto.setHost(getHelper().createName());
		dto.setPort(80);
		dto.setLocal(false);
		//
		return dto;
	}
	
	@Test
	public void testFindByText() {		
		String text = getHelper().createName();
		SysConnectorServerDto remoteServer = prepareDto();
		remoteServer.setHost(text);
		remoteServer.setPassword(new GuardedString(getHelper().createName()));
		remoteServer.setDescription(getHelper().createName());
		SysConnectorServerDto remoteServerOne = createDto(remoteServer);
		remoteServer = prepareDto();
		remoteServer.setHost(getHelper().createName());
		remoteServer.setPassword(new GuardedString(getHelper().createName()));
		remoteServer.setDescription(text);
		SysConnectorServerDto remoteServerTwo = createDto(remoteServer);
		createDto(); // other
		//
		SysRemoteServerFilter filter = new SysRemoteServerFilter();
		filter.setText(text);
		List<SysConnectorServerDto> results = find(filter);
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().allMatch(r -> r.getPassword().asString().equals(GuardedString.SECRED_PROXY_STRING)));
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(remoteServerOne.getId())));
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(remoteServerTwo.getId())));
	}
	
	@Test
	public void testGetConnectorFrameworks() throws Exception {		
		String text = getHelper().createName();
		SysConnectorServerDto remoteServer = prepareDto();
		remoteServer.setHost(text);
		remoteServer.setPassword(new GuardedString(getHelper().createName()));
		remoteServer.setDescription(getHelper().createName());
		remoteServer = createDto(remoteServer);
		//
		// get connectors -> ends with exception => mock connection server 
	
		getMockMvc().perform(get(getDetailUrl(remoteServer.getId()) + "/frameworks")
				.with(authentication(getAdminAuthentication()))
	            .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testGetConnectorTypes() throws Exception {		
		String text = getHelper().createName();
		SysConnectorServerDto remoteServer = prepareDto();
		remoteServer.setHost(text);
		remoteServer.setPassword(new GuardedString(getHelper().createName()));
		remoteServer.setDescription(getHelper().createName());
		remoteServer = createDto(remoteServer);
		//
		// get connectors -> ends with exception => mock connection server 
	
		getMockMvc().perform(get(getDetailUrl(remoteServer.getId()) + "/connector-types")
				.with(authentication(getAdminAuthentication()))
	            .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isBadRequest());
	}
}
