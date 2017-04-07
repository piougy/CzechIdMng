package eu.bcvsolutions.idm.core.security.evaluator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationEvaluator;

/**
 * Abstract authorization evaluator template.
 * 
 * @author Radek Tomiška
 *
 * @param <E> evaluated {@link Identifiable} type - evaluator is designed for one domain type. 
 */
public abstract class AbstractAuthorizationEvaluator<E extends Identifiable> implements AuthorizationEvaluator<E> {

	private final Class<E> entityClass;

	@SuppressWarnings({ "unchecked" })
	public AbstractAuthorizationEvaluator() {
		this.entityClass = (Class<E>) GenericTypeResolver.resolveTypeArgument(getClass(), AuthorizationEvaluator.class);
	}

	@Override
	public String getModule() {
		return EntityUtils.getModule(this.getClass());
	}

	@Override
	public Class<E> getEntityClass() {
		return entityClass;
	}

	/**
	 * Could be used for {@link #evaluate(BaseEntity, BasePermission)} ordering,
	 * when more evaluators supports the same entity type (if the first one
	 * disapprove, then we dont need to continue etc.).
	 */
	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public boolean supports(Class<?> authorizableType) {
		Assert.notNull(authorizableType);
		//
		return entityClass.isAssignableFrom(authorizableType);
	}
	
	/**
	 * Returns universal configuration parameters. Don't forget to override this method additively.
	 */
	@Override
	public List<String> getParameterNames() {
		// any parameter for now
		return new ArrayList<>();
	}
	
	/**
	 * Returns null as default. Supposed to
	 * be overriden.
	 */
	@Override
	public Predicate getPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder, AuthorizationPolicy policy, BasePermission... permission) {
		return null;
	}

	/**
	 * Returns empty set - no data will be available. Supposed to be overriden.
	 */
	@Override
	public Set<String> getPermissions(E authorizable, AuthorizationPolicy policy) {
		return new HashSet<>();
	}

	/**
	 * Supposed to be overriden for orderable optimalizations.
	 */
	@Override
	public boolean evaluate(E authorizable, AuthorizationPolicy policy, BasePermission... permission) {
		Assert.notEmpty(permission);
		Set<String> permissions = getPermissions(authorizable, policy);
		//		
		return permissions.contains(IdmBasePermission.ADMIN.getName())
				|| hasPermission(permissions, permission);
	}
	
	@Override
	public boolean isDisableable() {
		return true;
	}
	
	@Override
	public boolean isDisabled() {
		// check for processor is enabled, if configuration service is given
//		if (configurationService != null) {
//			return !configurationService.getBooleanValue(
//					getConfigurationPrefix()
//					+ ConfigurationService.PROPERTY_SEPARATOR
//					+ PROPERTY_ENABLED, true);
//		}
		// enabled by default
		return false;
	}
	
	/**
	 * Returns policy's configured base permissons
	 * 
	 * @param policy
	 * @return
	 */
	protected Set<String> getBasePermissions(AuthorizationPolicy policy) {
		Set<String> permissions = new HashSet<>();
		if (StringUtils.isNotEmpty(policy.getBasePermissions())) {
			for (String basePermission : policy.getBasePermissions().split(AuthorizationPolicy.PERMISSION_SEPARATOR)) {
				if(StringUtils.isNotBlank(basePermission)) {
					permissions.add(basePermission.toUpperCase().trim());
				}
			}
		}
		return permissions;
	}
	
	/**
	 * Returns true, when policy has all given permissions
	 * 
	 * @param policy
	 * @param permission permissions to evaluate (AND)
	 * @return
	 */
	protected boolean hasPermission(AuthorizationPolicy policy, BasePermission... permission) {
		Assert.notNull(permission);
		Set<String> permissions = getBasePermissions(policy);
		//
		return permissions.contains(IdmBasePermission.ADMIN.getName())
				|| hasPermission(permissions, permission);
	}
	
	/**
	 * Returns true, when permissions have all given permissions
	 * 
	 * @param permissions
	 * @param permission
	 * @return
	 */
	protected boolean hasPermission(Collection<String> permissions, BasePermission... permission) {
		return permissions.containsAll(Arrays.stream(permission).map(Object::toString).collect(Collectors.toList()));
	}
}
