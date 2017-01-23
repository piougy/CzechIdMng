package eu.bcvsolutions.idm.core.api.event;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.security.api.service.EnabledEvaluator;

/**
 * Single entity event processor
 * 
 * Types could be {@literal null}, then processor supports all event types
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link AbstractEntity} type
 */
public abstract class AbstractEntityEventProcessor<E extends AbstractEntity> implements EntityEventProcessor<E>, ApplicationListener<AbstractEntityEvent<E>> {

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
	
	@Override
	public String getModule() {
		return this.getClass().getCanonicalName().split("\\.")[3];
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
			return configurationService.getBooleanValue(
					ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX
					+ getModule()
					+ ConfigurationService.PROPERTY_SEPARATOR
					+ getName()
					+ ConfigurationService.PROPERTY_SEPARATOR
					+ ModuleService.PROPERTY_ENABLED, false);
		}
		// enabled by default
		return false;
	}
}
