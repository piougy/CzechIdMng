package eu.bcvsolutions.idm.acc.event.processor.module;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.dto.SysConnectorServerDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRemoteServerFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemFilter;
import eu.bcvsolutions.idm.acc.entity.SysBlockedOperation;
import eu.bcvsolutions.idm.acc.entity.SysConnectorServer;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.repository.SysSystemRepository;
import eu.bcvsolutions.idm.acc.service.api.SysRemoteServerService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.ModuleDescriptorEvent;
import eu.bcvsolutions.idm.core.api.event.ModuleDescriptorEvent.ModuleDescriptorEventType;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test system migration.
 * 
 * @author Radek Tomiška
 *
 */
public class AccInitRemoteServerProcessorIntegrationTest extends AbstractIntegrationTest {

	@Autowired private AccInitRemoteServerProcessor initProcessor;
	@Autowired private SysSystemService systemService;
	@Autowired private SysSystemRepository systemRepository;
	@Autowired private SysRemoteServerService remoteServerService;
	@Autowired private ConfidentialStorage confidentialStorage;
	
	@Test
	public void testInit() {
		String host = getHelper().createName();
		String password = getHelper().createName();
		String description = getHelper().createName();
		//
		SysConnectorServerDto remoteServer = new SysConnectorServerDto();
		remoteServer.setHost(host);
		remoteServer.setPassword(new GuardedString(password));
		remoteServer.setPort(2);
		remoteServer.setTimeout(2);
		remoteServer.setUseSsl(true);
		remoteServerService.save(remoteServer);
		//
		SysRemoteServerFilter filter = new SysRemoteServerFilter();
		filter.setText(host);
		List<SysConnectorServerDto> results = remoteServerService.find(filter, null).getContent();
		Assert.assertEquals(1, results.size());
		SysConnectorServerDto existRemoteServer = results.get(0);
		//
		// wee need to save system old way => repository is used
		SysSystem system = new SysSystem();
		system.setRemote(true);
		system.setName(getHelper().createName());
		system.setDescription(description);
		system.setConnectorServer(new SysConnectorServer());
		system.getConnectorServer().setHost(host);
		system.getConnectorServer().setPassword(new GuardedString(password));
		system.getConnectorServer().setPort(1);
		system.getConnectorServer().setTimeout(2);
		system.getConnectorServer().setUseSsl(true);
		system.setBlockedOperation(new SysBlockedOperation());
		SysSystemDto systemOne = systemService.get(systemRepository.save(system).getId());
		confidentialStorage.saveGuardedString(systemOne.getId(),
				SysSystem.class, SysSystemService.REMOTE_SERVER_PASSWORD, new GuardedString(password));
		//
		system = new SysSystem();
		system.setRemote(true);
		system.setName(getHelper().createName());
		system.setDescription(description);
		system.setConnectorServer(new SysConnectorServer());
		system.getConnectorServer().setHost(host);
		system.getConnectorServer().setPassword(new GuardedString(password));
		system.getConnectorServer().setPort(1);
		system.getConnectorServer().setTimeout(2);
		system.getConnectorServer().setUseSsl(true);
		system.setBlockedOperation(new SysBlockedOperation());
		SysSystemDto systemTwo = systemService.get(systemRepository.save(system).getId());
		confidentialStorage.saveGuardedString(systemTwo.getId(),
				SysSystem.class, SysSystemService.REMOTE_SERVER_PASSWORD, new GuardedString(password));
		//
		system = new SysSystem();
		system.setRemote(true);
		system.setName(getHelper().createName());
		system.setDescription(description);
		system.setConnectorServer(new SysConnectorServer());
		system.getConnectorServer().setHost(host);
		String differentPassword = getHelper().createName();
		system.getConnectorServer().setPassword(new GuardedString(differentPassword)); // different password
		system.getConnectorServer().setPort(1);
		system.getConnectorServer().setTimeout(2);
		system.getConnectorServer().setUseSsl(true);
		system.setBlockedOperation(new SysBlockedOperation());
		SysSystemDto systemThree = systemService.get(systemRepository.save(system).getId());
		confidentialStorage.saveGuardedString(systemThree.getId(),
				SysSystem.class, SysSystemService.REMOTE_SERVER_PASSWORD, new GuardedString(differentPassword));
		//
		system = new SysSystem();
		system.setRemote(true);
		system.setName(getHelper().createName());
		system.setDescription(description);
		system.setConnectorServer(new SysConnectorServer());
		system.getConnectorServer().setHost(host);
		system.getConnectorServer().setPassword(new GuardedString(password));
		system.getConnectorServer().setPort(1);
		system.getConnectorServer().setTimeout(2);
		system.getConnectorServer().setUseSsl(false); // useSsl - different
		system.setBlockedOperation(new SysBlockedOperation());
		SysSystemDto systemFour = systemService.get(systemRepository.save(system).getId());
		confidentialStorage.saveGuardedString(systemFour.getId(),
				SysSystem.class, SysSystemService.REMOTE_SERVER_PASSWORD, new GuardedString(password));
		//
		system = new SysSystem();
		system.setRemote(true);
		system.setName(getHelper().createName());
		system.setDescription(description);
		system.setConnectorServer(new SysConnectorServer());
		system.getConnectorServer().setHost(host);
		system.getConnectorServer().setPassword(new GuardedString(password));
		system.getConnectorServer().setPort(2);
		system.getConnectorServer().setTimeout(2);
		system.getConnectorServer().setUseSsl(true); // useSsl - different
		system.setBlockedOperation(new SysBlockedOperation());
		SysSystemDto systemFive = systemService.get(systemRepository.save(system).getId());
		confidentialStorage.saveGuardedString(systemFive.getId(),
				SysSystem.class, SysSystemService.REMOTE_SERVER_PASSWORD, new GuardedString(password));
		//
		SysSystemFilter systemFilter = new SysSystemFilter();
		systemFilter.setText(description);
		List<SysSystemDto> systems = systemService.find(systemFilter, null).getContent();
		Assert.assertEquals(5, systems.size());
		Assert.assertTrue(systems.stream().allMatch(s -> s.isRemote()));
		Assert.assertTrue(systems.stream().allMatch(s -> s.getRemoteServer() == null));
		//
		// process migration
		initProcessor.process(new ModuleDescriptorEvent(
				ModuleDescriptorEventType.INIT, 
				new ModuleDescriptorDto(AccModuleDescriptor.MODULE_ID)
		));
		//
		systems = systemService.find(systemFilter, null).getContent();
		Assert.assertTrue(systems.stream().allMatch(s -> s.isRemote()));
		Assert.assertTrue(systems.stream().allMatch(s -> s.getRemoteServer() != null));
		results = remoteServerService.find(filter, null).getContent();
		Assert.assertEquals(4, results.size());
		//
		// one - two => same
		systemOne = systemService.get(systemOne);
		systemTwo = systemService.get(systemTwo);
		Assert.assertEquals(systemOne.getRemoteServer(), systemTwo.getRemoteServer());
		Assert.assertEquals(password, confidentialStorage.getGuardedString(systemOne.getId(),
				SysSystem.class, SysSystemService.REMOTE_SERVER_PASSWORD).asString());
		Assert.assertEquals(password, confidentialStorage.getGuardedString(systemTwo.getId(),
				SysSystem.class, SysSystemService.REMOTE_SERVER_PASSWORD).asString());
		Assert.assertEquals(password, remoteServerService.getPassword(systemTwo.getRemoteServer()).asString());
		//
		// three - different password
		systemThree = systemService.get(systemThree);
		Assert.assertEquals(differentPassword, confidentialStorage.getGuardedString(systemThree.getId(),
				SysSystem.class, SysSystemService.REMOTE_SERVER_PASSWORD).asString());
		Assert.assertEquals(differentPassword, remoteServerService.getPassword(systemThree.getRemoteServer()).asString());
		Assert.assertNotEquals(systemOne.getRemoteServer(), systemThree.getRemoteServer());
		//
		// four - different ssl
		systemFour = systemService.get(systemFour);
		Assert.assertEquals(password, confidentialStorage.getGuardedString(systemOne.getId(),
				SysSystem.class, SysSystemService.REMOTE_SERVER_PASSWORD).asString());
		Assert.assertEquals(password, remoteServerService.getPassword(systemFour.getRemoteServer()).asString());
		Assert.assertNotEquals(systemOne.getRemoteServer(), systemFour.getRemoteServer());
		Assert.assertNotEquals(systemThree.getRemoteServer(), systemFour.getRemoteServer());
		//
		// five - exists
		systemFive = systemService.get(systemFive);
		Assert.assertEquals(existRemoteServer.getId(), systemFive.getRemoteServer());
	}
}
