package eu.bcvsolutions.idm.core.security.evaluator;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Share entity with uuid
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Share entity by uuid")
public class UuidEvaluator extends AbstractAuthorizationEvaluator<Identifiable> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UuidEvaluator.class);
	public static final String PARAMETER_UUID = "uuid";
	
	@Override
	public boolean supports(Class<?> authorizableType) {
		Assert.notNull(authorizableType);
		// uuid superclasses only
		return super.supports(authorizableType)
				&& (AbstractEntity.class.isAssignableFrom(authorizableType) || AbstractDto.class.isAssignableFrom(authorizableType));
	}
	
	@Override
	public Predicate getPredicate(Root<Identifiable> root, CriteriaQuery<?> query, CriteriaBuilder builder, AuthorizationPolicy policy, BasePermission... permission) {
		if (!hasPermission(policy, permission)) {
			return null;
		}
		//
		UUID uuid = getUuid(policy);		
		if (uuid == null) { 
			return null;
		}
		return builder.equal(root.get(AbstractEntity_.id.getName()), uuid);
	}
	
	@Override
	public Set<String> getPermissions(Identifiable entity, AuthorizationPolicy policy) {
		Set<String> permissions = super.getPermissions(entity, policy);
		if (entity == null) {
			return permissions;
		}	
		UUID uuid = getUuid(policy);
		if (uuid != null && uuid.equals(entity.getId())) {
			permissions.addAll(policy.getPermissions());
		}
		return permissions;
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_UUID);
		return parameters;
	}
	
	private UUID getUuid(AuthorizationPolicy policy) {
		try {
			return policy.getEvaluatorProperties().getUuid(PARAMETER_UUID);
		} catch (ClassCastException ex) {
			LOG.warn("Wrong uuid for authorization evaluator - skipping.", ex);
			return null;
		}
	}
}
