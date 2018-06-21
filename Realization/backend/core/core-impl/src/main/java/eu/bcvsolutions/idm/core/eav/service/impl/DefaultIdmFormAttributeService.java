package eu.bcvsolutions.idm.core.eav.service.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleAttributeRuleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleAttributeRuleRequestFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter;
import eu.bcvsolutions.idm.core.eav.api.service.FormValueService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute_;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition_;
import eu.bcvsolutions.idm.core.eav.repository.IdmFormAttributeRepository;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Form attribute (attribute definition) service
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmFormAttributeService 
		extends AbstractReadWriteDtoService<IdmFormAttributeDto, IdmFormAttribute, IdmFormAttributeFilter> 
		implements IdmFormAttributeService {

	private final IdmFormAttributeRepository repository;
	private final PluginRegistry<FormValueService<?>, Class<?>> formValueServices;
	private final IdmAutomaticRoleAttributeRuleService automaticRoleAttributeService;
	@Autowired
	private IdmAutomaticRoleAttributeRuleRequestService automaticRoleAttributeRequestService;
	
	@Autowired
	public DefaultIdmFormAttributeService(
			IdmFormAttributeRepository repository,
			List<? extends FormValueService<?>> formValueServices,
					IdmAutomaticRoleAttributeRuleService automaticRoleAttributeService) {
		super(repository);
		//
		Assert.notNull(formValueServices);
		Assert.notNull(automaticRoleAttributeService);
		//
		this.repository = repository;
		this.formValueServices = OrderAwarePluginRegistry.create(formValueServices);
		this.automaticRoleAttributeService = automaticRoleAttributeService;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.FORMATTRIBUTE, getEntityClass());
	}
	
	@Override
	@Transactional
	public IdmFormAttributeDto saveInternal(IdmFormAttributeDto dto) {
		// default seq
		if (dto.getSeq() == null) {
			dto.setSeq((short) 0);
		}
		// check seq
		return super.saveInternal(dto);
	}
	
	@Override
	@Transactional
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void deleteInternal(IdmFormAttributeDto dto) {
		Assert.notNull(dto);
		// attribute with filled values cannot be deleted
		IdmFormValueFilter filter = new IdmFormValueFilter();
		filter.setAttributeId(dto.getId());
		formValueServices.getPlugins().forEach(formValueService -> {
			if (formValueService.find(filter, new PageRequest(0, 1)).getTotalElements() > 0) {
				throw new ResultCodeException(CoreResultCode.FORM_ATTRIBUTE_DELETE_FAILED_HAS_VALUES, ImmutableMap.of("formAttribute", dto.getCode()));
			}
		});
		// delete all values
		// TODO: add some force delete parameter => rewrite service to event usage
		/* formValueServices.getPlugins().forEach(formValueService -> {
			formValueService.find(filter, null).getContent().forEach(formValue -> {
				formValueService.delete((IdmFormValueDto) formValue);
			});
		});*/
		
		//
		// check rules for automatic role attributes
		IdmAutomaticRoleAttributeRuleFilter automaticRoleRuleFilter = new IdmAutomaticRoleAttributeRuleFilter();
		automaticRoleRuleFilter.setFormAttributeId(dto.getId());
		long totalElements = automaticRoleAttributeService.find(automaticRoleRuleFilter, new PageRequest(0, 1)).getTotalElements();
		if (totalElements > 0) {
			// some automatic roles use this attribute
			throw new ResultCodeException(CoreResultCode.FORM_ATTRIBUTE_DELETE_FAILED_AUTOMATIC_ROLE_RULE_ASSIGNED, ImmutableMap.of("formAttribute", dto.getId()));
		}
		//
		// Check rules requests for automatic role attributes. Deletes relation on this form attribute.
		IdmAutomaticRoleAttributeRuleRequestFilter automaticRoleRuleRequestFilter = new IdmAutomaticRoleAttributeRuleRequestFilter();
		automaticRoleRuleRequestFilter.setFormAttributeId(dto.getId());
		List<IdmAutomaticRoleAttributeRuleRequestDto> ruleRequests = automaticRoleAttributeRequestService.find(automaticRoleRuleRequestFilter, null).getContent();
		ruleRequests.forEach(rule -> {
			rule.setFormAttribute(null);
			automaticRoleAttributeRequestService.save(rule);
		});
		//
		super.deleteInternal(dto);
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmFormAttribute> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			IdmFormAttributeFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// quick - "fulltext"
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.or(
					builder.like(builder.lower(root.get(IdmFormAttribute_.code)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmFormAttribute_.name)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmFormAttribute_.description)), "%" + filter.getText().toLowerCase() + "%")			
					));
		}
		//
		// attribute code
		if (StringUtils.isNotEmpty(filter.getCode())) {
			predicates.add(builder.equal(root.get(IdmFormAttribute_.code), filter.getCode()));
		}
		// definition attributes
		if (filter.getDefinitionId() != null) {
			predicates.add(builder.equal(root.get(IdmFormAttribute_.formDefinition).get(IdmFormDefinition_.id), filter.getDefinitionId()));
		}
		if (StringUtils.isNotEmpty(filter.getDefinitionType())) {
			predicates.add(builder.equal(root.get(IdmFormAttribute_.formDefinition).get(IdmFormDefinition_.type), filter.getDefinitionType()));
		}
		if (StringUtils.isNotEmpty(filter.getDefinitionCode())) {
			predicates.add(builder.equal(root.get(IdmFormAttribute_.formDefinition).get(IdmFormDefinition_.code), filter.getDefinitionCode()));
		}
		if (StringUtils.isNotEmpty(filter.getDefinitionName())) {
			predicates.add(builder.equal(root.get(IdmFormAttribute_.formDefinition).get(IdmFormDefinition_.name), filter.getDefinitionName()));
		}
		//
		return predicates;
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmFormAttributeDto findAttribute(String definitionType, String definitionCode, String attributeName, BasePermission... permission) {
		IdmFormAttribute attribute = repository.findOneByFormDefinition_typeAndFormDefinition_codeAndCode(definitionType, definitionCode, attributeName);
		//
		return toDto(checkAccess(attribute, permission));
	}

}
