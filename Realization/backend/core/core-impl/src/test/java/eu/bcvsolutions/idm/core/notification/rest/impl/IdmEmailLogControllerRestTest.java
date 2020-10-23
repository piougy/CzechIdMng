package eu.bcvsolutions.idm.core.notification.rest.impl;

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationState;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmEmailLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Controller tests
 * - TODO: move all filters here
 * 
 * @author Radek Tomiška
 *
 */
public class IdmEmailLogControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmEmailLogDto> {

	@Autowired private IdmEmailLogController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmEmailLogDto, ?> getController() {
		return controller;
	}
	
	@Override
	protected boolean isReadOnly() {
		return true;
	}
	
	@Override
	protected boolean supportsAutocomplete() {
		return false;
	}
	
	@Test
	public void testFindByState() {
		String message = getHelper().createName();
		IdmEmailLogDto emailLog = prepareDto();
		emailLog.setMessage(new IdmMessageDto.Builder(NotificationLevel.SUCCESS).setMessage(message).build());
		emailLog.setSent(ZonedDateTime.now());
		IdmEmailLogDto emailLogOne = createDto(emailLog);
		emailLog = prepareDto(); // other
		emailLog.setMessage(new IdmMessageDto.Builder(NotificationLevel.SUCCESS).setMessage(message).build());
		emailLog.setSent(null);
		IdmEmailLogDto emailLogTwo = createDto(emailLog);
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.add("text", message);
		parameters.set("state", NotificationState.ALL.name());
		List<IdmEmailLogDto> results = find(parameters);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(emailLogOne.getId())));
		//
		parameters.set("state", NotificationState.NOT.name());
		results = find(parameters);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(emailLogTwo.getId())));
		//
		parameters.set("state", NotificationState.PARTLY.name());
		results = find(parameters);
		Assert.assertTrue(results.isEmpty());
	}

	@Override
	protected IdmEmailLogDto prepareDto() {
		IdmIdentityDto recipient = getHelper().createIdentity((GuardedString) null);
		IdmEmailLogDto dto = new IdmEmailLogDto();
		dto.setMessage(new IdmMessageDto.Builder(NotificationLevel.SUCCESS).setMessage(getHelper().createName()).build());
		dto.getRecipients().add(new IdmNotificationRecipientDto(recipient.getId()));
		//
		return dto;
	}
}
