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
	 * Generator with seq equals or lower than this constant is marked as system and is not possible created the generator with this seq by REST.
	 */
	static short SYSTEM_SEQ_MAXIMUM = 10;
	
	/**
	 * Return enabled generators for entity type sorted by order from lower to higher.
	 *
	 * @param entityType
	 * @return
	 */
	List<IdmGeneratedValueDto> getEnabledGenerator(Class<? extends Identifiable> entityType); 
}
