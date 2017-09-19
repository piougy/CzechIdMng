package eu.bcvsolutions.idm.core.api.service;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.AvailableServiceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptAuthorityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmScriptAuthorityFilter;

/**
 * Interface of service for script authority
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface IdmScriptAuthorityService extends ReadWriteDtoService<IdmScriptAuthorityDto, IdmScriptAuthorityFilter> {
	
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
	
	/**
	 * Check if service is allowed for use in script;
	 * 
	 * @param dto
	 * @return
	 */
	boolean isServiceReachable(String serviceName, String className);
}
