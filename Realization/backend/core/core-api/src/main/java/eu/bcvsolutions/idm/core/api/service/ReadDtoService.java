package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Provide additional methods to retrieve entities using the pagination and
 * sorting abstraction for DTO services.
 * 
 * @author Svanda
 */
public interface ReadDtoService<DTO extends BaseDto, E extends BaseEntity, F extends BaseFilter>
		extends BaseDtoService<DTO> {

	/**
	 * Returns {@link BaseFilter} type class, which is controlled by this
	 * service
	 * 
	 * @return
	 */
	Class<F> getFilterClass();

	/**
	 * Returns DTO by given id. Returns null, if dto is not exists.
	 * 
	 * @param id
	 * @return
	 */
	DTO getDto(Serializable id);

	/**
	 * Returns page of DTOs
	 * 
	 * @param pageable
	 * @return
	 */
	Page<DTO> findDto(Pageable pageable);

	/**
	 * Returns page of DTOs by given filter
	 * 
	 * @param filter
	 * @param pageable
	 * @return
	 */
	Page<DTO> findDto(F filter, Pageable pageable);

	/**
	 * Returns page of entities by given filter
	 * 
	 * @param filter
	 * @param pageable
	 * @return
	 */
	Page<E> find(F filter, Pageable pageable);

	/**
	 * Returns page of entities
	 * 
	 * @param pageable
	 * @return
	 */
	Page<E> find(Pageable pageable);

	/**
	 * Returns entity by given id. Returns null, if entity is not exists. For
	 * AbstractEntity uuid or string could be given.
	 */
	E get(Serializable id);

	/**
	 * Converts entity to DTO
	 * 
	 * @see Embedded
	 * @param entity
	 * @param dto
	 *            if is not null, then will be use as input to convert
	 * @return
	 */
	DTO toDto(E entity, DTO dto);

	/**
	 * Converts DTO to entity
	 * 
	 * @see Embedded
	 * @param entity
	 *            if is not null, then will be use as input to convert
	 * @param dto
	 * @return
	 */
	E toEntity(DTO dto, E entity);

	/**
	 * Converts list of entities wrapped to Page object to list of DTOs wrapped
	 * to Page object.
	 * 
	 * @param entityPage
	 * @return
	 */
	Page<DTO> toDtoPage(Page<E> entityPage);

}
