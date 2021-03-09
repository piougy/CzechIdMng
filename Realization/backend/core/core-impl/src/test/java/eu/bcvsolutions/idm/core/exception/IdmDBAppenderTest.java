package eu.bcvsolutions.idm.core.exception;

import ch.qos.logback.classic.spi.LoggingEvent;
import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmGenerateValueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.ValueGeneratorDto;
import eu.bcvsolutions.idm.core.api.generator.AbstractGeneratorTest;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.generator.identity.IdentityEmailGenerator;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test for implementation of log DBAppender. Only purpose is fix bugs in DBAppender (max message length, forbidden characters in Postgresql).
 *
 * @author Vít Švanda
 * @since 11.0.0
 */
public class IdmDBAppenderTest extends AbstractIntegrationTest {

	@Test
	public void testLogTooLongMessage() {
		IdmDBAppender appender = new IdmDBAppender();
		appender.stop();
		String veryLongText = "verylongtext!!!!!!!!!!!!!!!verylongtext!!!!!!!!!!!!!!!verylongtext!!!!!!!!!!!!!!!" +
				"verylongtext!!!!!!!!!!!!!!!verylongtext!!!!!!!!!!!!!!!verylongtext!!!!!!!!!!!!!!!verylongtext!!" +
				"!!!!!!!!!!!!!verylongtext!!!!!!!!!!!!!!!";
		Assert.assertTrue(veryLongText.length() > 200);
		
		LoggingEvent loggingEvent = new LoggingEvent();
		loggingEvent.setMessage(veryLongText);
		String formattedMessage = loggingEvent.getFormattedMessage();
		Assert.assertEquals(veryLongText, formattedMessage);

		appender.doAppend(loggingEvent);
		formattedMessage = loggingEvent.getFormattedMessage();
		Assert.assertNotEquals(veryLongText, formattedMessage);
		Assert.assertEquals(199, formattedMessage.length());
		String message = loggingEvent.getMessage();
		Assert.assertNotEquals(veryLongText, message);
		Assert.assertEquals(199, message.length());
	}
	
	@Test
	public void testLogForbiddenChars() {
		IdmDBAppender appender = new IdmDBAppender();
		appender.stop();
		String wrongMessage = "Log message with forbidden (\u0000) characters \\x00!";
		
		LoggingEvent loggingEvent = new LoggingEvent();
		loggingEvent.setMessage(wrongMessage);
		String formattedMessage = loggingEvent.getFormattedMessage();
		Assert.assertEquals(wrongMessage, formattedMessage);

		appender.doAppend(loggingEvent);
		formattedMessage = loggingEvent.getFormattedMessage();
		Assert.assertNotEquals(wrongMessage, formattedMessage);
		Assert.assertFalse(formattedMessage.contains("\u0000"));
		Assert.assertFalse(formattedMessage.contains("\\x00"));
		String message = loggingEvent.getMessage();
		Assert.assertNotEquals(wrongMessage, message);
		Assert.assertFalse(message.contains("\u0000"));
		Assert.assertFalse(message.contains("\\x00"));
	}
}
