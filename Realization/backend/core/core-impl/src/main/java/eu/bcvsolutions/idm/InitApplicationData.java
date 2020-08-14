package eu.bcvsolutions.idm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.AppModule;
import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.ModuleDescriptorEvent;
import eu.bcvsolutions.idm.core.api.event.ModuleDescriptorEvent.ModuleDescriptorEventType;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.config.flyway.CoreFlywayConfig;
import eu.bcvsolutions.idm.core.model.event.processor.module.InitAdminIdentityProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.module.InitOrganizationProcessor;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Initialize CzechIdM application (data) - publish INIT event for {@link AppModule}.
 *
 * @author Radek Tomiška
 *
 */
@DependsOn(CoreFlywayConfig.NAME)
@Component(InitApplicationData.NAME)
public class InitApplicationData implements ApplicationListener<ContextRefreshedEvent> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InitApplicationData.class);
	//
	public static final String NAME = "initApplicationData";
	public static final String ADMIN_USERNAME = InitAdminIdentityProcessor.ADMIN_USERNAME;
	public static final String ADMIN_PASSWORD = InitAdminIdentityProcessor.ADMIN_PASSWORD;
	/**
	 * @deprecated @since 10.5.0 - use {@link RoleConfiguration#getAdminRoleCode()}
	 */
	@Deprecated
	public static final String ADMIN_ROLE = RoleConfiguration.DEFAULT_ADMIN_ROLE;
	public static final String DEFAULT_TREE_TYPE = InitOrganizationProcessor.DEFAULT_TREE_TYPE;
	//
	@Autowired private SecurityService securityService;
	@Autowired private EntityEventManager entityEventManager;

	@Override
	@Transactional
	public void onApplicationEvent(ContextRefreshedEvent event) {
		init();
	}

	protected void init() {
		securityService.setSystemAuthentication();
		//
		LOG.info("Init application - start ...");
		try {
			//
			// Cancels all previously ran events (before server is restarted).
			// Init is called before new event is published => defined here (cannot be done in processor).
			entityEventManager.init();
			// start application by event
			ModuleDescriptorEvent event = new ModuleDescriptorEvent(
					ModuleDescriptorEventType.INIT, 
					new ModuleDescriptorDto(AppModule.MODULE_ID)
			);
			// publish event => all registered processors will be notified
			entityEventManager.publishEvent(event);
			//
			LOG.info("Init application - complete.");
		} finally {
			securityService.logout();
		}
	}
}
