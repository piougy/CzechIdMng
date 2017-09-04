package eu.bcvsolutions.idm.core.eav.service.api;

import java.util.List;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.dto.IdmFormDefinitionDto;

/**
 * Form definition service
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmFormDefinitionService extends 
		ReadWriteDtoService<IdmFormDefinitionDto, QuickFilter>,
		ScriptEnabled {

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
	IdmFormDefinitionDto findOneByTypeAndCode(String type, String code);
	
	/**
	 * Returns main definition for given type (unique).
	 * 
	 * @param type required
	 * @return
	 */
	IdmFormDefinitionDto findOneByMain(String type);
	
	/**
	 * Returns all definitions by given type
	 * 
	 * @param type required
	 * @return
	 */
	List<IdmFormDefinitionDto> findAllByType(String type);
}
