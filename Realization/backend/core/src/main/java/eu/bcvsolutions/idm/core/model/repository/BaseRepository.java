package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.model.entity.BaseEntity;

/**
 * Common repository for base entities
 * 
 * TODO: QueryDslPredicateExecutor<E>
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@NoRepositoryBean
public interface BaseRepository<E extends BaseEntity> extends PagingAndSortingRepository<E, Long> {

	@Override
	@Transactional(timeout = 10)
	Iterable<E> findAll();
}
