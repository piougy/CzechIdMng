package eu.bcvsolutions.idm.core.model.event.processor.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ConfigurationProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.model.event.ConfigurationEvent.ConfigurationEventType;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;

/**
 * When default role is changed, then authorization policy caches have to be evicted.
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(ConfigurationChangeDefaultRoleProcessor.PROCESSOR_NAME)
@Description("When default role is changed, then authorization policy caches have to be evicted.")
public class ConfigurationChangeDefaultRoleProcessor
		extends CoreEventProcessor<IdmConfigurationDto> 
		implements ConfigurationProcessor {
	
	public static final String PROCESSOR_NAME = "core-configuration-change-default-role-processor";
	//
	@Autowired private IdmCacheManager cacheManager;
	
	public ConfigurationChangeDefaultRoleProcessor() {
		super(ConfigurationEventType.UPDATE, ConfigurationEventType.CREATE, ConfigurationEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmConfigurationDto> event) {
		return super.conditional(event) && event.getContent().getName().equals(RoleConfiguration.PROPERTY_DEFAULT_ROLE);
	}

	@Override
	public EventResult<IdmConfigurationDto> process(EntityEvent<IdmConfigurationDto> event) {
		// cached permissions
		cacheManager.evictCache(AuthorizationManager.PERMISSION_CACHE_NAME);
		// cached configured authorization policies
		cacheManager.evictCache(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 10;
	}
}
