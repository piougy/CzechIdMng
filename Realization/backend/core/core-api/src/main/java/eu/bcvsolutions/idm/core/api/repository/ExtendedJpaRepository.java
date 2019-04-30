package eu.bcvsolutions.idm.core.api.repository;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Fix: count without fetches
 * 
 * @author Radek Tomiška
 *
 * @param <E>
 * @since 9.6.0
 */
@NoRepositoryBean
public class ExtendedJpaRepository<E, ID extends Serializable>
		extends SimpleJpaRepository<E, ID> {
	
	private EntityManager entityManager;

	public ExtendedJpaRepository(JpaEntityInformation<E, ?> entityInformation, EntityManager entityManager) {
		super(entityInformation, entityManager);
		//
		this.entityManager = entityManager;
	}
	
	public ExtendedJpaRepository(Class<E> domainClass, EntityManager entityManager) {
		this(JpaEntityInformationSupport.getEntityInformation(domainClass, entityManager), entityManager);
	}
	
	/**
	 * Find all is not supposed to be used on big record counts
	 */
	@Override
	@Transactional(timeout = 10, readOnly = true)
	public List<E> findAll() {
		return super.findAll();
	}
	
	@Override
	protected TypedQuery<Long> getCountQuery(Specification<E> spec) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		//
		Root<E> root = applySpecificationToCriteria(spec, query);
		//
		if (query.isDistinct()) {
			query.select(builder.countDistinct(root));
		} else {
			query.select(builder.count(root));
		}
		//
		// Remove all Orders the Specifications might have applied.
		query.orderBy(Collections.<Order> emptyList());
		// Remove all fetches
		root.getFetches().clear();
		//
		return entityManager.createQuery(query);
	}
	
	private <S> Root<E> applySpecificationToCriteria(Specification<E> spec, CriteriaQuery<S> query) {
		Assert.notNull(query);
		Root<E> root = query.from(getDomainClass());
		//
		if (spec == null) {
			return root;
		}
		//
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		Predicate predicate = spec.toPredicate(root, query, builder);
		if (predicate != null) {
			query.where(predicate);
		}
		//
		return root;
	}
}
