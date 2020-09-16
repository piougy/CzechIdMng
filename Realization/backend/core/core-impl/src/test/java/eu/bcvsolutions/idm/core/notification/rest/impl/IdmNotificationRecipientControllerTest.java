package eu.bcvsolutions.idm.core.notification.rest.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationRecipientFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Controller tests.
 * 
 * @author Radek Tomiška
 *
 */
public class IdmNotificationRecipientControllerTest extends AbstractReadWriteDtoControllerRestTest<IdmNotificationRecipientDto> {

	@Autowired private IdmNotificationRecipientController controller;
	@Autowired private IdmNotificationLogService notificationLogService;
	
	@Override
	protected AbstractReadWriteDtoController<IdmNotificationRecipientDto, ?> getController() {
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
		IdmNotificationRecipientDto recipentOne = createDto();
		createDto(); // other
		//
		IdmNotificationRecipientFilter filter = new IdmNotificationRecipientFilter();
		filter.setText(recipentOne.getRealRecipient());
		List<IdmNotificationRecipientDto> attachments = find(filter);
		Assert.assertEquals(1, attachments.size());
		Assert.assertTrue(attachments.stream().anyMatch(r -> r.getId().equals(recipentOne.getId())));
	}
	
	@Test
	public void testFindByRealRecipient() {
		IdmNotificationRecipientDto recipentOne = createDto();
		createDto(); // other
		//
		IdmNotificationRecipientFilter filter = new IdmNotificationRecipientFilter();
		filter.setRealRecipient(recipentOne.getRealRecipient());
		List<IdmNotificationRecipientDto> attachments = find(filter);
		Assert.assertEquals(1, attachments.size());
		Assert.assertTrue(attachments.stream().anyMatch(r -> r.getId().equals(recipentOne.getId())));
	}
	
	@Test
	public void testFindByIdentityRecipient() {
		IdmNotificationRecipientDto recipentOne = createDto();
		createDto(); // other
		//
		IdmNotificationRecipientFilter filter = new IdmNotificationRecipientFilter();
		filter.setIdentityRecipient(recipentOne.getIdentityRecipient());
		List<IdmNotificationRecipientDto> attachments = find(filter);
		Assert.assertEquals(1, attachments.size());
		Assert.assertTrue(attachments.stream().anyMatch(r -> r.getId().equals(recipentOne.getId())));
	}
	
	@Test
	public void testFindByNotification() {
		IdmNotificationRecipientDto recipentOne = createDto();
		createDto(); // other
		//
		IdmNotificationRecipientFilter filter = new IdmNotificationRecipientFilter();
		filter.setNotification(recipentOne.getNotification());
		List<IdmNotificationRecipientDto> attachments = find(filter);
		Assert.assertEquals(1, attachments.size());
		Assert.assertTrue(attachments.stream().anyMatch(r -> r.getId().equals(recipentOne.getId())));
	}

	@Override
	protected IdmNotificationRecipientDto prepareDto() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmNotificationLogDto notification = new IdmNotificationLogDto();
		notification.setMessage(new IdmMessageDto.Builder(NotificationLevel.SUCCESS).setMessage(getHelper().createName()).build());
		// related notification
		notification = notificationLogService.save(notification);
		//
		IdmNotificationRecipientDto dto = new IdmNotificationRecipientDto();
		dto.setIdentityRecipient(identity.getId());
		dto.setRealRecipient(getHelper().createName());
		dto.setNotification(notification.getId());
		//
		return dto;
	}
}
