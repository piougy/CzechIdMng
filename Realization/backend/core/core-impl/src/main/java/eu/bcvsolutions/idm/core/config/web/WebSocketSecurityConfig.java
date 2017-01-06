package eu.bcvsolutions.idm.core.config.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

import eu.bcvsolutions.idm.security.domain.CustomMessageSecurityExpressionHandler;

/**
 * Websocket security configuration
 * 
 * @author Radek Tomiška
 *
 */
@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
	
	/**
	 * Messages for specific user
	 */
	protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
		messages.expressionHandler(new CustomMessageSecurityExpressionHandler<>());
		messages.simpDestMatchers("/user/**").fullyAuthenticated();
	}
	
	/**
	 * Disable csrf protection
	 * TODO: implement csrf protection
	 */
	@Override
	protected boolean sameOriginDisabled() {
		return true;
	}
}