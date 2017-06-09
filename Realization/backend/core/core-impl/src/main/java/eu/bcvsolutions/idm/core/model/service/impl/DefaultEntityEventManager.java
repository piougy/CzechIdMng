package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.EntityEventProcessorDto;
import eu.bcvsolutions.idm.core.api.dto.filter.EntityEventProcessorFilter;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;

/**
 * Entity processing based on event publishing.
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultEntityEventManager implements EntityEventManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultEntityEventManager.class);
	private final ApplicationContext context;
	private final ApplicationEventPublisher publisher;
	private final EnabledEvaluator enabledEvaluator;
	private final LookupService lookupService;
	
	public DefaultEntityEventManager(
			ApplicationContext context, 
			ApplicationEventPublisher publisher,
			EnabledEvaluator enabledEvaluator,
			LookupService lookupService) {
		Assert.notNull(context, "Spring context is required");
		Assert.notNull(publisher, "Event publisher is required");
		Assert.notNull(enabledEvaluator, "Enabled evaluator is required");
		Assert.notNull(lookupService, "LookupService is required");
		//
		this.context = context;
		this.publisher = publisher;
		this.enabledEvaluator = enabledEvaluator;
		this.lookupService = lookupService;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <E extends Serializable> EventContext<E> process(EntityEvent<E> event) {
		Assert.notNull(event);
		Serializable content = event.getContent();
		//
		LOG.debug("Publishing event [{}] [{}]", content.getClass().getSimpleName(), event.getType());
		//
		// continue suspended event
		event.getContext().setSuspended(false);
		//
		// read previous (original) dto source - usable in "check modification" processors
		if (event.getOriginalSource() == null && (content instanceof AbstractDto)) { // original source could be set externally
			AbstractDto contentDto = (AbstractDto) content;
			// works only for dto modification
			if (contentDto.getId() != null && lookupService.getDtoLookup(contentDto.getClass()) != null) {
				event.setOriginalSource((E) lookupService.lookupDto(contentDto.getClass(), contentDto.getId()));
			}
		}
		//
		this.publisher.publishEvent(event); 
		LOG.debug("Publishing event [{}] [{}] is completed", content.getClass().getSimpleName(), event.getType());
		//
		return event.getContext();
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public List<EntityEventProcessorDto> find(EntityEventProcessorFilter filter) {
		List<EntityEventProcessorDto> dtos = new ArrayList<>();
		Map<String, EntityEventProcessor> processors = context.getBeansOfType(EntityEventProcessor.class);
		for(Entry<String, EntityEventProcessor> entry : processors.entrySet()) {
			EntityEventProcessor<?> processor = entry.getValue();
			// entity event processor depends on module - we could not call any processor method
			if (!enabledEvaluator.isEnabled(processor)) {
				continue;
			}
			if (filter != null && filter.getContentClass() != null && !filter.getContentClass().isAssignableFrom(processor.getEntityClass())) {
				continue;
			}
			EntityEventProcessorDto dto = new EntityEventProcessorDto();
			dto.setId(entry.getKey());
			dto.setName(processor.getName());
			dto.setModule(processor.getModule());
			dto.setEntityType(processor.getEntityClass().getSimpleName());
			dto.setEventTypes(Lists.newArrayList(processor.getEventTypes()));
			dto.setClosable(processor.isClosable());
			dto.setDisabled(processor.isDisabled());
			dto.setDisableable(processor.isDisableable());
			dto.setOrder(processor.getOrder());
			// resolve documentation
			dto.setDescription(AutowireHelper.getBeanDescription(dto.getId()));
			dto.setConfigurationProperties(processor.getConfigurationMap());
			dtos.add(dto);
		};
		LOG.debug("Returning [{}] registered entity event processors", dtos.size());
		return dtos;
	}	
}
