package eu.bcvsolutions.idm.core.api.rest;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * Basic interface for DTO controllers..
 * @author svandav
 *
 * @param <DTO>
 * @param <E>
 */

public interface BaseDtoController<E extends AbstractDto> extends BaseController {
	
	static final String TREE_BASE_PATH = "/tree";
}
