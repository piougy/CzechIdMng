package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;

/**
 * Single entity event processor
 * 
 * Types could be {@literal null}, then processor supports all event types
 * 
 * TODO: move @Autowire to @Configuration bean post processor
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link BaseEntity}, {@link BaseDto} or any other {@link Serializable} content type
 */
public abstract class AbstractEntityEventProcessor<E extends Serializable> implements EntityEventProcessor<E>, ApplicationListener<AbstractEntityEvent<E>> {

	private final Class<E> entityClass;
	private final Set<String> types = new HashSet<>();
	
	@Autowired(required = false)
	private EnabledEvaluator enabledEvaluator; // optional internal dependency - checks for module is enabled
	
	@Autowired(required = false)
	private ConfigurationService configurationService; // optional internal dependency - checks for processor is enabled
	
	@SuppressWarnings({"unchecked"})
	public AbstractEntityEventProcessor(EventType... types) {
		this.entityClass = (Class<E>)GenericTypeResolver.resolveTypeArgument(getClass(), EntityEventProcessor.class);
		if (types != null) {
			for(EventType type : types) {
				this.types.add(type.name());
			}
		}
	}
	
	public AbstractEntityEventProcessor(EnabledEvaluator enabledEvaluator, ConfigurationService configurationService, EventType... types) {
		this(types);
		this.enabledEvaluator = enabledEvaluator;
		this.configurationService = configurationService;
	}
	
	@Override
	public String getModule() {
		return EntityUtils.getModule(this.getClass());
	}
	
	@Override
	public String getName() {
		String name = this.getClass().getCanonicalName();
		if (StringUtils.isEmpty(name)) {
			// TODO: inline classes ...
			return null;
		}
		return name;
	}
	
	@Override
	public Class<E> getEntityClass() {
		return entityClass;
	}
	
	@Override
	public String[] getEventTypes() {
		return types.toArray(new String[types.size()]);
	}
	
	@Override
	public boolean supports(EntityEvent<?> entityEvent) {
		Assert.notNull(entityEvent);
		Assert.notNull(entityEvent.getContent(), "EntityeEvent does not contain content, content is required!");
		
		return entityEvent.getContent().getClass().isAssignableFrom(entityClass)
				&& (types.isEmpty() || types.contains(entityEvent.getType().name()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EventResult<E> process(EntityEvent<E> event, EventContext<E> context) {
		return process(event);
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(java.lang.Object)
	 */
	@Override
	public void onApplicationEvent(AbstractEntityEvent<E> event) {
		// check for module is enabled, if evaluator is given
		if (enabledEvaluator != null && !enabledEvaluator.isEnabled(this.getClass())) {
			return;
		}
		// check for processor is enabled
		if (isDisabled()) {
			return;
		}
		//
		if (!supports(event)) {
			// event is not supported with this processor
			return;
		}
		if (event.isClosed()) {	
			// event is completely processed 
			return;
		}
		if (event.isSuspended()) {	
			// event is suspended
			return;
		}
		Integer processedOrder = event.getProcessedOrder();
		if (processedOrder != null && processedOrder >= this.getOrder()) {	
			// event was processed with this processor
			return;
		}
		EventContext<E> context = event.getContext();
		// process event
		EventResult<E> result = process(event, context);
		// add result to history
		context.addResult(result);
	}
	
	@Override
	public boolean isClosable() {
		return false;
	}
	
	@Override
	public boolean isDisableable() {
		return true;
	}
	
	@Override
	public boolean isDisabled() {
		// check for processor is enabled, if configuration service is given
		if (configurationService != null) {
			return !configurationService.getBooleanValue(
					getConfigurationPrefix()
					+ ModuleService.PROPERTY_ENABLED, true);
		}
		// enabled by default
		return false;
	}
	
	/**
	 * Returns prefix to configuration for this entity event processor. 
	 * Under this prefix could be found all event processor's properties.
	 * 
	 * @return
	 */
	protected String getConfigurationPrefix() {
		return ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX
				+ getModule()
				+ ConfigurationService.PROPERTY_SEPARATOR
				+ "processor"
				+ ConfigurationService.PROPERTY_SEPARATOR
				+ getName()
				+ ConfigurationService.PROPERTY_SEPARATOR;
	}
}
