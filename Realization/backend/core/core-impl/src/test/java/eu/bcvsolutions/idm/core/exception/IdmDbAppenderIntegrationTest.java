package eu.bcvsolutions.idm.core.exception;

import org.junit.Assert;
import org.junit.Test;

import ch.qos.logback.classic.spi.LoggingEvent;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for implementation of log DBAppender. Only purpose is fix bugs in DBAppender (max message length, forbidden characters in Postgresql).
 *
 * @author Vít Švanda
 * @since 11.0.0
 */
public class IdmDbAppenderIntegrationTest extends AbstractIntegrationTest {

	@Test
	public void testLogTooLongMessage() {
		IdmDbAppender appender = new IdmDbAppender();
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
		IdmDbAppender appender = new IdmDbAppender();
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
