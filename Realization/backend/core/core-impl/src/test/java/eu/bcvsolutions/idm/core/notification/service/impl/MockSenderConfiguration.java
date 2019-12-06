package eu.bcvsolutions.idm.core.notification.service.impl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.bcvsolutions.idm.core.notification.api.service.NotificationSender;

/**
 * Test moc sender init.
 * 
 * @author Radek Tomiška
 *
 */
@Configuration
public class MockSenderConfiguration {

	static final String DEFAULT_SENDER = "mockDefaultSender";
	static final String CONFIGURED_SENDER = "mockConfiguredSender";

	// this bean will be injected into the OrderServiceTest class
	@Bean(name = DEFAULT_SENDER)
	public NotificationSender<?> mockDefaultSender() {
		return new MockSender(DEFAULT_SENDER, -100);
	}

	@Bean
	public NotificationSender<?> mockUnavailableSender() {
		// this sender will be overriden by default and by configured sender
		return new MockSender("mockUnavailableSender", 0);
	}

	@Bean(name = CONFIGURED_SENDER)
	public NotificationSender<?> mockConfiguredSender() {
		return new MockSender(CONFIGURED_SENDER, 0);
	}
}