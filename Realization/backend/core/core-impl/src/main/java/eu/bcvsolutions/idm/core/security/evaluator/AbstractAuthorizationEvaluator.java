package eu.bcvsolutions.idm.core.security.evaluator;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.core.GenericTypeResolver;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Abstract authorization evaluator template.
 * 
 * @author Radek Tomiška
 *
 * @param <E> evaluated {@link BaseEntity} type - evaluator is designed for one domain type. 
 */
public abstract class AbstractAuthorizationEvaluator<E extends BaseEntity> implements AuthorizationEvaluator<E> {

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
	public boolean supports(Class<? extends BaseEntity> entityType) {
		Assert.notNull(entityType);
		//
		return entityType.isAssignableFrom(entityClass);
	}

	/**
	 * Returns disjunction as default - no data will be available. Supposed to
	 * be overriden.
	 */
	@Override
	public Predicate getPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
		return builder.disjunction();
	}

	/**
	 * Returns empty set - no data will be available. Supposed to be overriden.
	 */
	@Override
	public Set<BasePermission> evaluate(E entity) {
		return new HashSet<>();
	}

	/**
	 * Returns false - no data will be available. Supposed to be overriden.
	 */
	@Override
	public boolean evaluate(E entity, BasePermission permission) {
		return false;
	}

}
