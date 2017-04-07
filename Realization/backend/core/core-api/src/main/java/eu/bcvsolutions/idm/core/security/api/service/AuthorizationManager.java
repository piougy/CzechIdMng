package eu.bcvsolutions.idm.core.security.api.service;

import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
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
	 * Return security predicate for given permissions and root by {@link BaseEntity} type.
	 * Calls all registered and enabled {@link AuthorizationEvaluator} which support {@link BaseEntity} type.
	 * 
	 * @param root evaluated {@link BaseEntity} type root
	 * @param query
	 * @param builder
	 * @param permission permissions to evaluate (and)
	 * @return
	 */
	<E extends Identifiable> Predicate getPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder, BasePermission... permission);
	
	/**
	 * Returns, what logged user could do with given entity
	 * 
	 * @param entity
	 * @return
	 */
	<E extends Identifiable> Set<String> getPermissions(E entity);
	
	/**
	 * Returns true, when currently logged user has all given permissions on given entity.
	 * 
	 * @param entity
	 * @param permission permissions to evaluate (AND)
	 * @return
	 */
	<E extends Identifiable> boolean evaluate(E entity, BasePermission... permission);
	
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
