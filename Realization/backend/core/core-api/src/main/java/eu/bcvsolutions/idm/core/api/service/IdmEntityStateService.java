package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;

/**
 * Persisted entity states.
 * 
 * @author Radek Tomiška
 * @since 8.0.0
 */
public interface IdmEntityStateService extends 
		EventableDtoService<IdmEntityStateDto, IdmEntityStateFilter>,
		ScriptEnabled {
}
