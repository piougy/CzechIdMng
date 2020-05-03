package eu.bcvsolutions.idm.core.security.evaluator.identity;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormProjection;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormProjection_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.evaluator.AbstractAuthorizationEvaluator;

/**
 * Permissions to identity by form projection (user type).
 * 
 * @author Radek Tomiška
 * @since 10.3.0
 */
@Component(IdentityByFormProjectionEvaluator.EVALUATOR_NAME)
@Description("Permissions to identity by form projection (user type).")
public class IdentityByFormProjectionEvaluator extends AbstractAuthorizationEvaluator<IdmIdentity> {

	public static final String EVALUATOR_NAME = "core-identity-by-form-projection-evaluator";
	public static final String PARAMETER_FORM_PROJECTION = "form-projection";
	//
	@Autowired private SecurityService securityService;
	
	@Override
	public String getName() {
		return EVALUATOR_NAME;
	}
	
	@Override
	public Predicate getPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder, AuthorizationPolicy policy, BasePermission... permission) {
		if (!hasPermission(policy, permission)) {
			return null;
		}
		if (!securityService.isAuthenticated()) {
			return null;
		}
		UUID formProjection = getFormProjection(policy);
		if (formProjection == null ) {
			return builder.isNull(root.get(IdmIdentity_.formProjection));
		}
		//
		return builder.equal(root.get(IdmIdentity_.formProjection).get(IdmFormProjection_.id), formProjection);
	}
	
	@Override
	public Set<String> getPermissions(IdmIdentity entity, AuthorizationPolicy policy) {
		Set<String> permissions = super.getPermissions(entity, policy);
		if (entity == null || !securityService.isAuthenticated()) {
			return permissions;
		}
		UUID formProjection = getFormProjection(policy);
		IdmFormProjection identityProjection = entity.getFormProjection();
		if (identityProjection == null) {
			// default projection is supported too
			if (formProjection == null) {
				permissions.addAll(policy.getPermissions());
			}
		} else if (Objects.equals(identityProjection.getId(), formProjection)) {
			permissions.addAll(policy.getPermissions());
		}
		return permissions;
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_FORM_PROJECTION);
		//
		return parameters;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto formProjection = new IdmFormAttributeDto(
				PARAMETER_FORM_PROJECTION, 
				PARAMETER_FORM_PROJECTION, 
				PersistentType.UUID, 
				BaseFaceType.FORM_PROJECTION_SELECT
		);
		//
		return Lists.newArrayList(formProjection);
	}
	
	private UUID getFormProjection(AuthorizationPolicy policy) {
		return DtoUtils.toUuid(policy.getEvaluatorProperties().get(PARAMETER_FORM_PROJECTION));
	}
}
