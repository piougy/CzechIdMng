package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.forest.index.repository.ForestIndexRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity;

/**
 * Persists forest tree indexes
 *
 * @author Radek Tomiška
 */
@RepositoryRestResource(
		exported = false
)
public interface IdmForestIndexEntityRepository extends ForestIndexRepository<IdmForestIndexEntity> {
	
}
