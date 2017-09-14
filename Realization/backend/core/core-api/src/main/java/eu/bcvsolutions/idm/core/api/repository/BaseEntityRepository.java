package eu.bcvsolutions.idm.core.api.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Common repository for base entities
 * 
 * @param <E> entity type
 * @param <ID> entity identifier type
 * @author Radek Tomiška
 */
@NoRepositoryBean
public interface BaseEntityRepository<E extends BaseEntity, ID extends Serializable> 
		extends PagingAndSortingRepository<E, ID>, JpaSpecificationExecutor<E> {

	/**
	 * Find all is not supposed to be used on big recourd counts
	 */
	@Override
	@Transactional(timeout = 10, readOnly = true)
	Iterable<E> findAll();
}

