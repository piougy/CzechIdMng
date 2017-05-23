package eu.bcvsolutions.idm.core.eav.service.api;

import java.util.List;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;

/**
 * Form definition service
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmFormDefinitionService extends ReadWriteEntityService<IdmFormDefinition, QuickFilter> {

	/**
	 * Default definition name for type (if no name is given)
	 */
	static final String DEFAULT_DEFINITION_CODE = "default";
	
	/**
	 * Returns form definition by given type and code (unique).
	 * 
	 * @param type required
	 * @param code [optional] if code is {@code null}, then main definition for given type is used.
	 * @return
	 */
	IdmFormDefinition findOneByTypeAndCode(String type, String code);
	
	/**
	 * Returns main definition for given type (unique).
	 * 
	 * @param type required
	 * @return
	 */
	IdmFormDefinition findOneByMain(String type);
	
	/**
	 * Returns all definitions by given type
	 * 
	 * @param type required
	 * @return
	 */
	List<IdmFormDefinition> findAllByType(String type);
}
