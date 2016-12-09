package eu.bcvsolutions.idm.core.model.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.event.DefaultEventContext;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.EntityEventProcessorManager;
import eu.bcvsolutions.idm.security.api.service.EnabledEvaluator;

/**
 * Entity processing based on spring plugins
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultEntityEventProcessorManager implements EntityEventProcessorManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultEntityEventProcessorManager.class);
	private final PluginRegistry<EntityEventProcessor<?>, EntityEvent<?>> entityProcessors;
	private final EnabledEvaluator enabledEvaluator;
	
	@Autowired
	public DefaultEntityEventProcessorManager(
			List<? extends EntityEventProcessor<?>> entityProcessors,
			EnabledEvaluator enabledEvaluator) {
		Assert.notNull(entityProcessors, "Entity processors are required");
		Assert.notNull(enabledEvaluator, "Enabled evaluator is required");
		//
		this.entityProcessors = OrderAwarePluginRegistry.create(entityProcessors);
		this.enabledEvaluator = enabledEvaluator;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends AbstractEntity> EventContext<E> process(EntityEvent<E> event) {		
		return process(event, new DefaultEventContext<>());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends AbstractEntity> EventContext<E> process(EntityEvent<E> event, EventContext<E> context) {
		EntityEvent<E> processEvent = event;
		for(EntityEventProcessor<E> processor : getProcessors(processEvent)) {
			EventResult<E> eventResult = processor.process(processEvent, context);
			// add result to history
			context.addResult(eventResult);
			//
			//  no other events will be processed (break event chain)
			if (eventResult.isCompleted()) {
				break;
			}
			// next event will be called with previous processor result
			processEvent = eventResult.getEvent();
			if (!event.getType().equals(processEvent.getType())) {
				throw new IllegalStateException(MessageFormat.format("Changing event type is not supported [{0} - {1}]!", event.getType(), processEvent.getType()));
			}
		}		
		return context;
	}
	
	@SuppressWarnings("unchecked")
	public <E extends AbstractEntity> List<EntityEventProcessor<E>> getProcessors(EntityEvent<E> event) {
		return entityProcessors.getPluginsFor(event).stream()
				.filter(processor -> {
					if (!enabledEvaluator.isEnabled(processor)) {
						LOG.warn("Processor [{}] is disabled, skipping", processor.getClass());
						return false;
					}
					return true;
				})
				.map(processor -> {
					return (EntityEventProcessor<E>) processor;
				})
				.collect(Collectors.toList());
	}
	
}
