package eu.bcvsolutions.idm.core.eav.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListItemDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmCodeListItemFilter;
import eu.bcvsolutions.idm.core.eav.api.service.AbstractFormableService;
import eu.bcvsolutions.idm.core.eav.api.service.CodeListManager;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmCodeListItemService;
import eu.bcvsolutions.idm.core.eav.entity.IdmCodeListItem;
import eu.bcvsolutions.idm.core.eav.entity.IdmCodeListItem_;
import eu.bcvsolutions.idm.core.eav.entity.IdmCodeList_;
import eu.bcvsolutions.idm.core.eav.repository.IdmCodeListItemRepository;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
	
/**
 * CRUD code list items.
 * 
 * @see CodeListManager
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
public class DefaultIdmCodeListItemService
		extends AbstractFormableService<IdmCodeListItemDto, IdmCodeListItem, IdmCodeListItemFilter> 
		implements IdmCodeListItemService {
	
	private final IdmCodeListItemRepository repository;
	private final FormService formService;
	
	@Autowired
	public DefaultIdmCodeListItemService(
			IdmCodeListItemRepository repository,
			FormService formService,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager, formService);
		//
		this.repository = repository;
		this.formService = formService;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.CODELISTITEM, getEntityClass());
	}
	
	@Override
	public IdmCodeListItemDto getItem(UUID codeListId, String itemCode,  BasePermission... permission) {
		IdmCodeListItem item = repository.findOneByCodeList_IdAndCode(codeListId, itemCode);
		//
		checkAccess(item, permission);
		//
		return toDto(item);
	}
	
	@Override
	protected IdmCodeListItemDto toDto(IdmCodeListItem entity, IdmCodeListItemDto dto) {
		dto = super.toDto(entity, dto);
		if (dto == null) {
			return null;
		}
		// load eav attributes
		// TODO: trimmed only?
		IdmCodeListDto codeList = DtoUtils.getEmbedded(dto, IdmCodeListItem_.codeList);
		dto.getEavs().add(formService.getFormInstance(dto, codeList.getFormDefinition()));
		//
		return dto;
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmCodeListItem> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmCodeListItemFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// fulltext
		if (!StringUtils.isEmpty(filter.getText())) {
			predicates.add(builder.or(
				builder.like(builder.lower(root.get(IdmCodeListItem_.code)), "%" + filter.getText().toLowerCase() + "%"),
				builder.like(builder.lower(root.get(IdmCodeListItem_.name)), "%" + filter.getText().toLowerCase() + "%")
			));
		}
		// code
		String code = filter.getCode();
		if (StringUtils.isNotEmpty(code)) {
			predicates.add(builder.equal(root.get(IdmCodeListItem_.code), code));
		}
		// code list
		UUID codeListId = filter.getCodeListId();
		if (codeListId != null) {
			predicates.add(builder.equal(root.get(IdmCodeListItem_.codeList).get(IdmCodeList_.id), codeListId));
		}
		//
		return predicates;
	}
}
