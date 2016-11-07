package eu.bcvsolutions.idm.core.api.service;

import java.util.UUID;

import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.repository.BaseRepository;

/**
 * Provide additional methods to retrieve entities using the pagination and
 * sorting abstraction.
 * 
 * @author Radek Tomiška
 * @see Sort
 * @see Pageable
 * @see Page
 */
public abstract class AbstractReadEntityService<E extends BaseEntity, F extends BaseFilter> implements ReadEntityService<E, F> {
	
	private final Class<E> entityClass;
	
	@SuppressWarnings("unchecked")
	public AbstractReadEntityService() {
		entityClass = (Class<E>)GenericTypeResolver.resolveTypeArgument(getClass(), BaseEntityService.class);
	}
	
	/**
	 * Returns underlying repository
	 * 
	 * @return
	 */
	protected abstract BaseRepository<E, F> getRepository();

	/**
	 * Returns {@link BaseEntity} type class, which is controlled by this service
	 * 
	 * @return
	 */
	@Override
	public Class<E> getEntityClass() {
		return entityClass;
	}

	@Override
	@Transactional(readOnly = true)
	public E get(UUID id) {
		return getRepository().findOne(id);
	}
	
	@Override
	@Transactional(readOnly = true)
	public E get(String id) {
		try {
			return get(UUID.fromString(id));
		} catch(IllegalArgumentException ex) {
			// simply not found
			return null;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Page<E> find(F filter, Pageable pageable) {
		if (filter == null) {
			return find(pageable);
		}
		return getRepository().find(filter, pageable);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<E> find(Pageable pageable) {
		return getRepository().findAll(pageable);
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(Class<?> delimiter) {
		return entityClass.isAssignableFrom(delimiter);
	}

}
