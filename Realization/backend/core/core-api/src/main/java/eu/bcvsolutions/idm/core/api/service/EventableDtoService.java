package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Adds event processing to generic CRUD operations on a repository for a specific DTO type.
 * 
 * @param <DTO> {@link BaseDto} type
 * @param <F> {@link BaseFilter} type
 * @author Radek Tomiška
 */
public interface EventableDtoService<DTO extends BaseDto, F extends BaseFilter> extends 
	ReadWriteDtoService<DTO, F>,
	EventableService<DTO> {
	
}
