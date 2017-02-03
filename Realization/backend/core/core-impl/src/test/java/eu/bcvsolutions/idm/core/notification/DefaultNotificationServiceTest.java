package eu.bcvsolutions.idm.core.notification;

import static org.junit.Assert.assertEquals;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.core.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationConfiguration;
import eu.bcvsolutions.idm.core.notification.repository.IdmEmailLogRepository;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationLogRepository;
import eu.bcvsolutions.idm.core.notification.service.api.EmailNotificationSender;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationConfigurationService;
import eu.bcvsolutions.idm.core.notification.service.api.NotificationManager;
import eu.bcvsolutions.idm.core.security.service.impl.DefaultSecurityService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for {@link DefaultSecurityService}
 * 
 * @author Radek Tomiška 
 *
 */
public class DefaultNotificationServiceTest extends AbstractIntegrationTest {

	private static final String TOPIC = "idm:test";
	
	@Autowired
	private NotificationManager notificationManager;
	
	@Autowired
	private EmailNotificationSender emailService;
	
	@Autowired
	private IdmNotificationLogRepository idmNotificationRepository;
	
	@Autowired
	private IdmIdentityRepository identityRepository;
	
	@Autowired
	private IdmEmailLogRepository emailLogRepository;
	
	@Autowired
	private IdmNotificationConfigurationService notificationConfigurationService;
	IdmNotificationConfiguration config = null;
	
	@Before
	public void clear() {
		loginAsAdmin("admin");
		emailLogRepository.deleteAll();
		idmNotificationRepository.deleteAll();
		//
		config = new IdmNotificationConfiguration();
		config.setTopic(TOPIC);
		config.setNotificationType(IdmEmailLog.NOTIFICATION_TYPE);
		config = notificationConfigurationService.save(config);
	}
	
	@After
	@Override
	public void logout() {
		notificationConfigurationService.delete(config);
		super.logout();
	}
	
	@Test
	@Transactional
	public void testSendSimple() {
		assertEquals(0, idmNotificationRepository.count());
		
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		
		notificationManager.send(TOPIC, new IdmMessage.Builder().setSubject("subject").setTextMessage("Idm notification").build(),  identity);
		
		assertEquals(1, idmNotificationRepository.count());
		assertEquals(1, emailLogRepository.count());
	}
	
	@Test
	@Transactional
	public void testFilterByDate() {
		assertEquals(0, idmNotificationRepository.count());
		
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		
		DateTime start = new DateTime();
		notificationManager.send(TOPIC, new IdmMessage.Builder().setSubject("subject").setTextMessage("Idm notification").build(),  identity);		
		notificationManager.send(TOPIC, new IdmMessage.Builder().setSubject("subject2").setTextMessage("Idm notification2").build(),  identity);	
		
		NotificationFilter filter = new NotificationFilter();
		assertEquals(2, idmNotificationRepository.find(filter, null).getTotalElements());
		
		filter.setFrom(start);
		assertEquals(2, idmNotificationRepository.find(filter, null).getTotalElements());
		
		filter.setFrom(null);
		filter.setTill(start);
		assertEquals(0, idmNotificationRepository.find(filter, null).getTotalElements());
	}
	
	@Test
	@Transactional
	public void testEmailFilterBySender() {
		NotificationFilter filter = new NotificationFilter();
		
		filter.setSender(InitTestData.TEST_USER_2);
		assertEquals(0, emailLogRepository.find(filter, null).getTotalElements());
		filter.setSender(InitTestData.TEST_USER_1);
		assertEquals(0, emailLogRepository.find(filter, null).getTotalElements());
		
		// send some email
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		IdmIdentity identity2 = identityRepository.findOneByUsername(InitTestData.TEST_USER_2);
		emailService.send(TOPIC, new IdmMessage.Builder().setSubject("subject").setTextMessage("Idm notification").build(),  identity);
		
		filter.setSender(null);
		assertEquals(1, emailLogRepository.find(filter, null).getTotalElements());
		filter.setSender(identity2.getUsername());
		assertEquals(0, emailLogRepository.find(filter, null).getTotalElements());
		filter.setSender(null);
		filter.setRecipient(identity.getUsername());
		assertEquals(1, emailLogRepository.find(filter, null).getTotalElements());
	}
	
	@Test
	@Transactional
	public void testEmailFilterBySent() {
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		NotificationFilter filter = new NotificationFilter();
		
		emailService.send(new IdmMessage.Builder().setSubject("subject").setTextMessage("Idm notification").build(),  identity);
		filter.setSent(true);
		assertEquals(0, emailLogRepository.find(filter, null).getTotalElements());
		
		emailService.send(new IdmMessage.Builder().setSubject("subject2").setTextMessage("Idm notification2").build(),  identity);
		filter.setSent(false);
		assertEquals(2, emailLogRepository.find(filter, null).getTotalElements());
	}
	
	
}
