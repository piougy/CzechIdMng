package eu.bcvsolutions.idm.core.api.service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Adds event processing to abstract implementation for generic CRUD operations on a repository for a
 * specific type.
 * 
 * @author Radek Tomiška
 *
 * @param <DTO> dto type
 * @param <E> entity type
 * @param <F> filter type
 */
public abstract class AbstractEventableDtoService<DTO extends BaseDto, E extends BaseEntity, F extends BaseFilter>
		extends AbstractReadWriteDtoService<DTO, E, F> 
		implements EventableDtoService<DTO, F> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractEventableDtoService.class);
	//
	private final EntityEventManager entityEventManager;
	
	public AbstractEventableDtoService(
			AbstractEntityRepository<E, F> repository,
			EntityEventManager entityEventManager
			) {
		super(repository);
		//
		Assert.notNull(entityEventManager, "Event manager is required for eventable service!");
		//
		this.entityEventManager = entityEventManager;
	}
	
	@Override
	@Transactional
	public EventContext<DTO> publish(EntityEvent<DTO> event, BasePermission... permission){
		Assert.notNull(event, "Event must be not null!");
		Assert.notNull(event.getContent(), "Content (dto) in event must be not null!");
		//
		checkAccess(toEntity(event.getContent(), null), permission);
		return entityEventManager.process(event);
	}
	
	/**
	 * Publish {@link CoreEvent} only.
	 * 
	 * @see AbstractEntityEventProcessor
	 */
	@Override
	@Transactional
	public DTO save(DTO dto, BasePermission... permission) {
		Assert.notNull(dto);
		//
		if (isNew(dto)) { // create
			LOG.debug("Saving new dto[{}]", dto);
			return publish(new CoreEvent<DTO>(CoreEventType.CREATE, dto), permission).getContent();
		}
		LOG.debug("Saving dto [{}] ", dto);
		return publish(new CoreEvent<DTO>(CoreEventType.UPDATE, dto), permission).getContent();
	}
	
	/**
	 * Publish {@link CoreEvent} only.
	 * 
	 * @see AbstractEntityEventProcessor
	 */
	@Override
	@Transactional
	public void delete(DTO dto, BasePermission... permission) {
		Assert.notNull(dto);
		//
		LOG.debug("Deleting dto [{}]", dto);
		//
		publish(new CoreEvent<DTO>(CoreEventType.DELETE, dto), permission);
	}
}
