package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Provide additional methods to retrieve DTOs and entities using the pagination
 * and sorting abstraction.
 * 
 * @author Svanda
 * @see Sort
 * @see Pageable
 * @see Page
 */
public abstract class AbstractReadDtoService<DTO extends BaseDto, E extends BaseEntity, F extends BaseFilter>
		implements ReadDtoService<DTO, E, F> {

	private final Class<E> entityClass;
	private final Class<F> filterClass;
	private final Class<DTO> dtoClass;
	@Autowired
	private ModelMapper modelMapper;

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
	private Class<E> getEntityClass() {
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
	public DTO getDto(Serializable id) {
		E entity = get(id);
		return toDto(entity);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<DTO> findDto(Pageable pageable) {
		Page<E> page = find(pageable);
		return toDtoPage(page);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<DTO> findDto(F filter, Pageable pageable) {
		Page<E> page = find(filter, pageable);
		return toDtoPage(page);
	}

	/**
	 * Returns entity by given id. Returns null, if entity is not exists. For
	 * AbstractEntity uuid or string could be given.
	 */
	@Override
	@Transactional(readOnly = true)
	public E get(Serializable id) {
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
		return getRepository().findOne((UUID) id);
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
	 * 
	 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(Class<?> delimiter) {
		return dtoClass.isAssignableFrom(delimiter);
	}

	protected DTO toDto(E entity) {
		return toDto(entity, null);
	}

	@Override
	@Transactional
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

	@Override
	@Transactional
	public Page<DTO> toDtoPage(Page<E> entityPage) {
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

	@Override
	@Transactional
	public E toEntity(DTO dto, E entity) {
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

}
