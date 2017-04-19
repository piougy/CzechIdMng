package eu.bcvsolutions.idm.core.model.service.api;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.dto.IdmScriptAuthorityDto;
import eu.bcvsolutions.idm.core.model.dto.filter.ScriptAuthorityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmScriptAuthority;

/**
 * Interface of service for script authority
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface IdmScriptAuthorityService extends ReadWriteDtoService<IdmScriptAuthorityDto, IdmScriptAuthority, ScriptAuthorityFilter> {
	
	/**
	 * Delete all script authorities by script id.
	 *   
	 * @param scriptId
	 */
	void deleteAllByScript(UUID scriptId);
}
