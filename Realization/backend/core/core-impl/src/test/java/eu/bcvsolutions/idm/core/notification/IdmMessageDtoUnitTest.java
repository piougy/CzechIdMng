package eu.bcvsolutions.idm.core.notification;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Construct notification message test.
 * - test builder 
 * 
 * @author Radek Tomiška
 *
 */
public class IdmMessageDtoUnitTest extends AbstractUnitTest {
	
	private static final String PARAMETER_SUBJECT = "subject";
	private static final String PARAMETER_TEXT = "text";
	private static final String PARAMETER_HTML = "<div>html</div>";
	
	@Test
	public void testDefaultLevel() {
		Assert.assertEquals(IdmMessageDto.DEFAULT_LEVEL, new IdmMessageDto().getLevel());
	}
	
	@Test
	public void testSimpleSubjectAndMessage() {
		IdmMessageDto message = new IdmMessageDto.Builder().setSubject(PARAMETER_SUBJECT).setMessage(PARAMETER_TEXT).build();
		//
		Assert.assertEquals(PARAMETER_SUBJECT, message.getSubject());
		Assert.assertEquals(PARAMETER_TEXT, message.getTextMessage());
		Assert.assertEquals(PARAMETER_TEXT, message.getHtmlMessage());
	}
	
	@Test
	public void testTextAndHtmlMessage() {
		IdmMessageDto message = new IdmMessageDto
				.Builder()
				.setSubject(PARAMETER_SUBJECT)
				.setMessage(PARAMETER_TEXT)
				.setHtmlMessage(PARAMETER_HTML)
				.build();
		//
		Assert.assertEquals(PARAMETER_SUBJECT, message.getSubject());
		Assert.assertEquals(PARAMETER_TEXT, message.getTextMessage());
		Assert.assertEquals(PARAMETER_HTML, message.getHtmlMessage());
	}

	@Test
	public void testTemplate() {
		IdmNotificationTemplateDto template = new IdmNotificationTemplateDto();
		template.setSubject(PARAMETER_SUBJECT);
		template.setBodyText(PARAMETER_TEXT);
		IdmMessageDto message = new IdmMessageDto
				.Builder()
				.setTemplate(template)
				.build();
		//
		Assert.assertEquals(PARAMETER_SUBJECT, message.getSubject());
		Assert.assertEquals(PARAMETER_TEXT, message.getTextMessage());
		Assert.assertEquals(PARAMETER_TEXT, message.getHtmlMessage());
	}
	
	@Test
	public void testTemplateOveloadMessageAndSubject() {
		IdmNotificationTemplateDto template = new IdmNotificationTemplateDto();
		template.setSubject("template");
		template.setBodyText("template");
		IdmMessageDto message = new IdmMessageDto
				.Builder()
				.setTemplate(template)
				.setMessage(PARAMETER_TEXT)
				.setSubject(PARAMETER_SUBJECT)
				.build();
		//
		Assert.assertEquals(PARAMETER_SUBJECT, message.getSubject());
		Assert.assertEquals(PARAMETER_TEXT, message.getTextMessage());
		Assert.assertEquals(PARAMETER_TEXT, message.getHtmlMessage());
	}
	
	@Test
	public void testModelSuccess() {
		ResultModel model = new DefaultResultModel(CoreResultCode.ACCEPTED);
		IdmMessageDto message = new IdmMessageDto
				.Builder()
				.setModel(model)
				.build();
		Assert.assertEquals(model.getStatusEnum(), message.getSubject());
		Assert.assertEquals(model.getMessage(), message.getTextMessage());
		Assert.assertEquals(model.getMessage(), message.getHtmlMessage());
		Assert.assertEquals(NotificationLevel.SUCCESS, message.getLevel());
	}
	
	@Test
	public void testModelWarning() {
		ResultModel model = new DefaultResultModel(CoreResultCode.BAD_VALUE);
		IdmMessageDto message = new IdmMessageDto
				.Builder()
				.setModel(model)
				.build();
		Assert.assertEquals(model.getStatusEnum(), message.getSubject());
		Assert.assertEquals(model.getMessage(), message.getTextMessage());
		Assert.assertEquals(model.getMessage(), message.getHtmlMessage());
		Assert.assertEquals(NotificationLevel.WARNING, message.getLevel());
	}
	
	@Test
	public void testModelError() {
		ResultModel model = new DefaultResultModel(CoreResultCode.INTERNAL_SERVER_ERROR);
		IdmMessageDto message = new IdmMessageDto
				.Builder()
				.setModel(model)
				.build();
		Assert.assertEquals(model.getStatusEnum(), message.getSubject());
		Assert.assertEquals(model.getMessage(), message.getTextMessage());
		Assert.assertEquals(model.getMessage(), message.getHtmlMessage());
		Assert.assertEquals(NotificationLevel.ERROR, message.getLevel());
	}
	
	@Test
	public void testModelOveloadMessageAndSubject() {
		ResultModel model = new DefaultResultModel(CoreResultCode.INTERNAL_SERVER_ERROR);
		IdmMessageDto message = new IdmMessageDto
				.Builder()
				.setModel(model)
				.setMessage(PARAMETER_TEXT)
				.setSubject(PARAMETER_SUBJECT)
				.build();
		Assert.assertEquals(PARAMETER_SUBJECT, message.getSubject());
		Assert.assertEquals(PARAMETER_TEXT, message.getTextMessage());
		Assert.assertEquals(PARAMETER_TEXT, message.getHtmlMessage());
		Assert.assertEquals(NotificationLevel.ERROR, message.getLevel());
	}
	
	@Test
	public void testModelOveloadLevel() {
		ResultModel model = new DefaultResultModel(CoreResultCode.INTERNAL_SERVER_ERROR);
		IdmMessageDto message = new IdmMessageDto
				.Builder()
				.setModel(model)
				.setLevel(NotificationLevel.SUCCESS)
				.build();
		Assert.assertEquals(NotificationLevel.SUCCESS, message.getLevel());
	}
	
	@Test
	public void testMergeParameters() {
		ResultModel model = new DefaultResultModel(CoreResultCode.INTERNAL_SERVER_ERROR, ImmutableMap.of("one", "one", "two", "two"));
		IdmMessageDto message = new IdmMessageDto
				.Builder()
				.setModel(model)
				.addParameter("one", "OneUpdated")
				.addParameter("three", "three")
				.build();
		Assert.assertEquals("OneUpdated", message.getParameters().get("one"));
		Assert.assertEquals("two", message.getParameters().get("two"));
		Assert.assertEquals("three", message.getParameters().get("three"));
	}
}
