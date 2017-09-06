package eu.bcvsolutions.idm.core.eav.api.service;

import java.util.List;

import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormDefinitionFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Form definition service
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmFormDefinitionService extends 
		ReadWriteDtoService<IdmFormDefinitionDto, IdmFormDefinitionFilter>,
		AuthorizableService<IdmFormDefinitionDto>,
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
