package eu.bcvsolutions.idm.core.api.repository;

import java.util.UUID;

import org.springframework.data.repository.NoRepositoryBean;

import eu.bcvsolutions.idm.core.api.dto.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Common repository for abstract entities
 * 
 * TODO: QueryDslPredicateExecutor<E>
 * 
 * @author Radek Tomiška
 *
 * @param <E> entity type
 * @param <F> basic filter
 */
@NoRepositoryBean
public interface AbstractEntityRepository<E extends BaseEntity, F extends BaseFilter> extends BaseEntityRepository<E, UUID, F> {
}
