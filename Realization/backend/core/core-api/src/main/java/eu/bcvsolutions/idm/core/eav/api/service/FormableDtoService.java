package eu.bcvsolutions.idm.core.eav.api.service;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.FormableDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;

/**
 * CRUD operations for formable DTO, which supports event processing.
 * 
 * @param <DTO> {@link BaseDto} type
 * @param <F> {@link BaseFilter} type
 * @author Radek Tomiška
 * @since 10.3.3
 */
public interface FormableDtoService<DTO extends FormableDto, F extends BaseFilter> extends EventableDtoService<DTO, F> {
	
}
