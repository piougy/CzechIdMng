package eu.bcvsolutions.idm.core.api.service;

import java.util.List;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmGeneratedValueDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmGeneratedValueFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Generated values service
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 */
public interface IdmGeneratedValueService
		extends ReadWriteDtoService<IdmGeneratedValueDto, IdmGeneratedValueFilter>,
		AuthorizableService<IdmGeneratedValueDto> {

	/**
	 * Return enabled generators for entity type sorted by order from lower to higher.
	 *
	 * @param entityType
	 * @return
	 */
	List<IdmGeneratedValueDto> getEnabledGenerator(Class<? extends Identifiable> entityType); 
}
