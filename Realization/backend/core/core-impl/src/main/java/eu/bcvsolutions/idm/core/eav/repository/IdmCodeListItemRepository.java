package eu.bcvsolutions.idm.core.eav.repository;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.eav.entity.IdmCodeListItem;

/**
 * Repository for code list items
 * 
 * @author Radek Tomiška 
 * @since 9.4.0
 */
public interface IdmCodeListItemRepository extends AbstractEntityRepository<IdmCodeListItem> {
	
	/**
	 * Load code list item by code list id and item's code.
	 * 
	 * @param codeListId
	 * @param code
	 * @return
	 */
	IdmCodeListItem findOneByCodeList_IdAndCode(UUID codeListId, String code);
}
