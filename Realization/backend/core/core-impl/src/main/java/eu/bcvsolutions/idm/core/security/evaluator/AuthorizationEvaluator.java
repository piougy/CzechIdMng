package eu.bcvsolutions.idm.core.security.evaluator;

import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.core.Ordered;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Authorization policy evaluator. Ensures data security.
 * 
 * @author Radek Tomiška
 *
 * @param <E> evaluated {@link BaseEntity} type - evaluator is designed for one domain type. 
 */
public interface AuthorizationEvaluator<E extends BaseEntity> extends Ordered {
	
	
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
	 * Returns true, when evaluator supports given entityType
	 * 
	 * @param entityType
	 * @return
	 */
	boolean supports(Class< ? extends BaseEntity> entityType);
	
	/**
	 * Returns jpa criteria predicate, which can be used in queries - adds security on entities. 
	 * Predicate with "exist" subquery is recommended 
	 * 
	 * @param root evaluated evaluated {@link BaseEntity} type root 
	 * @param query
	 * @param builder
	 * @return predicate with "exist" subquery is recommended
	 */
	Predicate getPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder);
	
	/**
	 * Returns, what logged user could do with given entity
	 * 
	 * @param entity
	 * @return set of {@link BasePermission}s 
	 */
	Set<BasePermission> evaluate(E entity);
	
	/**
	 * Returns true, when currently logged user has given permission on given entity.
	 * 
	 * @param entity
	 * @param permission
	 * @return
	 */
	boolean evaluate(E entity, BasePermission permission);

}
