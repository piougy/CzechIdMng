package eu.bcvsolutions.idm.core.model.repository;

import java.util.UUID;

import eu.bcvsolutions.forest.index.repository.ForestIndexRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity;

/**
 * Persists forest tree indexes
 *
 * @author Radek Tomiška
 */
public interface IdmForestIndexEntityRepository extends ForestIndexRepository<IdmForestIndexEntity, UUID> {
	
}
