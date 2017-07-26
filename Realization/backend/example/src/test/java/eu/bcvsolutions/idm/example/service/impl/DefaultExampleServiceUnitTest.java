package eu.bcvsolutions.idm.example.service.impl;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import eu.bcvsolutions.idm.core.notification.service.api.NotificationManager;
import eu.bcvsolutions.idm.example.config.domain.ExampleConfiguration;
import eu.bcvsolutions.idm.example.dto.Pong;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Example service - unit tests
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultExampleServiceUnitTest extends AbstractUnitTest {
	
	@Mock 
	private ExampleConfiguration exampleConfiguration;
	@Mock 
	private NotificationManager notificationManager;
	@InjectMocks 
	private DefaultExampleService service;
	
	@Test
	public void testPingWithMessage() {
		String message = "test";
		Pong pong = service.ping(message);
		Assert.assertNotNull(pong);
		Assert.assertEquals(message, pong.getMessage());
	}
}
