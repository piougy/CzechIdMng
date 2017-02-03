package eu.bcvsolutions.idm.core.notification.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.notification.entity.IdmNotification;
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
	public List<String> routes(IdmNotification notification) {
		List<String> routes = new ArrayList<>();
		notificationConfigurationService.getSenders(notification).forEach(sender -> {
			// we need to know spring bean id from instance
			for(Entry<String, NotificationSender> entry : context.getBeansOfType(NotificationSender.class).entrySet()) {
				if (entry.getValue().equals(sender)) {
					routes.add(String.format("bean:%s?method=send", entry.getKey()));
				}
			}
		});
		if (routes.isEmpty()) {
			return null; // no sender
		}
	    return routes;
	}
}
