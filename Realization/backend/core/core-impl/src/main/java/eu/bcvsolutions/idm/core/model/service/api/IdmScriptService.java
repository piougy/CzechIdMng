package eu.bcvsolutions.idm.core.model.service.api;

import eu.bcvsolutions.idm.core.api.dto.filter.ScriptFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.dto.IdmScriptDto;

/**
 * Default service for script
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmScriptService extends ReadWriteDtoService<IdmScriptDto, ScriptFilter> {
	
	/**
	 * Return {@link IdmScriptDto} byt name.
	 * @param name
	 * @return
	 */
	IdmScriptDto getScriptByName(String name);
}
