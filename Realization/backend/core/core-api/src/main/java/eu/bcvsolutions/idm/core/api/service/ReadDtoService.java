package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Provide additional methods to retrieve entities using the pagination and
 * sorting abstraction for DTO services.
 * 
 * @author Svanda
 * 
 * @see Sort
 * @see Pageable
 * @see Page
 * @param <DTO> {@link BaseDto} type
 * @param <E> {@link BaseEntity} type
 * @param <F> {@link BaseFilter} type
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
	 * Returns DTO by given id. Returns null, if dto is not exists. Authorization policies are evaluated.
	 *
	 * @param id
	 * @param permission permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	DTO getDto(Serializable id, BasePermission... permission);

	/**
	 * Returns page of DTOs
	 * Never throws {@link ForbiddenEntityException} - returning available dtos by given permissions (AND).
	 * 
	 * @param pageable
	 * @param permission permissions to evaluate
	 * @return
	 */
	Page<DTO> findDto(Pageable pageable, BasePermission... permission);
	
	/**
	 * Returns page of DTOs by given filter, authorization permission will be evaluated. 
	 * Never throws {@link ForbiddenEntityException} - returning available dtos by given permissions (AND).
	 * 
	 * @param filter
	 * @param pageable
	 * @param permission permissions to evaluate
	 * @return
	 */
	Page<DTO> findDto(F filter, Pageable pageable, BasePermission... permission);

	/**
	 * Returns page of entities by given filter.
	 * Never throws {@link ForbiddenEntityException} - returning available dtos by given permissions (AND).
	 * 
	 * @param filter
	 * @param pageable
	 * @param permission permissions to evaluate
	 * @return
	 */
	Page<E> find(F filter, Pageable pageable, BasePermission... permission);

	/**
	 * Returns page of entities.
	 * Never throws {@link ForbiddenEntityException} - returning available dtos by given permissions (AND).
	 * 
	 * @param pageable
	 * @param permission permissions to evaluate
	 * @return
	 */
	Page<E> find(Pageable pageable, BasePermission... permission);

	/**
	 * Returns entity by given id. Returns null, if entity is not exists. For AbstractEntity uuid or string could be given.
	 * 
	 * @param id entity identifier
	 * @param permission permissions to evaluate
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	E get(Serializable id, BasePermission... permission);

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
	
	/**
	 * Returns whether the given dto is considered to be new.
	 * 
	 * @param dto must never be {@literal null}
	 * @return
	 */
	boolean isNew(DTO dto);
	
	/**
	 * Returns, what currently logged identity can do with given dto / entity
	 * 
	 * @param backendId
	 * @return
	 */
	Set<String> getPermissions(Serializable id);
	
	/**
	 * Transform entity to dto
	 * 
	 * @param modelMapper
	 */
	void setModelMapper(ModelMapper modelMapper);

}
