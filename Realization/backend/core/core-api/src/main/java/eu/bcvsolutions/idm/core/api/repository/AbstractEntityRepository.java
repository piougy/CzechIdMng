package eu.bcvsolutions.idm.core.api.repository;

import java.util.UUID;

import org.springframework.data.repository.NoRepositoryBean;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Common repository for abstract entities
 * 
 * TODO: QueryDslPredicateExecutor<E>
 * 
 * @param <E> entity type
 * @author Radek Tomiška
 */
@NoRepositoryBean
public interface AbstractEntityRepository<E extends BaseEntity> extends BaseEntityRepository<E, UUID> {

}
