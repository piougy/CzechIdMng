package eu.bcvsolutions.idm.core.eav.api.service;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListItemDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmCodeListItemFilter;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * CRUD code list items.
 * 
 * @see CodeListManager
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
public interface IdmCodeListItemService extends 
		EventableDtoService<IdmCodeListItemDto, IdmCodeListItemFilter>,
		AuthorizableService<IdmCodeListItemDto>,
		ScriptEnabled {
	
	/**
	 * Load code list item by code list id and item's code.
	 * 
	 * Items eav values will be available.
	 * 
	 * @param codeListId
	 * @param itemCode
	 * @param permission base permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	IdmCodeListItemDto getItem(UUID codeListId, String itemCode, BasePermission... permission);
	
}
