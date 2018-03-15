package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleAttributeRuleRequestFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleRequestService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute_;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleAttributeRuleRequest;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleAttributeRuleRequest_;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleAttributeRule_;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleRequest_;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmAutomaticRoleAttributeRuleRequestRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Default implementation of automatic role rule request service
 * 
 * @author svandav
 */
@Service("automaticRoleAttributeRuleRequestService")
public class DefaultIdmAutomaticRoleAttributeRuleRequestService extends
		AbstractReadWriteDtoService<IdmAutomaticRoleAttributeRuleRequestDto, IdmAutomaticRoleAttributeRuleRequest, IdmAutomaticRoleAttributeRuleRequestFilter>
		implements IdmAutomaticRoleAttributeRuleRequestService {

	@Autowired
	private IdmFormAttributeService formAttributeService;

	@Autowired
	public DefaultIdmAutomaticRoleAttributeRuleRequestService(
			IdmAutomaticRoleAttributeRuleRequestRepository repository) {
		super(repository);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.AUTOMATICROLEATTRIBUTERULEREQUEST, getEntityClass());
	}

	@Override
	public IdmAutomaticRoleAttributeRuleRequestDto save(IdmAutomaticRoleAttributeRuleRequestDto dto,
			BasePermission... permission) {
		// now isn't possible do equals with string_value (clob), so it is necessary to
		// use only short text
		if ((AutomaticRoleAttributeRuleType.CONTRACT_EAV == dto.getType()
				|| AutomaticRoleAttributeRuleType.IDENTITY_EAV == dto.getType()) && dto.getFormAttribute() != null) {
			IdmFormAttributeDto formAttribute = formAttributeService.get(dto.getFormAttribute());
			if (formAttribute == null) {
				throw new ResultCodeException(CoreResultCode.NOT_FOUND,
						ImmutableMap.of("attribute", dto.getFormAttribute()));
			}
			if (formAttribute.getPersistentType() == PersistentType.TEXT) {
				throw new ResultCodeException(CoreResultCode.AUTOMATIC_ROLE_RULE_PERSISTENT_TYPE_TEXT);
			}
		}
		// check if is filled all necessary attribute
		if ((dto.getType() == AutomaticRoleAttributeRuleType.CONTRACT
				|| dto.getType() == AutomaticRoleAttributeRuleType.IDENTITY)
				&& StringUtils.isEmpty(dto.getAttributeName())) {
			throw new ResultCodeException(CoreResultCode.AUTOMATIC_ROLE_RULE_ATTRIBUTE_EMPTY,
					ImmutableMap.of("automaticRoleId", dto.getId(), "attribute",
							IdmAutomaticRoleAttributeRule_.attributeName.getName()));
		}
		if (dto.getComparison() == AutomaticRoleAttributeRuleComparison.EQUALS && dto.getValue() == null) {
			throw new ResultCodeException(CoreResultCode.AUTOMATIC_ROLE_RULE_ATTRIBUTE_EMPTY,
					ImmutableMap.of("attribute", IdmAutomaticRoleAttributeRule_.value.getName()));
		}
		return super.save(dto, permission);
	}

	@Override
	protected List<Predicate> toPredicates(Root<IdmAutomaticRoleAttributeRuleRequest> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, IdmAutomaticRoleAttributeRuleRequestFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		if (filter.getRoleRequestId() != null) {
			predicates.add(builder.equal(
					root.get(IdmAutomaticRoleAttributeRuleRequest_.request).get(IdmAutomaticRoleRequest_.id),
					filter.getRoleRequestId()));
		}
		if (filter.getFormAttributeId() != null) {
			predicates.add(builder.equal(
					root.get(IdmAutomaticRoleAttributeRuleRequest_.formAttribute).get(IdmFormAttribute_.id),
					filter.getFormAttributeId()));
		}
		if (filter.getRoleId() != null) {
			predicates.add(builder.equal(root.get(IdmAutomaticRoleAttributeRuleRequest_.request)
					.get(IdmAutomaticRoleRequest_.role).get(IdmRole_.id), filter.getRoleId()));
		}
		if (filter.getAutomaticRoleId() != null) {
			predicates
					.add(builder.equal(
							root.get(IdmAutomaticRoleAttributeRuleRequest_.request)
									.get(IdmAutomaticRoleRequest_.automaticRole).get(IdmAutomaticRole_.id),
							filter.getAutomaticRoleId()));
		}
		if (filter.getRuleId() != null) {
			predicates.add(builder.equal(
					root.get(IdmAutomaticRoleAttributeRuleRequest_.rule).get(IdmAutomaticRoleAttributeRule_.id),
					filter.getRuleId()));
		}
		//
		return predicates;
	}

}
