package eu.bcvsolutions.idm.core.security.api.service;

import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.core.Ordered;
import org.springframework.security.core.GrantedAuthority;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Authorization policy evaluator. Ensures data security.
 * 
 * @author Radek Tomiška
 *
 * @param <E> evaluated {@link Identifiable} type - evaluator is designed for domain type (superclasses could be used as wildcard). 
 */
public interface AuthorizationEvaluator<E extends Identifiable> extends Ordered {
	
	
	/**
	 * Module identifier
	 * 
	 * @return
	 */
	String getModule();
	
	/**
	 * Returns entity class, which supports this processor
	 * 
	 * @return
	 */
	Class<E> getEntityClass();
	
	/**
	 * Returns true, when evaluator supports given authorizable type
	 * 
	 * @param authorizableType
	 * @return
	 */
	boolean supports(Class<?> authorizableType);
	
	/**
	 * Returns form parameter names for this task
	 * 
	 * @return
	 */
	List<String> getParameterNames();
	
	/**
	 * Returns "resource" permissions for given policy - default authorities after login
	 * 
	 * @param policy
	 * @return
	 */
	Set<GrantedAuthority> getGrantedAuthorities(AuthorizationPolicy policy);
	
	/**
	 * Returns jpa criteria predicate for given policy, which can be used in queries - adds security on entities. 
	 * Predicate with "exist" subquery is recommended. 
	 * Could return {@code null}, if evaluator doesn't want to append a predicate. 
	 * 
	 * @param policy
	 * @param root evaluated {@link BaseEntity} type root 
	 * @param query
	 * @param builder
	 * @return predicate with "exists" subquery is recommended
	 */
	Predicate getPredicate(AuthorizationPolicy policy, Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder);
	
	/**
	 * Returns base permissions - what logged user could do with given authorizable object by given policy
	 * 
	 * @param policy
	 * @param authorizable
	 * @return set of {@link BasePermission}s 
	 */
	Set<String> evaluate(AuthorizationPolicy policy, E authorizable);
	
	/**
	 * Returns true, when currently logged user has given permission on given authorizable object by given policy.
	 * 
	 * @param policy
	 * @param authorizable
	 * @param permission
	 * @return
	 */
	boolean evaluate(AuthorizationPolicy policy, E authorizable, BasePermission permission);
	
	/**
	 * Returns true, when evaluator could be disabled
	 * 
	 * @return
	 */
	boolean isDisableable();
	
	/**
	 * Returns true, when evaluator is disabled
	 * 
	 * @return
	 */
	boolean isDisabled();

}
