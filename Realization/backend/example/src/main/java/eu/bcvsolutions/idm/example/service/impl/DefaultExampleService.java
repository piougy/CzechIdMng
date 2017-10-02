package eu.bcvsolutions.idm.example.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.example.ExampleModuleDescriptor;
import eu.bcvsolutions.idm.example.config.domain.ExampleConfiguration;
import eu.bcvsolutions.idm.example.dto.Pong;
import eu.bcvsolutions.idm.example.service.api.ExampleService;

/**
 * Example business logic - implementation
 * 
 * @author Radek Tomiška
 *
 */
@Service("exampleService")
public class DefaultExampleService implements ExampleService {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultExampleService.class);
	private final ExampleConfiguration exampleConfiguration;
	private final NotificationManager notificationManager;
	
	@Autowired
	public DefaultExampleService(
			ExampleConfiguration exampleConfiguration,
			NotificationManager notificationManager) {
		Assert.notNull(exampleConfiguration, "Configuration is required!");
		Assert.notNull(notificationManager, "Notification manager is required!");
		//
		this.exampleConfiguration = exampleConfiguration;
		this.notificationManager = notificationManager;
	}

	@Override
	public Pong ping(String message) {
		LOG.info("Ping [{}]", message);
		//
		return new Pong(message);
	}
	
	@Override
	public String getPrivateValue() {
		return exampleConfiguration.getPrivateValue();
	}

	@Override
	public List<IdmNotificationLogDto> sendNotification(String message) {
		return notificationManager.send(
				ExampleModuleDescriptor.TOPIC_EXAMPLE,
				new IdmMessageDto.Builder()
					.setLevel(NotificationLevel.SUCCESS)
					.setMessage(message)
					.build());		
	}
}
