package eu.bcvsolutions.idm.core.eav.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormFilter;
import eu.bcvsolutions.idm.core.eav.api.service.CommonFormService;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormService;
import eu.bcvsolutions.idm.core.eav.entity.IdmForm;
import eu.bcvsolutions.idm.core.eav.entity.IdmForm_;
import eu.bcvsolutions.idm.core.eav.repository.IdmFormRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
	
/**
 * Common eav forms
 * - Persistent filters, configurable properties (long running tasks, evaluators etc.)
 * 
 * @author Radek Tomiška
 * @since 7.6.0
 */
public class DefaultCommonFormService
		extends AbstractReadWriteDtoService<IdmFormDto, IdmForm, IdmFormFilter> 
		implements IdmFormService, CommonFormService {
	
	private final FormService formService;
	private final IdmFormDefinitionService formDefinitionService;
	
	@Autowired
	public DefaultCommonFormService(
			IdmFormRepository repository, 
			FormService formService,
			IdmFormDefinitionService formDefinitionService) {
		super(repository);
		//
		Assert.notNull(formService);
		Assert.notNull(formDefinitionService);
		//
		this.formService = formService;
		this.formDefinitionService = formDefinitionService;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		// TODO: secure filters, lrt properties etc.
		return null; // new AuthorizableType(CoreGroupPermission.FORM, getEntityClass());
	}
	
	@Override
	public IdmFormDto saveInternal(IdmFormDto dto) {
		List<IdmFormValueDto> values = dto.getValues();
		dto = super.saveInternal(dto);
		// save form values
		dto.setValues(formService.saveValues(dto, dto.getFormDefinition(), values));
		//
		return dto;
	}
	
	/**
	 * Deletes a given entity with all extended attributes
	 * 
	 * @param entity
	 * @throws IllegalArgumentException in case the given entity is {@literal null}.
	 */
	@Override
	@Transactional
	public void deleteInternal(IdmFormDto dto) {
		formService.deleteValues(dto);
		//
		super.deleteInternal(dto);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmFormDto> getForms(Identifiable owner, BasePermission... permission) {
		Assert.notNull(owner);
		Assert.notNull(owner.getId());
		//
		IdmFormFilter filter = new IdmFormFilter();
		filter.setOwnerType(formDefinitionService.getOwnerType(owner));
		filter.setOwnerId(getOwnerId(owner));
		//
		return find(filter, null, permission).getContent();
	}
	
	@Override
	@Transactional
	public IdmFormDto saveForm(Identifiable owner, IdmFormDto form, BasePermission... permission) {
		Assert.notNull(owner);
		Assert.notNull(form);
		//
		if (StringUtils.isBlank(form.getOwnerType())) {
			form.setOwnerType(formDefinitionService.getOwnerType(owner));
		}
		if (form.getOwnerId() == null) {
			form.setOwnerId(getOwnerId(owner));
		}
		if (StringUtils.isEmpty(form.getOwnerCode()) && (owner instanceof Codeable)) {
			// this is just user readable attribute - code can be changed
			form.setOwnerCode(((Codeable) owner).getCode());
		}
		//
		return save(form, permission);
	}
	
	@Override
	@Transactional
	public void deleteForms(Identifiable owner, BasePermission... permission) {
		Assert.notNull(owner);
		//
		getForms(owner).forEach(form -> {
			delete(form, permission);
		});
	}
	
	@Override
	protected IdmFormDto toDto(IdmForm entity, IdmFormDto dto) {
		dto = super.toDto(entity, dto);
		// read form instance
		if (dto != null) {
			dto.setValues(formService.getValues(dto, dto.getFormDefinition()));
		}
		//
		return dto;
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmForm> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmFormFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// text - owner type, owner code
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.like(builder.lower(root.get(IdmForm_.name)), "%" + filter.getText().toLowerCase() + "%"));
		}
		// owner id
		if (filter.getOwnerId() != null) {
			predicates.add(builder.equal(root.get(IdmForm_.ownerId), filter.getOwnerId()));
		}
		// owner type
		if (StringUtils.isNotEmpty(filter.getOwnerType())) {
			predicates.add(builder.equal(root.get(IdmForm_.ownerType), filter.getOwnerType()));
		}
		// key
		if (StringUtils.isNotEmpty(filter.getOwnerCode())) {
			predicates.add(builder.equal(root.get(IdmForm_.ownerCode), filter.getOwnerCode()));
		}
		//
		return predicates;
	}
	
	/**
	 * UUID identifier from given owner.
	 * 
	 * @param owner
	 * @return
	 */
	private UUID getOwnerId(Identifiable owner) {
		Assert.notNull(owner);
		Assert.isInstanceOf(UUID.class, owner.getId(), "Entity with UUID identifier is supported as owner for common forms.");
		//
		return (UUID) owner.getId();
	}
}
