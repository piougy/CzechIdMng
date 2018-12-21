package eu.bcvsolutions.idm.core.eav.service.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListItemDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmCodeListFilter;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmCodeListItemFilter;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.api.service.CodeListManager;
import eu.bcvsolutions.idm.core.eav.api.service.IdmCodeListItemService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmCodeListService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.eav.entity.IdmCodeList;
import eu.bcvsolutions.idm.core.eav.entity.IdmCodeList_;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute_;
import eu.bcvsolutions.idm.core.eav.repository.IdmCodeListRepository;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
	
/**
 * CRUD code lists. Use code list manager instead.
 * 
 * @see CodeListManager
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
public class DefaultIdmCodeListService
		extends AbstractEventableDtoService<IdmCodeListDto, IdmCodeList, IdmCodeListFilter> 
		implements IdmCodeListService {
	
	private final IdmCodeListRepository repository;
	//
	@Autowired private IdmCodeListItemService codeListItemService;
	@Autowired private IdmFormDefinitionService formDefinitionService;
	@Autowired private IdmFormAttributeService formAttributeService;
	
	@Autowired
	public DefaultIdmCodeListService(
			IdmCodeListRepository repository,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
		//
		this.repository = repository;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.CODELIST, getEntityClass());
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmCodeListDto getByCode(String code) {
		return toDto(repository.findOneByCode(code));
	}
	
	@Override
	@Transactional
	public IdmCodeListDto saveInternal(IdmCodeListDto dto) {
		if (isNew(dto)) {
			IdmFormDefinitionDto formDefinition = new IdmFormDefinitionDto();
			formDefinition.setCode(dto.getCode());
			formDefinition.setName(dto.getName());
			formDefinition.setType(formDefinitionService.getOwnerType(IdmCodeListItemDto.class));
			formDefinition.setUnmodifiable(true);
			formDefinition = formDefinitionService.save(formDefinition);
			dto.setFormDefinition(formDefinition);
		} else {
			IdmFormDefinitionDto formDefinition = formDefinitionService.get(dto.getFormDefinition());
			if (!formDefinition.getCode().equals(dto.getCode())
					|| !formDefinition.getName().equals(dto.getName())) {
				formDefinition.setName(dto.getName());
				formDefinition.setCode(dto.getCode());
				//
				dto.setFormDefinition(formDefinitionService.save(formDefinition));
			}
		}
		return super.saveInternal(dto);
	}
	
	@Override
	@Transactional
	public void deleteInternal(IdmCodeListDto dto) {
		Assert.notNull(dto);
		Assert.notNull(dto.getId());
		//
		// delete code list items
		IdmCodeListItemFilter filter = new IdmCodeListItemFilter();
		filter.setCodeListId(dto.getId());
		codeListItemService.find(filter,  null).forEach(codeListItem -> {
			codeListItemService.delete(codeListItem);
		});
		//
		super.deleteInternal(dto);
		//
		// delete form definition
		// previous delete of code list items will delete associated form values.
		formDefinitionService.deleteById(dto.getFormDefinition());
	}
	
	@Override
	protected IdmCodeListDto toDto(IdmCodeList entity, IdmCodeListDto dto) {
		dto = super.toDto(entity, dto);
		if (dto == null) {
			return null;
		}
		if (!dto.isTrimmed()) {
			// set mapped attributes
			IdmFormAttributeFilter filter = new IdmFormAttributeFilter();
			filter.setDefinitionId(dto.getFormDefinition().getId());
			dto.getFormDefinition().setFormAttributes(
					formAttributeService
					.find(filter, getPageableAll(new Sort(IdmFormAttribute_.seq.getName(), IdmFormAttribute_.name.getName())))
					.getContent());
		}
		//
		return dto;
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmCodeList> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmCodeListFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// fulltext
		if (!StringUtils.isEmpty(filter.getText())) {
			predicates.add(builder.or(
				builder.like(builder.lower(root.get(IdmCodeList_.code)), "%" + filter.getText().toLowerCase() + "%"),
				builder.like(builder.lower(root.get(IdmCodeList_.name)), "%" + filter.getText().toLowerCase() + "%")
			));
		}
		// code
		String code = filter.getCode();
		if (StringUtils.isNotEmpty(code)) {
			predicates.add(builder.equal(root.get(IdmCodeList_.code), code));
		}
		//
		return predicates;
	}
}
