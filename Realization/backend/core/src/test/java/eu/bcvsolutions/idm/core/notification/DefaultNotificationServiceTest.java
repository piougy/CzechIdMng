package eu.bcvsolutions.idm.core.notification;

import static org.testng.AssertJUnit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import eu.bcvsolutions.idm.core.AbstractIntegrationTest;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.core.notification.repository.IdmEmailLogRepository;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationLogRepository;
import eu.bcvsolutions.idm.core.notification.service.NotificationService;
import eu.bcvsolutions.idm.core.security.service.impl.DefaultSecurityService;

/**
 * Test for {@link DefaultSecurityService}
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@Test
public class DefaultNotificationServiceTest extends AbstractIntegrationTest {

	@Autowired
	private NotificationService notificationService;
	
	@Autowired
	private IdmNotificationLogRepository idmNotificationRepository;
	
	@Autowired
	private IdmIdentityRepository identityRepository;
	
	@Autowired
	private IdmEmailLogRepository emailLogRepository;		
	
	@Test
	public void testSendSimple() {
		assertEquals(0, idmNotificationRepository.count());
		
		IdmIdentity identity = identityRepository.findOneByUsername("tomiska");
		
		notificationService.send(new IdmMessage("subject", "Idm notification"),  identity);
		
		assertEquals(1, idmNotificationRepository.count());
		assertEquals(1, emailLogRepository.count());
	}
}
