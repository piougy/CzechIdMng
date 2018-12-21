package eu.bcvsolutions.idm.core.eav.service.impl;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListItemDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmCodeListItemFilter;
import eu.bcvsolutions.idm.core.eav.api.service.CodeListManager;
import eu.bcvsolutions.idm.core.eav.api.service.IdmCodeListItemService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmCodeListService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;

/**
 * Code list manager - provide code lists and items through application. 
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
public class DefaultCodeListManager implements CodeListManager {
	
	@Autowired private LookupService lookupService;
	@Autowired private IdmCodeListService codeListService;
	@Autowired private IdmCodeListItemService codeListItemService;
	
	@Override
	public IdmCodeListDto get(Serializable codeListIdentifier, BasePermission... permission) {
		Assert.notNull(codeListIdentifier);
		//
		IdmCodeListDto codeList = (IdmCodeListDto) lookupService.lookupDto(IdmCodeListDto.class, codeListIdentifier);
		if (codeList == null) {
			return null;
		}
		if (ObjectUtils.isEmpty(PermissionUtils.trimNull(permission))) {
			return codeList;
		}
		//
		return codeListService.get(codeList, permission);
		
	}
	
	@Override
	@Transactional
	public IdmCodeListDto create(String code, BasePermission... permission) {
		IdmCodeListDto codeList = new IdmCodeListDto();
		codeList.setCode(code);
		codeList.setName(code);
		//
		return save(codeList, permission);
	}

	@Override
	@Transactional
	public IdmCodeListDto save(IdmCodeListDto codeList, BasePermission... permission) {
		return codeListService.save(codeList, permission);
	}

	@Override
	@Transactional
	public void delete(Serializable codeListIdentifier, BasePermission... permission) {
		IdmCodeListDto codeList = get(codeListIdentifier);
		if (codeList == null) {
			return;
		}
		//
		codeListService.delete(codeList, permission);
	}
	
	@Override
	public List<IdmCodeListItemDto> getItems(Serializable codeListIdentifier, Pageable pageable, BasePermission... permission) {
		Assert.notNull(codeListIdentifier);
		IdmCodeListDto codeList = get(codeListIdentifier);
		Assert.notNull(codeList);
		//
		IdmCodeListItemFilter filter = new IdmCodeListItemFilter();
		filter.setCodeListId(codeList.getId());
		//
		return codeListItemService.find(filter, pageable, permission).getContent();
	}

	@Override
	public IdmCodeListItemDto getItem(Serializable codeListIdentifier, String itemCode, BasePermission... permission) {
		Assert.notNull(codeListIdentifier);
		Assert.notNull(itemCode);
		//
		IdmCodeListDto codeList = get(codeListIdentifier);
		if (codeList == null) {
			return null;
		}
		//
		return codeListItemService.getItem(codeList.getId(), itemCode, permission);
	}

	@Override
	@Transactional
	public IdmCodeListItemDto createItem(Serializable codeListIdentifier, String code, String name, BasePermission... permission) {
		Assert.notNull(codeListIdentifier);
		Assert.notNull(code);
		Assert.notNull(name);
		//
		IdmCodeListDto codeList = get(codeListIdentifier);
		Assert.notNull(codeList);	
		//
		IdmCodeListItemDto item = new IdmCodeListItemDto();
		item.setCodeList(codeList.getId());
		item.setCode(code);
		item.setName(name);
		//
		return saveItem(item, permission);
	}

	@Override
	@Transactional
	public IdmCodeListItemDto saveItem(IdmCodeListItemDto item, BasePermission... permission) {
		return codeListItemService.save(item, permission);
	}

	@Override
	@Transactional
	public void deleteItem(Serializable codeListIdentifier, String itemCode, BasePermission... permission) {
		IdmCodeListItemDto item = getItem(codeListIdentifier, itemCode);
		//
		codeListItemService.delete(item, permission);
	}
}
