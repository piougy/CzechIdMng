package eu.bcvsolutions.idm.core.api.service;

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
		CodeableService<IdmScriptDto> {

	/**
	 * Return {@link IdmScriptDto} by name. This method return script by name,
	 * for one name may exist one or more script, method return only first!
	 * For code use method {@link #getByCode(String)}.
	 * 
	 * @param name
	 * @return
	 * @deprecated use {@link #getByCode(String)}
	 */
	@Deprecated
	IdmScriptDto getScriptByName(String name);
	
	/**
	 * Method return script founded by code.
	 * 
	 * @param code
	 * @return
	 * @deprecated use {@link #getByCode(String)}
	 */
	@Deprecated
	IdmScriptDto getScriptByCode(String code);
}
