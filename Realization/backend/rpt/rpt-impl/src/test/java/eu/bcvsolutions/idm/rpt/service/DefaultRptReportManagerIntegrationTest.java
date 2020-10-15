package eu.bcvsolutions.idm.rpt.service;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationState;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationAttachmentDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationAttachmentFilter;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationRecipientFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationAttachmentService;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationConfigurationService;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationRecipientService;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.core.scheduler.ObserveLongRunningTaskEndProcessor;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.SimpleTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulerManager;
import eu.bcvsolutions.idm.rpt.RptModuleDescriptor;
import eu.bcvsolutions.idm.rpt.api.dto.RptRenderedReportDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportExecutorDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportRendererDto;
import eu.bcvsolutions.idm.rpt.api.dto.filter.RptReportFilter;
import eu.bcvsolutions.idm.rpt.api.executor.AbstractReportExecutor;
import eu.bcvsolutions.idm.rpt.api.service.RptReportService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

public class DefaultRptReportManagerIntegrationTest extends AbstractIntegrationTest {

	@Autowired private ApplicationContext context;
	@Autowired private ObjectMapper mapper;
	@Autowired private AttachmentManager attachmentManager;
	@Autowired private ConfigurationService configurationService;
	@Autowired private SchedulerManager schedulerManager;
	@Autowired private RptReportService reportService;
	@Autowired private TestFilterReportExecutor testFilterReportExecutor;
	@Autowired private IdmNotificationLogService notificationService;
	@Autowired private IdmNotificationRecipientService notificationRecipientService;
	@Autowired private IdmNotificationTemplateService notificationTemplateService;
	@Autowired private IdmNotificationConfigurationService notificationConfigurationService;
	@Autowired private IdmNotificationAttachmentService notificationAttachmentService;
	//
	private DefaultReportManager manager;
	
	@Before
	public void init() {
		manager = context.getAutowireCapableBeanFactory().createBean(DefaultReportManager.class);
	}
	
	@Test
	public void testSupportedReports() {
		List<RptReportExecutorDto> reports = manager.getExecutors();
		//
		Assert.assertTrue(reports.size() > 0);
		Assert.assertTrue(reports.stream().anyMatch(e -> e.getName().equals(TestReportExecutor.REPORT_NAME)));
	}
	
	@Test
	public void testSupportedRenderers() {
		List<RptReportRendererDto> renderers = manager.getRenderers(TestReportExecutor.REPORT_NAME);
		//
		Assert.assertTrue(renderers.size() > 0);
		Assert.assertTrue(renderers.stream().anyMatch(e -> e.getName().equals(TestReportRenderer.RENDERER_NAME)));
	}
	
	@Test
	@Transactional
	public void testGenerateReportByExecutor() throws IOException {
		TestReportExecutor testReportExecutor = context.getAutowireCapableBeanFactory().createBean(TestReportExecutor.class);
		
		RptReportDto report = testReportExecutor.generate(new RptReportDto(UUID.randomUUID()));
		
		Assert.assertNotNull(report.getData());
		InputStream is = attachmentManager.getAttachmentData(report.getData());
		try {
			Assert.assertEquals(mapper.writeValueAsString(TestReportExecutor.identities), IOUtils.toString(is));
		} finally {
			IOUtils.closeQuietly(is);
		}
		//
		attachmentManager.deleteAttachments(report);
	}
	
	@Test
	public void testGenerateReportByManager() throws IOException {
		RptReportDto report = new RptReportDto();
		report.setExecutorName(TestReportExecutor.REPORT_NAME);
		report = manager.generate(report);
		final UUID reportId = report.getId();
		Assert.assertNotNull(reportId);
		
		Assert.assertNotNull(report.getData());
		InputStream is = attachmentManager.getAttachmentData(report.getData());
		try {
			Assert.assertEquals(mapper.writeValueAsString(TestReportExecutor.identities), IOUtils.toString(is));
		} finally {
			IOUtils.closeQuietly(is);
		}
		attachmentManager.deleteAttachments(report);
	}
	
	@Test
	public void testGenerateReportWithFilter() throws IOException {
		RptReportDto report = new RptReportDto();
		report.setExecutorName(TestFilterReportExecutor.REPORT_NAME);
		IdmFormDto filter = new IdmFormDto();
		TestFilterReportExecutor testReportExecutor = context.getAutowireCapableBeanFactory().createBean(TestFilterReportExecutor.class);
		IdmFormDefinitionDto definition = testReportExecutor.getFormDefinition();
		IdmFormValueDto username = new IdmFormValueDto(definition.getMappedAttributeByCode(IdmIdentity_.username.getName()));
		username.setValue(TestReportExecutor.identities.get(0).getUsername());
		filter.getValues().add(username);
		filter.setFormDefinition(definition.getId());
		report.setFilter(filter);
		//
		report = manager.generate(report);
		final UUID reportId = report.getId();
		Assert.assertNotNull(reportId);
		
		Assert.assertNotNull(report.getData());
		InputStream is = attachmentManager.getAttachmentData(report.getData());
		try {
			Assert.assertEquals(
					mapper.writeValueAsString(Lists.newArrayList(TestReportExecutor.identities.get(0))), 
					IOUtils.toString(is));
		} finally {
			IOUtils.closeQuietly(is);
		}
		
		attachmentManager.deleteAttachments(report);
	}
	
	@Test
	public void testRenderReportAsJson() throws IOException {
		TestReportExecutor testReportExecutor = context.getAutowireCapableBeanFactory().createBean(TestReportExecutor.class);
		
		RptReportDto report = testReportExecutor.generate(new RptReportDto(UUID.randomUUID()));
		
		RptRenderedReportDto renderedData = manager.render(report, TestReportRenderer.RENDERER_NAME);
		//
		Assert.assertEquals(mapper.writeValueAsString(TestReportExecutor.identities), IOUtils.toString(renderedData.getRenderedReport()));
	}
	
	@Test
	public void testScheduleReport() throws Exception {
		String testPropagateParameterValue = getHelper().createName();
		Task task = new Task();
		task.setInstanceId(configurationService.getInstanceId());
		task.setTaskType(testFilterReportExecutor.getClass());
		task.setDescription("test report");
		task.getParameters().put(IdmIdentity_.username.getName(), testPropagateParameterValue);
		//
		task = schedulerManager.createTask(task);
		//
		ObserveLongRunningTaskEndProcessor.listenTask(task.getId());
		//
		SimpleTaskTrigger trigger = new SimpleTaskTrigger();
		trigger.setTaskId(task.getId());
		trigger.setFireTime(ZonedDateTime.now());
		schedulerManager.createTrigger(task.getId(), trigger);
		//
		ObserveLongRunningTaskEndProcessor.waitForEnd(task.getId());
		//
		assertEquals(OperationState.EXECUTED, ObserveLongRunningTaskEndProcessor.getResult(task.getId()).getState());
		//
		// find report by LRT task
		RptReportFilter reportFilter = new RptReportFilter();
		IdmLongRunningTaskDto lrt = ObserveLongRunningTaskEndProcessor.getLongRunningTask(task.getId());
		Assert.assertEquals(testPropagateParameterValue, lrt.getTaskProperties().get(IdmIdentity_.username.getName()));
		reportFilter.setLongRunningTaskId(ObserveLongRunningTaskEndProcessor.getLongRunningTask(task.getId()).getId());
		//
		getHelper().waitForResult(res -> {
			return reportService.count(reportFilter) == 0;
		});
		//
		List<RptReportDto> reports = reportService.find(reportFilter, null).getContent();
		Assert.assertEquals(1, reports.size());
		IdmFormInstanceDto formInstance = new IdmFormInstanceDto(reports.get(0), testFilterReportExecutor.getFormDefinition(), reports.get(0).getFilter());
		Assert.assertEquals(testPropagateParameterValue, formInstance.toSinglePersistentValue(IdmIdentity_.username.getName()));
	}
	
	@Test
	public void testSendDefaultNotificationAfterEnd() throws Exception {
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		try {
			getHelper().login(identity); // report is sent to logged identity by default
			//
			RptReportDto report = new RptReportDto();
			report.setExecutorName(TestReportExecutor.REPORT_NAME);
			report = manager.generate(report);
			UUID reportId = report.getId();
			Assert.assertNotNull(reportId);
			Assert.assertNotNull(report.getData());
			//
			try (InputStream is = attachmentManager.getAttachmentData(report.getData())) {
				Assert.assertEquals(mapper.writeValueAsString(TestReportExecutor.identities), IOUtils.toString(is));
			}
			//
			// test notification is sent
			IdmNotificationFilter notificationFilter = new IdmNotificationFilter();
			notificationFilter.setTopic(RptModuleDescriptor.TOPIC_REPORT_GENERATE_SUCCESS);
			notificationFilter.setRecipient(identity.getUsername());
			notificationFilter.setNotificationType(IdmNotificationLog.class);
			List<IdmNotificationLogDto> notifications = notificationService.find(notificationFilter, null).getContent();
			Assert.assertEquals(1, notifications.size());
			//
			// test notification attachments
			IdmNotificationAttachmentFilter notificationAttachmentFilter = new IdmNotificationAttachmentFilter();
			notificationAttachmentFilter.setNotification(notifications.get(0).getId());
			List<IdmNotificationAttachmentDto> notificationAttachments = notificationAttachmentService.find(notificationAttachmentFilter, null).getContent();
			Assert.assertEquals(1, notificationAttachments.size());
			Assert.assertNotNull(notificationAttachments.get(0).getAttachment());
			//
			attachmentManager.deleteAttachments(report);
		} finally {
			logout();
		}
	}
	
	@Test
	public void testNotSendDefaultNotificationWithoutLoggedIdentityAfterEnd() throws Exception {
		NotificationConfigurationDto config = createConfig(null, false);
		//
		RptReportDto report = new RptReportDto();
		report.setExecutorName(TestReportExecutor.REPORT_NAME);
		IdmFormDto filter = new IdmFormDto();
		TestReportExecutor testReportExecutor = context.getAutowireCapableBeanFactory().createBean(TestReportExecutor.class);
		IdmFormDefinitionDto definition = testReportExecutor.getFormDefinition();
		IdmFormValueDto topic = new IdmFormValueDto(definition.getMappedAttributeByCode(AbstractReportExecutor.PROPERTY_TOPIC_REPORT_GENERATE_SUCCESS));
		topic.setValue(config.getTopic());
		filter.getValues().add(topic);
		filter.setFormDefinition(definition.getId());
		report.setFilter(filter);
		report = manager.generate(report);
		UUID reportId = report.getId();
		Assert.assertNotNull(reportId);
		Assert.assertNotNull(report.getData());
		//
		try (InputStream is = attachmentManager.getAttachmentData(report.getData())) {
			Assert.assertEquals(mapper.writeValueAsString(TestReportExecutor.identities), IOUtils.toString(is));
		}
		attachmentManager.deleteAttachments(report);
		//
		// test notification is sent
		IdmNotificationFilter notificationFilter = new IdmNotificationFilter();
		notificationFilter.setTopic(config.getTopic());
		notificationFilter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationService.find(notificationFilter, null).getContent();
		Assert.assertEquals(1, notifications.size());
		Assert.assertEquals(NotificationState.NOT, notifications.get(0).getState());
	}
	
	@Test
	public void testSendConfiguredNotificationAfterEnd() throws Exception {
		IdmIdentityDto identity = getHelper().createIdentity();
		NotificationConfigurationDto config = createConfig(null, false);
		//
		try {
			getHelper().login(identity); // report is sent to logged identity by default
			//
			RptReportDto report = new RptReportDto();
			report.setExecutorName(TestReportExecutor.REPORT_NAME);
			IdmFormDto filter = new IdmFormDto();
			TestReportExecutor testReportExecutor = context.getAutowireCapableBeanFactory().createBean(TestReportExecutor.class);
			IdmFormDefinitionDto definition = testReportExecutor.getFormDefinition();
			IdmFormValueDto topic = new IdmFormValueDto(definition.getMappedAttributeByCode(AbstractReportExecutor.PROPERTY_TOPIC_REPORT_GENERATE_SUCCESS));
			topic.setValue(config.getTopic());
			filter.getValues().add(topic);
			filter.setFormDefinition(definition.getId());
			report.setFilter(filter);
			report = manager.generate(report);
			UUID reportId = report.getId();
			Assert.assertNotNull(reportId);
			Assert.assertNotNull(report.getData());
			//
			try (InputStream is = attachmentManager.getAttachmentData(report.getData())) {
				Assert.assertEquals(mapper.writeValueAsString(TestReportExecutor.identities), IOUtils.toString(is));
			}
			attachmentManager.deleteAttachments(report);
			//
			// test notification is sent
			IdmNotificationFilter notificationFilter = new IdmNotificationFilter();
			notificationFilter.setTopic(config.getTopic());
			notificationFilter.setRecipient(identity.getUsername());
			notificationFilter.setNotificationType(IdmNotificationLog.class);
			List<IdmNotificationLogDto> notifications = notificationService.find(notificationFilter, null).getContent();
			Assert.assertEquals(1, notifications.size());
		} finally {
			logout();
		}
	}
	
	@Test
	public void testNotSendNotConfiguredNotificationAfterEnd() throws Exception {
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		try {
			getHelper().login(identity); // report is sent to logged identity by default
			//
			RptReportDto report = new RptReportDto();
			report.setExecutorName(TestReportExecutor.REPORT_NAME);
			IdmFormDto filter = new IdmFormDto();
			TestReportExecutor testReportExecutor = context.getAutowireCapableBeanFactory().createBean(TestReportExecutor.class);
			IdmFormDefinitionDto definition = testReportExecutor.getFormDefinition();
			IdmFormValueDto topic = new IdmFormValueDto(definition.getMappedAttributeByCode(AbstractReportExecutor.PROPERTY_TOPIC_REPORT_GENERATE_SUCCESS));
			filter.getValues().add(topic);
			filter.setFormDefinition(definition.getId());
			report.setFilter(filter);
			report = manager.generate(report);
			UUID reportId = report.getId();
			Assert.assertNotNull(reportId);
			Assert.assertNotNull(report.getData());
			//
			try (InputStream is = attachmentManager.getAttachmentData(report.getData())) {
				Assert.assertEquals(mapper.writeValueAsString(TestReportExecutor.identities), IOUtils.toString(is));
			}
			attachmentManager.deleteAttachments(report);
			//
			// test notification is sent
			IdmNotificationFilter notificationFilter = new IdmNotificationFilter();
			notificationFilter.setRecipient(identity.getUsername());
			notificationFilter.setNotificationType(IdmNotificationLog.class);
			List<IdmNotificationLogDto> notifications = notificationService.find(notificationFilter, null).getContent();
			Assert.assertTrue(notifications.isEmpty());
		} finally {
			logout();
		}
	}
	
	@Test
	public void testSendConfiguredAdditionalNotificationAfterEnd() throws Exception {
		IdmIdentityDto identity = getHelper().createIdentity();
		String recipient = getHelper().createName() + "@test-bcvsolutions.eu";
		NotificationConfigurationDto config = createConfig(recipient, false);
		//
		try {
			getHelper().login(identity); // report is sent to logged identity by default
			//
			RptReportDto report = new RptReportDto();
			report.setExecutorName(TestReportExecutor.REPORT_NAME);
			IdmFormDto filter = new IdmFormDto();
			TestReportExecutor testReportExecutor = context.getAutowireCapableBeanFactory().createBean(TestReportExecutor.class);
			IdmFormDefinitionDto definition = testReportExecutor.getFormDefinition();
			IdmFormValueDto topic = new IdmFormValueDto(definition.getMappedAttributeByCode(AbstractReportExecutor.PROPERTY_TOPIC_REPORT_GENERATE_SUCCESS));
			topic.setValue(config.getTopic());
			filter.getValues().add(topic);
			filter.setFormDefinition(definition.getId());
			report.setFilter(filter);
			report = manager.generate(report);
			UUID reportId = report.getId();
			Assert.assertNotNull(reportId);
			Assert.assertNotNull(report.getData());
			//
			try (InputStream is = attachmentManager.getAttachmentData(report.getData())) {
				Assert.assertEquals(mapper.writeValueAsString(TestReportExecutor.identities), IOUtils.toString(is));
			}
			attachmentManager.deleteAttachments(report);
			//
			// test notification is sent
			IdmNotificationFilter notificationFilter = new IdmNotificationFilter();
			notificationFilter.setTopic(config.getTopic());
			notificationFilter.setRecipient(identity.getUsername());
			notificationFilter.setNotificationType(IdmNotificationLog.class);
			List<IdmNotificationLogDto> notifications = notificationService.find(notificationFilter, null).getContent();
			Assert.assertEquals(1, notifications.size());
			//
			IdmNotificationRecipientFilter recipientFilter = new IdmNotificationRecipientFilter();
			recipientFilter.setRealRecipient(recipient);
			List<IdmNotificationRecipientDto> recipients = notificationRecipientService.find(recipientFilter, null).getContent();
			Assert.assertFalse(recipients.isEmpty());
			Assert.assertEquals(config.getTopic(), notificationService.get(recipients.get(0).getNotification()).getTopic());
		} finally {
			logout();
		}
	}
	
	@Test
	public void testSendConfiguredRedirectNotificationAfterEnd() throws Exception {
		IdmIdentityDto identity = getHelper().createIdentity();
		String recipient = getHelper().createName() + "@test-bcvsolutions.eu";
		NotificationConfigurationDto config = createConfig(recipient, true);
		//
		try {
			getHelper().login(identity); // report is sent to logged identity by default
			//
			RptReportDto report = new RptReportDto();
			report.setExecutorName(TestReportExecutor.REPORT_NAME);
			IdmFormDto filter = new IdmFormDto();
			TestReportExecutor testReportExecutor = context.getAutowireCapableBeanFactory().createBean(TestReportExecutor.class);
			IdmFormDefinitionDto definition = testReportExecutor.getFormDefinition();
			IdmFormValueDto topic = new IdmFormValueDto(definition.getMappedAttributeByCode(AbstractReportExecutor.PROPERTY_TOPIC_REPORT_GENERATE_SUCCESS));
			topic.setValue(config.getTopic());
			filter.getValues().add(topic);
			filter.setFormDefinition(definition.getId());
			report.setFilter(filter);
			report = manager.generate(report);
			UUID reportId = report.getId();
			Assert.assertNotNull(reportId);
			Assert.assertNotNull(report.getData());
			//
			try (InputStream is = attachmentManager.getAttachmentData(report.getData())) {
				Assert.assertEquals(mapper.writeValueAsString(TestReportExecutor.identities), IOUtils.toString(is));
			}
			attachmentManager.deleteAttachments(report);
			//
			// test notification is sent
			IdmNotificationFilter notificationFilter = new IdmNotificationFilter();
			notificationFilter.setTopic(config.getTopic());
			notificationFilter.setRecipient(identity.getUsername());
			notificationFilter.setNotificationType(IdmNotificationLog.class);
			List<IdmNotificationLogDto> notifications = notificationService.find(notificationFilter, null).getContent();
			Assert.assertTrue(notifications.isEmpty());
			//
			IdmNotificationRecipientFilter recipientFilter = new IdmNotificationRecipientFilter();
			recipientFilter.setRealRecipient(recipient);
			List<IdmNotificationRecipientDto> recipients = notificationRecipientService.find(recipientFilter, null).getContent();
			Assert.assertFalse(recipients.isEmpty());
			Assert.assertEquals(config.getTopic(), notificationService.get(recipients.get(0).getNotification()).getTopic());
		} finally {
			logout();
		}
	}
	
	private IdmNotificationTemplateDto createTestTemplate() {
		// create templates
		IdmNotificationTemplateDto template = new IdmNotificationTemplateDto();
		template.setName(getHelper().createName());
		template.setBodyHtml(getHelper().createName());
		template.setBodyText(template.getBodyHtml());
		template.setCode(template.getName());
		template.setSubject(getHelper().createName());
		return notificationTemplateService.save(template);
	}

	/**
	 * TODO: move to helper?
	 *
	 * @return
	 */
	private NotificationConfigurationDto createConfig(String recipient, boolean redirect) {
		NotificationConfigurationDto config = new NotificationConfigurationDto();
		config.setTopic(getHelper().createName());
		config.setNotificationType(IdmEmailLog.NOTIFICATION_TYPE);
		config.setTemplate(createTestTemplate().getId());
		config.setRecipients(recipient);
		config.setRedirect(redirect);
		//
		return notificationConfigurationService.save(config);
	}
}
