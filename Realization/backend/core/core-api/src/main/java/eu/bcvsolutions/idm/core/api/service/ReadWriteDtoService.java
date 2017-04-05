package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Interface for generic CRUD operations on a repository for a specific DTO type.
 * 
 * @author Svanda
 * @author Radek Tomiška
 *
 * @param <DTO> {@link BaseDto} type
 * @param <E> {@link BaseEntity} type
 * @param <F> {@link BaseFilter} type
 */
public interface ReadWriteDtoService<DTO extends BaseDto, E extends BaseEntity, F extends BaseFilter> extends ReadDtoService<DTO, E, F> {

	/**
	 * Saves a given DTO. Event could be published instead persisting directly.
	 * 
	 * @param dto
	 * @return the saved DTO
	 */
	DTO save(DTO dto);	
	
	/**
	 * Persists a given DTO.
	 * 
	 * @param entity
	 * @return the saved DTO
	 */
	DTO saveInternal(DTO dto);
	
	/**
	 * Saves a given DTO. Event could be published instead persisting dto directly. Authorization policies are evaluated.
	 * 
	 * @param dto
	 * @param permission permission to evaluate
	 * @return the saved DTO
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	DTO save(DTO dto, BasePermission permission);	
	
	/**
	 * Saves all given DTO. Event could be published instead persisting directly.
	 * 
	 * @param dtos
	 * @return the saved DTOs
	 * @throws IllegalArgumentException in case the given entity is {@literal null}.
	 */
	Iterable<DTO> saveAll(Iterable<DTO> dtos);
	
	/**
	 * Deletes a given DTO. Event could be published instead persisting directly.
	 * 
	 * @param dto
	 * @throws IllegalArgumentException in case the given entity is {@literal null}.
	 */
	void delete(DTO dto);
	
	/**
	 * Deletes a given DTO. Event could be published instead persisting dto directly. Authorization policies are evaluated.
	 * 
	 * @param dto
	 * @throws IllegalArgumentException in case the given entity is {@literal null}.
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	void delete(DTO dto, BasePermission permission);
	
	/**
	 * Deletes a given DTO (from repository).
	 * 
	 * @param dto
	 * @throws IllegalArgumentException in case the given entity is {@literal null}.
	 */
	void deleteInternal(DTO dto);
	
	/**
	 * Deletes a dto by given id (from repository).
	 * 
	 * @param dto id
	 * @throws IllegalArgumentException in case the given dto id is {@literal null}.
	 */
	void deleteInternalById(Serializable id);
}
