package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Interface for generic CRUD operations on a repository for a specific DTO type.
 * 
 * @author Svanda
 *
 */
public interface ReadWriteDtoService<DTO extends BaseDto, E extends BaseEntity, F extends BaseFilter> extends ReadDtoService<DTO, E, F> {

	/**
	 * Saves a given dto.
	 * @param entity
	 * @return the saved dto
	 */
	DTO saveDto(DTO entity);
	
	/**
	 * Saves all given dto.
	 * 
	 * @param dtos
	 * @return the saved dtos
	 * @throws IllegalArgumentException in case the given entity is {@literal null}.
	 */
	Iterable<DTO> saveAllDto(Iterable<DTO> dtos);
	
	/**
	 * Deletes a given entity.
	 * 
	 * @param dto
	 * @throws IllegalArgumentException in case the given entity is {@literal null}.
	 */
	void deleteDto(DTO dto);
	
	/**
	 * Deletes a given dto id.
	 * 
	 * @param dto id
	 * @throws IllegalArgumentException in case the given dto id is {@literal null}.
	 */
	void deleteDtoById(Serializable id);
}
