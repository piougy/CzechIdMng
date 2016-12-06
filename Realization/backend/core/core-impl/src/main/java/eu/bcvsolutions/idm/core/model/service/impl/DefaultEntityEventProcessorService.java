package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.service.EntityEventProcessorService;

/**
 * Entity processing based on spring plugins
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultEntityEventProcessorService implements EntityEventProcessorService {

	private final PluginRegistry<EntityEventProcessor<?>, EntityEvent<?>> entityProcessors;
	
	@Autowired
	public DefaultEntityEventProcessorService(List<? extends EntityEventProcessor<?>> entityProcessors) {
		Assert.notNull(entityProcessors, "Entity processors are required");
		//
		this.entityProcessors = OrderAwarePluginRegistry.create(entityProcessors);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends AbstractEntity> EntityEvent<E> process(EntityEvent<E> event) {		
		// TODO: immutable event = context - clone
		for(EntityEventProcessor<E> processor : getProcessors(event)) {
			event = processor.process(event);
			//
			//  no other events will be processed (break event chain)
			if (event.isComplete()) {
				break;
			}
		}		
		return event;
	}
	
	@SuppressWarnings("unchecked")
	public <E extends AbstractEntity> List<EntityEventProcessor<E>> getProcessors(EntityEvent<E> event) {
		return entityProcessors.getPluginsFor(event).stream()
				.map(processor -> {
					return (EntityEventProcessor<E>) processor;
				})
				.collect(Collectors.toList());
	}
	
}
