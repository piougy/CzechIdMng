package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleAttributeRuleFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute_;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleAttributeRule;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleAttributeRule_;
import eu.bcvsolutions.idm.core.model.event.AutomaticRoleAttributeRuleEvent;
import eu.bcvsolutions.idm.core.model.event.AutomaticRoleAttributeRuleEvent.AutomaticRoleAttributeRuleEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmAutomaticRoleAttributeRuleRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Service for works with rules for automatic role by attribute
 * 
 * @author Ondrej Kopr
 *
 */
public class DefaultIdmAutomaticRoleAttributeRuleService extends
		AbstractReadWriteDtoService<IdmAutomaticRoleAttributeRuleDto, IdmAutomaticRoleAttributeRule, IdmAutomaticRoleAttributeRuleFilter>
		implements IdmAutomaticRoleAttributeRuleService {

	private final EntityEventManager entityEventManager;
	private IdmFormAttributeService formAttributeService = null;

	@Autowired
	public DefaultIdmAutomaticRoleAttributeRuleService(
			IdmAutomaticRoleAttributeRuleRepository repository,
			EntityEventManager entityEventManager) {
		super(repository);
		//
		Assert.notNull(entityEventManager, "Manager is required.");
		//
		this.entityEventManager = entityEventManager;
	}
	
	@Override
	public IdmAutomaticRoleAttributeRuleDto save(IdmAutomaticRoleAttributeRuleDto dto, BasePermission... permission) {
		// Numeric attribute can be only EAV (for now, even external code is string)
		boolean isAttributeNumeric = false;
		AutomaticRoleAttributeRuleComparison comparison = dto.getComparison();
		// now isn't possible do equals with string_value (clob), so it is necessary to use only short text
		if ((AutomaticRoleAttributeRuleType.CONTRACT_EAV == dto.getType() || AutomaticRoleAttributeRuleType.IDENTITY_EAV == dto.getType()) && dto.getFormAttribute() != null) {
			initFormAttributeService();
			IdmFormAttributeDto formAttribute = formAttributeService.get(dto.getFormAttribute());
			if (formAttribute == null) {
				throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of(
						"attribute", dto.getFormAttribute()));
			}
			if (formAttribute.getPersistentType() == PersistentType.TEXT) {
				throw new ResultCodeException(CoreResultCode.AUTOMATIC_ROLE_RULE_PERSISTENT_TYPE_TEXT);
			}
			if (formAttribute.isMultiple() && (comparison != AutomaticRoleAttributeRuleComparison.EQUALS &&
					comparison != AutomaticRoleAttributeRuleComparison.IS_EMPTY &&
					comparison != AutomaticRoleAttributeRuleComparison.IS_NOT_EMPTY)) {
				throw new ResultCodeException(CoreResultCode.AUTOMATIC_ROLE_RULE_INVALID_COMPARSION_WITH_MULTIPLE_ATTIBUTE, ImmutableMap.of(
						"comparison", comparison.name()));
			}
			// Numeric value now can be only EAV
			PersistentType formAttributePersistenType = formAttribute.getPersistentType();
			isAttributeNumeric = formAttributePersistenType == PersistentType.INT || 
					formAttributePersistenType == PersistentType.DOUBLE ||
					formAttributePersistenType == PersistentType.LONG;
		}
		if ((comparison == AutomaticRoleAttributeRuleComparison.GREATER_THAN_OR_EQUAL ||
				comparison == AutomaticRoleAttributeRuleComparison.LESS_THAN_OR_EQUAL) && !isAttributeNumeric) {
			throw new ResultCodeException(CoreResultCode.AUTOMATIC_ROLE_RULE_COMPARSION_IS_ONLY_FOR_NUMERIC_ATTRIBUTE, ImmutableMap.of(
					"comparison", comparison.name()));
		}
		// check if is filled all necessary attribute
		if ((dto.getType() == AutomaticRoleAttributeRuleType.CONTRACT || dto.getType() == AutomaticRoleAttributeRuleType.IDENTITY) && StringUtils.isEmpty(dto.getAttributeName())) {
			throw new ResultCodeException(CoreResultCode.AUTOMATIC_ROLE_RULE_ATTRIBUTE_EMPTY, ImmutableMap.of(
					"automaticRoleId", dto.getId(),
					"attribute", IdmAutomaticRoleAttributeRule_.attributeName.getName()));
		}
		if ((dto.getType() == AutomaticRoleAttributeRuleType.IDENTITY_EAV || dto.getType() == AutomaticRoleAttributeRuleType.CONTRACT_EAV) && dto.getAutomaticRoleAttribute() == null) {
			throw new ResultCodeException(CoreResultCode.AUTOMATIC_ROLE_RULE_ATTRIBUTE_EMPTY, ImmutableMap.of(
					"automaticRoleId", dto.getId(),
					"attribute", IdmAutomaticRoleAttributeRule_.automaticRoleAttribute.getName()));
		}
		if (comparison == AutomaticRoleAttributeRuleComparison.EQUALS && dto.getValue() == null) {
			throw new ResultCodeException(CoreResultCode.AUTOMATIC_ROLE_RULE_ATTRIBUTE_EMPTY, ImmutableMap.of(
					"attribute", IdmAutomaticRoleAttributeRule_.value.getName()));
		}
		//
		// throw new event
		if (isNew(dto)) {
			return entityEventManager.process(new AutomaticRoleAttributeRuleEvent(AutomaticRoleAttributeRuleEventType.CREATE, dto)).getContent();			
		}
		return entityEventManager.process(new AutomaticRoleAttributeRuleEvent(AutomaticRoleAttributeRuleEventType.UPDATE, dto)).getContent();
	}
	
	@Override
	public void delete(IdmAutomaticRoleAttributeRuleDto dto, BasePermission... permission) {
		entityEventManager.process(new AutomaticRoleAttributeRuleEvent(AutomaticRoleAttributeRuleEventType.DELETE, dto)).getContent();
	}

	@Override
	public List<IdmAutomaticRoleAttributeRuleDto> findAllRulesForAutomaticRole(UUID automaticRole) {
		IdmAutomaticRoleAttributeRuleFilter filter = new IdmAutomaticRoleAttributeRuleFilter();
		filter.setAutomaticRoleAttributeId(automaticRole);
		return this.find(filter, null).getContent();
	}

	@Override
	protected List<Predicate> toPredicates(Root<IdmAutomaticRoleAttributeRule> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, IdmAutomaticRoleAttributeRuleFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		if (StringUtils.isNotEmpty(filter.getAttributeName())) {
			predicates.add(
					builder.equal(root.get(IdmAutomaticRoleAttributeRule_.attributeName), filter.getAttributeName()));
		}
		//
		if (filter.getAutomaticRoleAttributeId() != null) {
			predicates.add(builder.equal(

					root.get(IdmAutomaticRoleAttributeRule_.automaticRoleAttribute).get(AbstractEntity_.id),
					filter.getAutomaticRoleAttributeId()));
		}
		//
		if (filter.getFormAttributeId() != null) {
			predicates.add(builder.equal(root.get(IdmAutomaticRoleAttributeRule_.formAttribute).get(AbstractEntity_.id),
					filter.getFormAttributeId()));
		}
		//
		if (filter.getType() != null) {
			predicates.add(builder.equal(root.get(IdmAutomaticRoleAttributeRule_.type), filter.getType()));
		}
		//
		if (StringUtils.isNotEmpty(filter.getValue())) {
			predicates.add(builder.equal(root.get(IdmAutomaticRoleAttributeRule_.value), filter.getValue()));
		}
		//
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.or(
					builder.like(builder.lower(root.get(IdmAutomaticRoleAttributeRule_.attributeName)),
							"%" + filter.getText().toLowerCase() + "%"),
					builder.like(
							builder.lower(
									root.get(IdmAutomaticRoleAttributeRule_.formAttribute).get(IdmFormAttribute_.name)),
							"%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmAutomaticRoleAttributeRule_.value)),
							"%" + filter.getText().toLowerCase() + "%")));
		}
		//
		return predicates;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.AUTOMATICROLEATTRIBUTERULE, getEntityClass());
	}

	@Override
	public void deleteAllByAttribute(UUID attributeId) {
		IdmAutomaticRoleAttributeRuleFilter filter = new IdmAutomaticRoleAttributeRuleFilter();
		filter.setAutomaticRoleAttributeId(attributeId);
		this.find(filter, null).forEach(rule -> this.deleteRuleWithSkipCheckLastRule(rule));
	}

	@Override
	public void deleteRuleWithSkipCheckLastRule(IdmAutomaticRoleAttributeRuleDto dto) {
		AutomaticRoleAttributeRuleEvent automaticRoleAttributeRuleEvent = new AutomaticRoleAttributeRuleEvent(AutomaticRoleAttributeRuleEventType.DELETE, dto);
		entityEventManager.process(automaticRoleAttributeRuleEvent);
	}

	/**
	 * Init form attribute if needed.
	 * We must add this bean with autowire helper, because exists connection between two this beans. 
	 */
	private void initFormAttributeService() {
		if (this.formAttributeService == null) {
			this.formAttributeService = AutowireHelper.getBean(IdmFormAttributeService.class);
		}
	}
}
