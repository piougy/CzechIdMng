package eu.bcvsolutions.idm.core.model.event.processor;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.TestHelper;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityMonitoredFieldsProcessor;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.core.notification.repository.IdmEmailLogRepository;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationLogRepository;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationLogService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Check if defined fields on identity was changed. If yes, then send
 * notification.
 * 
 * @author Svanda
 *
 */
public class IdentityMonitoredFieldsProcessorTest extends AbstractIntegrationTest {
	private static final String PROCESSOR_KEY = "idm.sec.core.processor." + IdentityMonitoredFieldsProcessor.PROCESSOR_NAME
			+ ".";

	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private IdmNotificationLogService notificationLogService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private TestHelper helper;
	@Autowired
	private IdmNotificationLogRepository idmNotificationRepository;
	@Autowired
	private IdmEmailLogRepository emailLogRepository;

	@Before
	public void login() {
		super.loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);

		emailLogRepository.deleteAll();
		idmNotificationRepository.deleteAll();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void sendNotificationTest() {
		configurationService.setValue(PROCESSOR_KEY + "enabled", Boolean.TRUE.toString());
		configurationService.setValue(PROCESSOR_KEY + IdentityMonitoredFieldsProcessor.PROPERTY_MONITORED_FIELDS,
				"firstName");
		configurationService.setValue(PROCESSOR_KEY + IdentityMonitoredFieldsProcessor.PROPERTY_RECIPIENTS_ROLE,
				"superAdminRole");
		//
		IdmIdentityDto identity = helper.createIdentity();
		List<IdmIdentityDto> recipients = identityService.findAllByRoleName("superAdminRole");
		Assert.notEmpty(recipients, "Test need some recipients");

		// Test before notify
		NotificationFilter filter = new NotificationFilter();
		filter.setRecipient(recipients.get(0).getUsername());

		identity.setFirstName("changed" + UUID.randomUUID());
		identity.setLastName("changed" + UUID.randomUUID());
		identityService.save(identity);

		// Test after notify
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent().stream().filter(notification -> {
			return notification.getTopic().equals(CoreModuleDescriptor.TOPIC_IDENTITY_MONITORED_CHANGED_FIELDS);
		}).collect(Collectors.toList());

		assertEquals(1, notifications.size());
		assertEquals(CoreModuleDescriptor.TOPIC_IDENTITY_MONITORED_CHANGED_FIELDS, notifications.get(0).getTopic());
		Assert.isTrue(notifications.get(0).getMessage().getHtmlMessage().contains(identity.getFirstName()));
		// Last name is not monitored
		Assert.isTrue(!notifications.get(0).getMessage().getHtmlMessage().contains(identity.getLastName()));
	}

	@Test
	public void changeNotMonitoredFieldTest() {
		configurationService.setValue(PROCESSOR_KEY + "enabled", Boolean.TRUE.toString());
		configurationService.setValue(PROCESSOR_KEY + IdentityMonitoredFieldsProcessor.PROPERTY_MONITORED_FIELDS,
				"firstName");
		configurationService.setValue(PROCESSOR_KEY + IdentityMonitoredFieldsProcessor.PROPERTY_RECIPIENTS_ROLE,
				"superAdminRole");
		//
		IdmIdentityDto identity = helper.createIdentity();
		List<IdmIdentityDto> recipients = identityService.findAllByRoleName("superAdminRole");
		Assert.notEmpty(recipients, "Test need some recipients");

		// Test before notify
		NotificationFilter filter = new NotificationFilter();
		filter.setRecipient(recipients.get(0).getUsername());
		
		String changedValue = "changed" + UUID.randomUUID();

		// We change not monitored fields
		identity.setLastName(changedValue);
		identity.setDescription(changedValue);
		identityService.save(identity);

		// Test after notify
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent().stream().filter(notification -> {
			return notification.getTopic().equals(CoreModuleDescriptor.TOPIC_IDENTITY_MONITORED_CHANGED_FIELDS);
		}).collect(Collectors.toList());

		// No notification should be sent
		assertEquals(0, notifications.size());
	}

	@Test
	public void sendNotificationDisabledTest() {
		configurationService.setValue(PROCESSOR_KEY + "enabled", Boolean.FALSE.toString());
		configurationService.setValue(PROCESSOR_KEY + IdentityMonitoredFieldsProcessor.PROPERTY_MONITORED_FIELDS,
				"firstName");
		configurationService.setValue(PROCESSOR_KEY + IdentityMonitoredFieldsProcessor.PROPERTY_RECIPIENTS_ROLE,
				"superAdminRole");
		//
		IdmIdentityDto identity = helper.createIdentity();
		List<IdmIdentityDto> recipients = identityService.findAllByRoleName("superAdminRole");
		Assert.notEmpty(recipients, "Test need some recipients");

		// Test before notify
		NotificationFilter filter = new NotificationFilter();
		filter.setRecipient(recipients.get(0).getUsername());

		String changedValue = "changed" + UUID.randomUUID();

		identity.setFirstName(changedValue);
		identityService.save(identity);

		// Test after notify ... must be 0 ... processor is disabled
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent().stream().filter(notification -> {
			return notification.getTopic().equals(CoreModuleDescriptor.TOPIC_IDENTITY_MONITORED_CHANGED_FIELDS);
		}).collect(Collectors.toList());
		//
		assertEquals(0, notifications.size());
	}

	@Test(expected = ResultCodeException.class)
	public void sendNotificationErrorTest() {
		configurationService.setValue(PROCESSOR_KEY + "enabled", Boolean.TRUE.toString());
		configurationService.setValue(PROCESSOR_KEY + IdentityMonitoredFieldsProcessor.PROPERTY_MONITORED_FIELDS,
				"firstName, WRONGFIELD");
		configurationService.setValue(PROCESSOR_KEY + IdentityMonitoredFieldsProcessor.PROPERTY_RECIPIENTS_ROLE,
				"superAdminRole");
		//
		IdmIdentityDto identity = helper.createIdentity();
		List<IdmIdentityDto> recipients = identityService.findAllByRoleName("superAdminRole");
		Assert.notEmpty(recipients, "Test need some recipients");

		// Test before notify
		NotificationFilter filter = new NotificationFilter();
		filter.setRecipient(recipients.get(0).getUsername());
		
		String changedValue = "changed" + UUID.randomUUID();

		identity.setFirstName(changedValue);
		identityService.save(identity);

		// Test after notify ... must be 0 ... processor is disabled
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent().stream().filter(notification -> {
			return notification.getTopic().equals(CoreModuleDescriptor.TOPIC_IDENTITY_MONITORED_CHANGED_FIELDS);
		}).collect(Collectors.toList());
		//
		assertEquals(0, notifications.size());
	}
}
