package eu.bcvsolutions.idm.core.security.api.service;

import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizationEvaluatorDto;

/**
 * Provides authorization evaluators to target read / write services
 * 
 * @author Radek Tomiška
 *
 */
public interface AuthorizationManager {
	
	/**
	 * Return security predicate for given permission and root by {@link BaseEntity} type.
	 * Calls all registered and enabled {@link AuthorizationEvaluator} which support {@link BaseEntity} type.
	 * 
	 * @param permission
	 * @param root evaluated {@link BaseEntity} type root
	 * @param query
	 * @param builder
	 * @return
	 */
	<E extends BaseEntity> Predicate getPredicate(BasePermission permission, Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder);
	
	/**
	 * Returns, what logged user could do with given entity
	 * 
	 * @param entity
	 * @return
	 */
	<E extends BaseEntity> Set<String> evaluate(E entity);
	
	/**
	 * Returns true, when currently logged user has given permission on given entity.
	 * 
	 * @param entity
	 * @param permission
	 * @return
	 */
	<E extends BaseEntity> boolean evaluate(E entity, BasePermission permission);
	
	/**
	 * Returns supported evaluators definitions
	 * 
	 * @return
	 */
	List<AuthorizationEvaluatorDto> getSupportedEvaluators();
	
	/**
	 * Returns all authorizable types
	 * 
	 * @return
	 */
	List<AuthorizableType> getAuthorizableTypes();
}
