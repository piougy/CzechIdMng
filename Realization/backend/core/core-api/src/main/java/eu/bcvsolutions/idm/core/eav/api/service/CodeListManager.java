package eu.bcvsolutions.idm.core.eav.api.service;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListItemDto;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Code list manager - provide code lists and items through application. 
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
public interface CodeListManager extends ScriptEnabled {

	/**
	 * Load code list by code
	 * 
	 * @param codeListIdentifier UUID or coe can be given
	 * @param permission base permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 * @see Codeable
	 */
	IdmCodeListDto get(Serializable codeListIdentifier, BasePermission... permission);
	
	/**
	 * Creates new code list with given code. Name will be the same as code (use {@link #save(IdmCodeListDto, BasePermission...)}, 
	 * if additional attributes has to be set).
	 * 
	 * @param code
	 * @param permission base permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	IdmCodeListDto create(String code, BasePermission... permission);
	
	/**
	 * Save code list
	 * 
	 * @param codeList
	 * @param permission base permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	IdmCodeListDto save(IdmCodeListDto codeList, BasePermission... permission);
	
	/**
	 * Delete code list
	 * 
	 * @param codeListIdentifier UUID or coe can be given
	 * @param permission base permissions to evaluate (AND)
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 * @see Codeable
	 */
	void delete(Serializable codeListIdentifier, BasePermission... permission);
	
	/**
	 * Load code list items by code list identifier ({@link Codeable}).
	 * 
	 * @param codeListIdentifier UUID or code can be given
	 * @param permission base permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 * @see Codeable
	 */
	List<IdmCodeListItemDto> getItems(Serializable codeListIdentifier, Pageable pageable, BasePermission... permission);
	
	/**
	 * Load code list item by code list identifier ({@link Codeable}) and item's code.
	 * 
	 * Items eav values will be available.
	 * 
	 * @param codeListIdentifier UUID or code can be given
	 * @param itemCode item's code
	 * @param permission base permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 * @see Codeable
	 */
	IdmCodeListItemDto getItem(Serializable codeListIdentifier, String itemCode, BasePermission... permission);
	
	/**
	 * Creates new code list item
	 * 
	 * @param codeListIdentifier UUID or code can be given
	 * @param code item's code
	 * @param name item's value
	 * @return
	 * @see Codeable
	 */
	IdmCodeListItemDto createItem(Serializable codeListIdentifier, String code, String name, BasePermission... permission);
	
	/**
	 * Save code list item
	 * 
	 * @param item
	 * @param permission base permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	IdmCodeListItemDto saveItem(IdmCodeListItemDto item, BasePermission... permission);
	
	/**
	 * Deletes item
	 * 
	 * @param codeListIdentifier UUID or code can be given
	 * @param itemCode
	 * @param permission base permissions to evaluate (AND)
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 * @see Codeable
	 */
	void deleteItem(Serializable codeListIdentifier, String itemCode, BasePermission... permission);
	
}
