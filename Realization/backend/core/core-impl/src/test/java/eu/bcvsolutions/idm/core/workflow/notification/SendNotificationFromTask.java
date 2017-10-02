package eu.bcvsolutions.idm.core.workflow.notification;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.AbstractCoreWorkflowIntegrationTest;
import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.core.workflow.config.WorkflowConfig;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;

public class SendNotificationFromTask extends AbstractCoreWorkflowIntegrationTest {
	
	public static final String WF_1_ENABLED_PROCESS_KEY = "testNotificationEnable";
	private static final String WF_2_FORM_DISABLED_PROCESS_KEY = "testNotificationDisable";
	private static final String WF_3_DISABLED_PROCESS_KEY = "testNotificationDisableGlobal";
	
	public static final String WF_TEST_IDENTITY_01 = "wfTestUser01";
	private static final String WF_TEST_IDENTITY_02 = "wfTestUser02";
	private static final String WF_TEST_IDENTITY_03 = "wfTestUser03";
	
	@Autowired
	private WorkflowProcessInstanceService processInstanceService;
	
	@Autowired
	private ConfigurationService configurationService;
	
	@Autowired
	private IdmNotificationLogService notificationLogService;
	
	@Autowired
	private IdmIdentityService identityService;
	
	@Before
	public void login() {
		super.loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void sendNotificationWithEnabledForm() {
		configurationService.setValue(WorkflowConfig.SEND_NOTIFICATION_CONFIGURATION_PROPERTY, Boolean.TRUE.toString());
		//
		IdmIdentityDto identity = createIdentity(WF_TEST_IDENTITY_01);
		//
		processInstanceService.startProcess(WF_1_ENABLED_PROCESS_KEY, null, InitTestData.TEST_USER_1, null, null);
		//
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(identity.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		//
		assertEquals(2, notifications.size());
		// two notifications - created + assigned, we didnt know order
		if (notifications.get(0).getTopic().equals(CoreModuleDescriptor.TOPIC_WF_TASK_ASSIGNED)) {
			assertEquals(CoreModuleDescriptor.TOPIC_WF_TASK_ASSIGNED, notifications.get(0).getTopic());
			assertEquals(CoreModuleDescriptor.TOPIC_WF_TASK_CREATED, notifications.get(1).getTopic());
		} else {
			assertEquals(CoreModuleDescriptor.TOPIC_WF_TASK_CREATED, notifications.get(0).getTopic());
			assertEquals(CoreModuleDescriptor.TOPIC_WF_TASK_ASSIGNED, notifications.get(1).getTopic());
		}
	}
	
	@Test
	public void sendNotificationGlobalDisabled() {
		configurationService.setValue(WorkflowConfig.SEND_NOTIFICATION_CONFIGURATION_PROPERTY, Boolean.FALSE.toString());
		//
		IdmIdentityDto identity = createIdentity(WF_TEST_IDENTITY_03);
		//
		processInstanceService.startProcess(WF_3_DISABLED_PROCESS_KEY, null, InitTestData.TEST_USER_1, null, null);
		//
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(identity.getUsername());
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		//
		assertEquals(0, notifications.size());
	}
	
	@Test
	public void sendNotificationWithDisabledForm() {
		configurationService.setValue(WorkflowConfig.SEND_NOTIFICATION_CONFIGURATION_PROPERTY, Boolean.TRUE.toString());
		//
		IdmIdentityDto identity = createIdentity(WF_TEST_IDENTITY_02);
		//
		processInstanceService.startProcess(WF_2_FORM_DISABLED_PROCESS_KEY, null, InitTestData.TEST_USER_1, null, null);
		//
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(identity.getUsername());
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		//
		assertEquals(0, notifications.size());
	}
	
	private IdmIdentityDto createIdentity(String username) {
		IdmIdentityDto entity = identityService.getByUsername(username);
		//
		if (entity != null) {
			return entity;
		} else {
			entity = new IdmIdentityDto();
		}
		//
		entity.setUsername(username);
		entity.setFirstName(username);
		entity.setLastName(username);
		return identityService.save(entity);
	}
}
