package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;

/**
 * Provide additional methods to retrieve entities using the pagination and
 * sorting abstraction.
 * 
 * @author Radek Tomiška
 * @see Sort
 * @see Pageable
 * @see Page
 * @deprecated use {@link AbstractReadDtoService}
 */
@Deprecated
public abstract class AbstractReadEntityService<E extends BaseEntity, F extends BaseFilter> implements ReadEntityService<E, F>, ScriptEnabled {
	
	private final Class<E> entityClass;
	private final Class<F> filterClass;
	private final AbstractEntityRepository<E, F> repository;
	private AuthorizationManager authorizationManager;
	@Autowired
	private ApplicationContext context;
	
	@SuppressWarnings("unchecked")
	public AbstractReadEntityService(AbstractEntityRepository<E, F> repository) {
		Class<?>[] genericTypes = GenericTypeResolver.resolveTypeArguments(getClass(), ReadEntityService.class);
		entityClass = (Class<E>)genericTypes[0];
		filterClass = (Class<F>)genericTypes[1];
		//
		Assert.notNull(repository, MessageFormat.format("Repository for class [{0}] is required!", entityClass));
		//
		this.repository = repository;
	}
	
	/**
	 * Returns underlying repository
	 * 
	 * @return
	 */
	protected AbstractEntityRepository<E, F> getRepository() {
		return repository;
	}

	/**
	 * Returns {@link BaseEntity} type class, which is controlled by this service
	 * 
	 * @return
	 */
	@Override
	public Class<E> getEntityClass() {
		return entityClass;
	}
	
	/**
	 * Returns {@link BaseFilter} type class, which is controlled by this service
	 * 
	 * @return
	 */
	@Override
	public Class<F> getFilterClass() {
		return filterClass;
	}	

	/**
	 * Returns entity by given id. Returns null, if entity is not exists. For AbstractEntity uuid or string could be given.
	 */
	@Override
	@Transactional(readOnly = true)
	public E get(Serializable id, BasePermission... permission) {
		if (AbstractEntity.class.isAssignableFrom(getEntityClass()) && (id instanceof String)) {
			// workflow / rest usage with string uuid variant
			// EL does not recognize two methods with the same name and different argument type
			try {
				return getRepository().findOne(UUID.fromString((String)id));
			} catch(IllegalArgumentException ex) {
				// simply not found
				return null;
			}
		}
		return getRepository().findOne((UUID)id);
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
	
	@Override
	public boolean isNew(E entity) {
		Assert.notNull(entity);
		//
		return entity.getId() == null || !getRepository().exists((UUID) entity.getId());
	}
	
	/**
	 * Returns authorization manager
	 * 
	 * @return
	 */
	protected AuthorizationManager getAuthorizationManager() {
		if (authorizationManager == null) {
			authorizationManager = context.getBean(AuthorizationManager.class);
		}
		return authorizationManager;
	}
}
