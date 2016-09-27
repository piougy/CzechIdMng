package eu.bcvsolutions.idm.notification;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.AbstractIntegrationTest;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.notification.repository.IdmConsoleLogRepository;
import eu.bcvsolutions.idm.notification.repository.IdmEmailLogRepository;
import eu.bcvsolutions.idm.notification.repository.IdmNotificationLogRepository;
import eu.bcvsolutions.idm.notification.service.EmailService;
import eu.bcvsolutions.idm.notification.service.NotificationService;
import eu.bcvsolutions.idm.security.service.impl.DefaultSecurityService;

/**
 * Test for {@link DefaultSecurityService}
 * 
 * @author Radek Tomiška 
 *
 */
public class DefaultNotificationServiceTest extends AbstractIntegrationTest {

	@Autowired
	private NotificationService notificationService;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private IdmNotificationLogRepository idmNotificationRepository;
	
	@Autowired
	private IdmIdentityRepository identityRepository;
	
	@Autowired
	private IdmEmailLogRepository emailLogRepository;	
	
	@Autowired
	private IdmConsoleLogRepository consoleLogRepository;
	
	@Before
	public void clear() {
		loginAsAdmin("admin");
		emailLogRepository.deleteAll();
		consoleLogRepository.deleteAll();
		idmNotificationRepository.deleteAll();
	}
	
	@After
	@Override
	public void logout() {
		super.logout();
	}
	
	@Test
	@Transactional
	public void testSendSimple() {
		assertEquals(0, idmNotificationRepository.count());
		
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		
		notificationService.send(new IdmMessage("subject", "Idm notification"),  identity);
		
		assertEquals(1, idmNotificationRepository.count());
		assertEquals(1, emailLogRepository.count());
	}
	
	@Test
	public void testFilterByDate() {
		assertEquals(0, idmNotificationRepository.count());
		
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		
		Date start = new Date();
		notificationService.send(new IdmMessage("subject", "Idm notification"),  identity);		
		Date middle = new Date();		
		notificationService.send(new IdmMessage("subject2", "Idm notification2"),  identity);
		Date after = new Date();		
		
		assertEquals(2, idmNotificationRepository.findByQuick(null, null, null, null, null, null, null).getTotalElements());
		assertEquals(2, idmNotificationRepository.findByQuick(null, null, null, null, start, null, null).getTotalElements());
		assertEquals(0, idmNotificationRepository.findByQuick(null, null, null, null, null, start, null).getTotalElements());
	}
	
	@Test
	@Transactional
	public void testEmailFilterBySender() {
		
		assertEquals(0, emailLogRepository.findByQuick(null, InitTestData.TEST_USER_2, null, null, null, null, null).getTotalElements());
		assertEquals(0, emailLogRepository.findByQuick(null, InitTestData.TEST_USER_1, null, null, null, null, null).getTotalElements());
		
		// send some email
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		IdmIdentity identity2 = identityRepository.findOneByUsername(InitTestData.TEST_USER_2);
		emailService.send(new IdmMessage("subject", "Idm notification"),  identity);
		assertEquals(1, emailLogRepository.findByQuick(null, null, null, null, null, null, null).getTotalElements());
		assertEquals(0, emailLogRepository.findByQuick(null, null, identity2.getUsername(), null, null, null, null).getTotalElements());
		assertEquals(1, emailLogRepository.findByQuick(null, null, identity.getUsername(), null, null, null, null).getTotalElements());
	}
	
	@Test
	@Transactional
	public void testEmailFilterBySent() {
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		emailService.send(new IdmMessage("subject", "Idm notification"),  identity);
		assertEquals(0, emailLogRepository.findByQuick(null, null, null, true, null, null, null).getTotalElements());
		
		emailService.send(new IdmMessage("subject2", "Idm notification2"),  identity);
		assertEquals(2, emailLogRepository.findByQuick(null, null, null, false, null, null, null).getTotalElements());
	}
	
	
}
