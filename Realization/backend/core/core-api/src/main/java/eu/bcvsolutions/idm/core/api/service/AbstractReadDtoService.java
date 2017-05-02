package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;

/**
 * Provide additional methods to retrieve DTOs and entities using the pagination
 * and sorting abstraction.
 * 
 * TODO: Move autowired fields to e.g. BeanPostProcessor
 * 
 * @author Svanda
 * @author Radek Tomiška
 * @see Sort
 * @see Pageable
 * @see Page
 *
 * @param <DTO> dto type for entity type
 * @param <F> filter
 */
public abstract class AbstractReadDtoService<DTO extends BaseDto, E extends BaseEntity, F extends BaseFilter>
		implements ReadDtoService<DTO, F>, ScriptEnabled {

	private final Class<E> entityClass;
	private final Class<F> filterClass;
	private final Class<DTO> dtoClass;
	@Autowired
	private ModelMapper modelMapper;
	@Autowired
	private ApplicationContext context;
	//
	private AuthorizationManager authorizationManager;
	private FilterManager filterManager;
	private final AbstractEntityRepository<E, F> repository;

	@SuppressWarnings("unchecked")
	public AbstractReadDtoService(AbstractEntityRepository<E, F> repository) {
		Class<?>[] genericTypes = GenericTypeResolver.resolveTypeArguments(getClass(), AbstractReadDtoService.class);
		entityClass = (Class<E>) genericTypes[1];
		filterClass = (Class<F>) genericTypes[2];
		dtoClass = (Class<DTO>) genericTypes[0];
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
	 * Returns {@link BaseDto} type class, which is controlled by this service
	 * 
	 * @return
	 */
	@Override
	public Class<DTO> getDtoClass() {
		return dtoClass;
	}

	/**
	 * Returns {@link BaseEntity} type class, which is controlled by this
	 * service
	 * 
	 * @return
	 */
	protected Class<E> getEntityClass() {
		return entityClass;
	}

	/**
	 * Returns {@link BaseFilter} type class, which is controlled by this
	 * service
	 * 
	 * @return
	 */
	@Override
	public Class<F> getFilterClass() {
		return filterClass;
	}

	/**
	 * Returns DTO by given id. Returns null, if DTO is not exists. For
	 * AbstractDto uuid or string could be given.
	 */	
	@Override
	@Transactional(readOnly = true)
	public DTO get(Serializable id, BasePermission... permission) {
		return toDto(getEntity(id, permission));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<DTO> find(Pageable pageable, BasePermission... permission) {
		return find(null, pageable, permission);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<DTO> find(final F filter, Pageable pageable, BasePermission... permission) {
		return toDtoPage(findEntities(filter, pageable, permission));
	}
	
	/**
	 * Supposed to be overriden - use super.toPredicates to transform default DataFilter props. 
	 * Transforms given filter to jpa predicate, never returns null.
	 * 
	 * @param filter
	 * @param root
	 * @param query
	 * @param builder
	 * @return
	 */
	protected List<Predicate> toPredicates(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder, F filter) {
		if (filter instanceof DataFilter) {
			return getFilterManager().toPredicates(root, query, builder, (DataFilter) filter);
		}
		return new ArrayList<>();
	}

	/**
	 * Returns entity by given id. Returns null, if entity is not exists. For
	 * AbstractEntity uuid or string could be given.
	 */
	@Transactional(readOnly = true)
	protected E getEntity(Serializable id, BasePermission... permission) {
		if (AbstractEntity.class.isAssignableFrom(getEntityClass()) && (id instanceof String)) {
			// workflow / rest usage with string uuid variant
			// EL does not recognize two methods with the same name and
			// different argument type
			try {
				return getRepository().findOne(UUID.fromString((String) id));
			} catch (IllegalArgumentException ex) {
				// simply not found
				return null;
			}
		}
		E entity = getRepository().findOne((UUID) id);
		if (entity == null) {
			// entity not found
			return null;
		}
		//
		return checkAccess(entity, permission);
	}
	
	@Transactional(readOnly = true)
	protected Page<E> findEntities(Pageable pageable, BasePermission... permission) {
		return findEntities(null, pageable, permission);
	}

	@Transactional(readOnly = true)
	protected Page<E> findEntities(F filter, Pageable pageable, BasePermission... permission) {
		// TODO: remove this if after all dtro services will be rewritten - remove getRepository().find(filter, pageable)
		if (!(this instanceof AuthorizableService)) {
			if (filter == null) {
				return getRepository().findAll(pageable);
			}
			return getRepository().find(filter, pageable);
		}
		// transform filter to criteria
		Specification<E> criteria = new Specification<E>() {
			public Predicate toPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = AbstractReadDtoService.this.toPredicates(root, query, builder, filter);
				//
				// permisions are not evaluated, if no permission was given
				if (!ObjectUtils.isEmpty(permission)) {
					predicates.add(getAuthorizationManager().getPredicate(root, query, builder, permission));
				}
				//
				return query.where(predicates.toArray(new Predicate[predicates.size()])).getRestriction();
			}
		};
		return getRepository().findAll(criteria, pageable);
	}

	@Override
	public boolean supports(Class<?> delimiter) {
		return dtoClass.isAssignableFrom(delimiter);
	}

	/**
	 * 
	 * Converts entity to DTO
	 * 
	 * @param entity
	 * @return
	 */
	protected DTO toDto(E entity) {
		return toDto(entity, null);
	}

	/**
	 * Converts entity to DTO
	 * 
	 * @see Embedded
	 * @param entity
	 * @param dto
	 *            if is not null, then will be use as input to convert
	 * @return
	 */
	public DTO toDto(E entity, DTO dto) {
		if (entity == null) {
			return null;
		}
		if (dto == null) {
			return modelMapper.map(entity, dtoClass);
		}
		modelMapper.map(entity, dto);
		return dto;
	}

	/**
	 * Converts list of entities wrapped to Page object to list of DTOs wrapped
	 * to Page object.
	 * 
	 * @param entityPage
	 * @return
	 */
	protected Page<DTO> toDtoPage(Page<E> entityPage) {
		List<DTO> dtos = this.toDtos(entityPage.getContent(), true);
		PageRequest pageRequest = null;
		if (entityPage.getSize() > 0) {
			pageRequest = new PageRequest(entityPage.getNumber(), entityPage.getSize(), entityPage.getSort());
		}
		Page<DTO> dtoPage = new PageImpl<>(dtos, pageRequest, entityPage.getTotalElements());
		return dtoPage;
	}

	/**
	 * Converts list of entities to list of DTOs
	 * 
	 * @param entities
	 * @param trimmed
	 * @return
	 */
	protected List<DTO> toDtos(List<E> entities, boolean trimmed) {
		if (entities == null) {
			return null;
		}
		List<DTO> dtos = new ArrayList<>();
		entities.forEach(entity -> {
			try {
				DTO newDto = dtoClass.newInstance();
				if (newDto instanceof AbstractDto) {
					((AbstractDto) newDto).setTrimmed(trimmed);
				}
				dtos.add(this.toDto(entity, newDto));
			} catch (InstantiationException | IllegalAccessException e) {
				throw new CoreException(e);
			}
		});
		return dtos;
	}

	/**
	 * Converts DTO to entity
	 * 
	 * @see Embedded
	 * @param entity
	 *            if is not null, then will be use as input to convert
	 * @param dto
	 * @return
	 */
	protected E toEntity(DTO dto, E entity) {
		if (dto == null) {
			return null;
		}
		if (entity != null) {
			modelMapper.map(dto, entity);
			return entity;
		}
		E createdEntity = modelMapper.map(dto, entityClass);
		return createdEntity;
	}
	
	@Override
	public boolean isNew(DTO dto) {
		Assert.notNull(dto);
		//
		return dto.getId() == null || !getRepository().exists((UUID) dto.getId());
	}
	
	
	/**
	 * Returns, what currently logged identity can do with given dto
	 * 
	 * @param backendId
	 * @return
	 */
	@Override
	@Transactional(readOnly = true)
	public Set<String> getPermissions(Serializable id) {
		E entity = getEntity(id);
		Assert.notNull(entity);
		//
		return getAuthorizationManager().getPermissions(entity); // null is for create
	}
	
	/**
	 * Evaluate authorization permission on given entity
	 *  
	 * @param entity
	 * @param permission
	 * @return
	 */
	protected E checkAccess(E entity, BasePermission... permission) {
		Assert.notNull(entity);
		//
		if (!ObjectUtils.isEmpty(permission) && this instanceof AuthorizableService && !getAuthorizationManager().evaluate(entity, permission)) {
			throw new ForbiddenEntityException(entity.getId());
		}
		return entity;
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
	
	protected FilterManager getFilterManager() {
		if (filterManager == null) {
			filterManager = context.getBean(FilterManager.class);
		}
		return filterManager;
	}
	
	protected void setModelMapper(ModelMapper modelMapper) {
		this.modelMapper = modelMapper;
	}
}
