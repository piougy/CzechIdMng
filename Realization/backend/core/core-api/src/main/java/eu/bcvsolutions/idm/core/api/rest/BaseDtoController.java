package eu.bcvsolutions.idm.core.api.rest;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;

/**
 * Basic interface for DTO controllers..
 *
 * @param <E>
 * @author svandav
 */

public interface BaseDtoController<E extends BaseDto> extends BaseController {
	
	static final String TREE_BASE_PATH = "/tree";
}
