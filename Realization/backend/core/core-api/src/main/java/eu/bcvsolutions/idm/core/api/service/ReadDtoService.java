package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Provide additional methods to retrieve entities using the pagination and
 * sorting abstraction for DTO services.
 * 
 * @author Svanda
 */
public interface ReadDtoService<DTO extends BaseDto, F extends BaseFilter> extends BaseDtoService<DTO> {

	/**
	 * Returns {@link BaseFilter} type class, which is controlled by this service
	 * 
	 * @return
	 */
	Class<F> getFilterClass();
	
	/**
	 * Returns dto by given id. Returns null, if dto is not exists.
	 * 
	 * @param id
	 * @return
	 */
	DTO getDto(Serializable id);
	
	/**
	 * Returns page of entities
	 * 
	 * @param pageable
	 * @return
	 */
	Page<DTO> findDto(Pageable pageable);
	
	/**
	 * Returns page of entities by given filter
	 * 
	 * @param filter
	 * @param pageable
	 * @return
	 */
	Page<DTO> findDto(F filter, Pageable pageable);
}
