package eu.bcvsolutions.idm.acc.rest.lookup;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.dto.SysConnectorServerDto;
import eu.bcvsolutions.idm.acc.service.api.SysRemoteServerService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Lookup remote server by example.
 * 
 * 
 * @author Radek Tomiška
 */
@Transactional
public class SysRemoteServerDtoLookupByExampleIntegrationTest extends AbstractIntegrationTest {

	@Autowired private SysRemoteServerService remoteServerService;
	@Autowired private SysRemoteServerDtoLookupByExample lookup;
	
	@Test
	public void testFindByExample() {
		SysConnectorServerDto remoteServer = new SysConnectorServerDto();
		remoteServer.setHost("localhost");
		remoteServer.setPort(100);
		remoteServer.setTimeout(125);
		remoteServer.setUseSsl(true);
		SysConnectorServerDto remoteServerOne = remoteServerService.save(remoteServer);
		//
		remoteServer = new SysConnectorServerDto();
		remoteServer.setHost("localhost");
		remoteServer.setPort(100);
		remoteServer.setTimeout(125);
		remoteServer.setUseSsl(false);
		SysConnectorServerDto remoteServerTwo = remoteServerService.save(remoteServer);
		//
		SysConnectorServerDto example = new SysConnectorServerDto();
		example.setHost("localhost");
		example.setPort(100);
		example.setTimeout(125);
		example.setUseSsl(true);
		Assert.assertEquals(remoteServerOne, lookup.lookup(example));
		//
		example.setUseSsl(false);
		Assert.assertEquals(remoteServerTwo, lookup.lookup(example));
		//
		example.setTimeout(112);
		Assert.assertNull(lookup.lookup(example));
		//
		example.setTimeout(125);
		example.setPort(444);
		Assert.assertNull(lookup.lookup(example));
		//
		example.setPort(100);
		example.setHost("mock");
		Assert.assertNull(lookup.lookup(example));
	}
	
}
