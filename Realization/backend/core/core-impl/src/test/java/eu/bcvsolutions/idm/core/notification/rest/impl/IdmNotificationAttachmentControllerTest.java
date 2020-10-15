package eu.bcvsolutions.idm.core.notification.rest.impl;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.ecm.service.impl.DefaultAttachmentManagerIntegrationTest;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationAttachmentDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationAttachmentFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationAttachmentService;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationConfigurationService;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Controller tests.
 * 
 * @author Radek Tomiška
 *
 */
public class IdmNotificationAttachmentControllerTest extends AbstractReadWriteDtoControllerRestTest<IdmNotificationAttachmentDto> {

	@Autowired private IdmNotificationAttachmentController controller;
	@Autowired private IdmNotificationLogService notificationLogService;
	@Autowired private AttachmentManager attachmentManager;
	@Autowired private IdmNotificationAttachmentService notificationAttachmentService;
	@Autowired private NotificationManager notificationManager;
	@Autowired private IdmNotificationTemplateService notificationTemplateService;
	@Autowired private IdmNotificationConfigurationService notificationConfigurationService;
	
	@Override
	protected AbstractReadWriteDtoController<IdmNotificationAttachmentDto, ?> getController() {
		return controller;
	}
	
	@Override
	protected boolean supportsAutocomplete() {
		return false;
	}
	
	@Override
	protected boolean isReadOnly() {
		return true;
	}
	
	@Test
	public void testFindByText() {
		IdmNotificationAttachmentDto attachmentOne = createDto();
		createDto(); // other
		//
		IdmNotificationAttachmentFilter filter = new IdmNotificationAttachmentFilter();
		filter.setText(attachmentOne.getName());
		List<IdmNotificationAttachmentDto> attachments = find(filter);
		Assert.assertEquals(1, attachments.size());
		Assert.assertTrue(attachments.stream().anyMatch(r -> r.getId().equals(attachmentOne.getId())));
	}
	
	@Test
	public void testFindByNotification() {
		IdmNotificationAttachmentDto attachmentOne = createDto();
		createDto(); // other
		//
		IdmNotificationAttachmentFilter filter = new IdmNotificationAttachmentFilter();
		filter.setNotification(attachmentOne.getNotification());
		List<IdmNotificationAttachmentDto> attachments = find(filter);
		Assert.assertEquals(1, attachments.size());
		Assert.assertTrue(attachments.stream().anyMatch(r -> r.getId().equals(attachmentOne.getId())));
	}
	
	@Test
	public void testDownload() throws Exception {
		NotificationConfigurationDto config = createConfig();
		IdmNotificationTemplateDto template = createTestTemplate();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		IdmAttachmentDto attachmentOne = DefaultAttachmentManagerIntegrationTest.prepareDtoWithoutContent();
		attachmentOne.setInputData(IOUtils.toInputStream(getHelper().createName())); // prepared attachment
		IdmAttachmentDto attachment = DefaultAttachmentManagerIntegrationTest.prepareDtoWithoutContent();
		String content = getHelper().createName();
		attachment.setInputData(IOUtils.toInputStream(content));	
		IdmAttachmentDto attachmentTwo = attachmentManager.saveAttachment(identity, attachment);
		List<IdmAttachmentDto> attachments = Lists.newArrayList(attachmentOne, attachmentTwo);
		//
		List<IdmNotificationLogDto> notifications = notificationManager.send(
				config.getTopic(), 
				new IdmMessageDto.Builder().setTemplate(template).build(), 
				null,
				Lists.newArrayList(identity),
				attachments);
		
		Assert.assertEquals(1, notifications.size());
		Assert.assertTrue(notifications.stream().anyMatch(n -> n.getType().equals(IdmEmailLog.NOTIFICATION_TYPE)));
		//
		IdmNotificationLogDto notification = notifications.get(0);
		//
		IdmNotificationAttachmentFilter notificationAttachmentFilter = new IdmNotificationAttachmentFilter();
		notificationAttachmentFilter.setNotification(notification.getId());
		List<IdmNotificationAttachmentDto> notificationAttachments = notificationAttachmentService.find(notificationAttachmentFilter, null).getContent();
		Assert.assertEquals(2, notificationAttachments.size());
		Assert.assertTrue(notificationAttachments.stream().allMatch(na -> na.getAttachment() != null));
		Assert.assertTrue(notificationAttachments.stream().anyMatch(na -> na.getAttachment().equals(attachmentTwo.getId())));
		IdmNotificationAttachmentDto notificationAttachment = notificationAttachments
				.stream()
				.filter(na -> na.getAttachment().equals(attachmentTwo.getId()))
				.findFirst()
				.get();
		//
		// download attachment
		String response = getMockMvc().perform(MockMvcRequestBuilders.get(getDetailUrl(notificationAttachment.getId()) + "/download")
        		.with(authentication(getAdminAuthentication())))
				.andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
		Assert.assertEquals(content, response);
		//
		// 404 - notification attachment not found
		getMockMvc().perform(MockMvcRequestBuilders.get(getDetailUrl(UUID.randomUUID()) + "/download")
        		.with(authentication(getAdminAuthentication())))
				.andExpect(status().isNotFound());
		//
		// 404 - delete attachment (e.g. simulate ecm store purge)
		attachmentManager.delete(attachmentTwo);
		getMockMvc().perform(MockMvcRequestBuilders.get(getDetailUrl(notificationAttachment.getId()) + "/download")
        		.with(authentication(getAdminAuthentication())))
				.andExpect(status().isNotFound());
	}

	@Override
	protected IdmNotificationAttachmentDto prepareDto() {
		IdmNotificationLogDto notification = new IdmNotificationLogDto();
		notification.setMessage(new IdmMessageDto.Builder(NotificationLevel.SUCCESS).setMessage(getHelper().createName()).build());
		// related notification
		notification = notificationLogService.save(notification);
		//
		IdmAttachmentDto attachment = attachmentManager.save(DefaultAttachmentManagerIntegrationTest.prepareDto());
		//
		IdmNotificationAttachmentDto dto = new IdmNotificationAttachmentDto();
		dto.setAttachment(attachment.getId());
		dto.setName(attachment.getName());
		dto.setNotification(notification.getId());
		//
		return dto;
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
	private NotificationConfigurationDto createConfig() {
		NotificationConfigurationDto config = new NotificationConfigurationDto();
		config.setTopic(getHelper().createName());
		config.setNotificationType(IdmEmailLog.NOTIFICATION_TYPE);
		//
		return  notificationConfigurationService.save(config);
	}
}
