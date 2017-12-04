package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleAttributeRuleFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleAttributeRule;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleAttributeRule_;
import eu.bcvsolutions.idm.core.model.repository.IdmAutomaticRoleAttributeRuleRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Service for works with rules for automatic role by attribute
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Service("automaticRoleAttributeRuleService")
public class DefaultIdmAutomaticRoleAttributeRuleService extends
		AbstractReadWriteDtoService<IdmAutomaticRoleAttributeRuleDto, IdmAutomaticRoleAttributeRule, IdmAutomaticRoleAttributeRuleFilter>
		implements IdmAutomaticRoleAttributeRuleService {

	@Autowired
	public DefaultIdmAutomaticRoleAttributeRuleService(
			IdmAutomaticRoleAttributeRuleRepository repository) {
		super(repository);
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmAutomaticRoleAttributeRule> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, IdmAutomaticRoleAttributeRuleFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		if (StringUtils.isNotEmpty(filter.getAttributeName())) {
			predicates.add(builder.equal(root.get(IdmAutomaticRoleAttributeRule_.attributeName), filter.getAttributeName()));
		}
		//
		if (filter.getAutomaticRoleAttributeId() != null) {
			predicates.add(builder.equal(root.get(IdmAutomaticRoleAttributeRule_.automaticRoleAttribute).get(AbstractEntity_.id), filter.getAutomaticRoleAttributeId()));
		}
		//
		if (filter.getFormAttributeId() != null) {
			predicates.add(builder.equal(root.get(IdmAutomaticRoleAttributeRule_.formAttribute).get(AbstractEntity_.id), filter.getFormAttributeId()));
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
			predicates.add(
					builder.or(
							builder.like(builder.lower(root.get(IdmAutomaticRoleAttributeRule_.attributeName)), "%" + filter.getText().toLowerCase() + "%"),
							builder.like(builder.lower(root.get(IdmAutomaticRoleAttributeRule_.value)), "%" + filter.getText().toLowerCase() + "%")
							));
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
		this.find(filter, null).forEach(rule -> this.delete(rule));
	}

}
