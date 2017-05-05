package eu.bcvsolutions.idm.core.notification.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationDto;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationConfigurationService;
import eu.bcvsolutions.idm.core.notification.service.api.NotificationSender;

/**
 * Sending notifications over Camel
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class NotificationRouteBuilder extends RouteBuilder {
	
	@Autowired
	private IdmNotificationConfigurationService notificationConfigurationService;
	@Autowired
	private ApplicationContext context;
	@Autowired
	private IdmConfigurationService configurationService;

	
	@Override
    public void configure() throws Exception {		
		
		from("direct:notifications").multicast()/*.parallelProcessing()*/.recipientList(method(this, "routes")).end();
    	//
    	// register email sender
    	from("direct:emails").to("bean:emailer?method=send");
    }
	
	/**
	 * Configure routes by sent notification
	 * 
	 * @param notification
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List<String> routes(IdmNotificationDto notification) {
		//TODO: where to put the config property?
		//final IdmConfiguration senderToUse = configurationService.get(notification.getSenderConfigPropertyName());

		List<String> routes = notificationConfigurationService.getSenders(notification)
				.stream()
				// We will use only sender that is currently set in configuration
				//.filter(sender -> senderToUse.getValue().equals(sender.getClass().getName()))
				.map(this::getRouteForSender)
				.filter(route -> route != null)
				.collect(Collectors.toList());

	    return routes.isEmpty() ? null : routes;
		}

	/**
	 * Returns route
	 *
	 * @param sender
	 * @return
	 */
	private String getRouteForSender(final NotificationSender<?> sender) {
		return context.getBeansOfType(NotificationSender.class).entrySet().stream()
				.filter(entry -> entry.getValue().equals(sender))
				.map(entry -> String.format("bean:%s?method=send", entry.getKey()))
				.findFirst()
				.get();
	}
}
