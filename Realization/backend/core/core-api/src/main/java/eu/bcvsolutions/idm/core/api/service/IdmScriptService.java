package eu.bcvsolutions.idm.core.api.service;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmScriptFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Default service for script
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface IdmScriptService extends
		ReadWriteDtoService<IdmScriptDto, IdmScriptFilter>, 
		Recoverable<IdmScriptDto>,
		AuthorizableService<IdmScriptDto>,
		CodeableService<IdmScriptDto>,
		CloneableService<IdmScriptDto> {
	
	/**
	 * Folder for scanning / initializing / redeploy scripts.
	 */
	String SCRIPT_FOLDER = "idm.sec.core.script.folder";
	
	/**
	 * Duplicate (create/persist new) script definition with all configurations.
	 * 
	 * @param id
	 * @return
	 */
	IdmScriptDto duplicate (UUID id);
}
