package eu.bcvsolutions.idm.core.security.evaluator.eav;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.entity.IdmCodeListItem;
import eu.bcvsolutions.idm.core.eav.entity.IdmCodeListItem_;
import eu.bcvsolutions.idm.core.eav.entity.IdmCodeList_;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.evaluator.AbstractAuthorizationEvaluator;

/**
 * Permissions to code list items by code list and item codes.
 * 
 * @author Radek Tomiška
 * @since 10.3.0
 */
@Component(CodeListItemByCodeEvaluator.EVALUATOR_NAME)
@Description("Permissions to code list items by code list and item codes.")
public class CodeListItemByCodeEvaluator extends AbstractAuthorizationEvaluator<IdmCodeListItem> {

	public static final String EVALUATOR_NAME = "core-codelist-item-by-code-evaluator";
	public static final String PARAMETER_CODELIST = "codelist";
	public static final String PARAMETER_ITEM_CODES = "item-codes";
	//
	@Autowired private SecurityService securityService;
	
	@Override
	public String getName() {
		return EVALUATOR_NAME;
	}
	
	@Override
	public Predicate getPredicate(Root<IdmCodeListItem> root, CriteriaQuery<?> query, CriteriaBuilder builder, AuthorizationPolicy policy, BasePermission... permission) {
		if (!hasPermission(policy, permission)) {
			return null;
		}
		if (!securityService.isAuthenticated()) {
			return null;
		}
		//
		List<Predicate> predicates = new ArrayList<>(2);
		predicates.add(builder.equal(root.get(IdmCodeListItem_.codeList).get(IdmCodeList_.id), getCodeList(policy)));
		//
		Set<String> itemCodes = getItemCodes(policy);
		if (!itemCodes.isEmpty()) {
			predicates.add(root.get(IdmCodeListItem_.code).in(itemCodes));
		}
		//
		return builder.and(predicates.toArray(new Predicate[predicates.size()]));
	}
	
	@Override
	public Set<String> getPermissions(IdmCodeListItem entity, AuthorizationPolicy policy) {
		Set<String> permissions = super.getPermissions(entity, policy);
		if (entity == null || !securityService.isAuthenticated()) {
			return permissions;
		}
		// code list doesn't fit
		if (!entity.getCodeList().getId().equals(getCodeList(policy))) {
			return permissions;
		}
		// fit form attributes 
		Set<String> itemCodes = getItemCodes(policy);
		if (itemCodes.isEmpty() || itemCodes.contains(entity.getCode())) {
			permissions.addAll(policy.getPermissions());
		}
		//
		return permissions;
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_CODELIST);
		parameters.add(PARAMETER_ITEM_CODES);
		//
		return parameters;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto codeList = new IdmFormAttributeDto(
				PARAMETER_CODELIST, 
				PARAMETER_CODELIST, 
				PersistentType.UUID, 
				BaseFaceType.CODE_LIST_SELECT
		);
		codeList.setRequired(true);
		//
		return Lists.newArrayList(
				codeList,
				new IdmFormAttributeDto(PARAMETER_ITEM_CODES, PARAMETER_ITEM_CODES, PersistentType.SHORTTEXT)
				);
	}
	
	private UUID getCodeList(AuthorizationPolicy policy) {
		return DtoUtils.toUuid(policy.getEvaluatorProperties().get(PARAMETER_CODELIST));
	}
	
	private Set<String> getItemCodes(AuthorizationPolicy policy) {
		Set<String> itemCodes = new HashSet<>();
		String configItemCodes = policy.getEvaluatorProperties().getString(PARAMETER_ITEM_CODES);
		if (StringUtils.isBlank(configItemCodes)) {
			return itemCodes;
		}
		return Arrays
			.stream(configItemCodes.split(","))
			.filter(StringUtils::isNotBlank)
			.map(StringUtils::trim)
			.collect(Collectors.toSet());
	}
}
