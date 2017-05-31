package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.IdmScriptAuthorityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.ScriptAuthorityFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.dto.AvailableServiceDto;

/**
 * Interface of service for script authority
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface IdmScriptAuthorityService extends ReadWriteDtoService<IdmScriptAuthorityDto, ScriptAuthorityFilter> {
	
	/**
	 * Delete all script authorities by script id.
	 *   
	 * @param scriptId
	 */
	void deleteAllByScript(UUID scriptId);
	
	/**
	 * Method find all service that can be used in scripts, return className
	 * 
	 * @param serviceName
	 * @return
	 */
	List<AvailableServiceDto> findServices(String serviceName);
}
