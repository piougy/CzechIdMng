package eu.bcvsolutions.idm.eav.service.api;

import eu.bcvsolutions.idm.core.api.dto.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;

/**
 * Form definition service
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmFormDefinitionService extends ReadWriteEntityService<IdmFormDefinition, EmptyFilter> {

	/**
	 * Default definition name for type (if no name is given)
	 */
	static final String DEFAULT_DEFINITION_NAME = "default";
	
	/**
	 * Returns form definition by given type and name (unique).
	 * 
	 * @param type required
	 * @param name if name is {@code null}, then {@value #DEFAULT_DEFINITION_NAME} is used.
	 * @return
	 */
	IdmFormDefinition get(String type, String name);
	
}
