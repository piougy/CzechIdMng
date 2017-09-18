package eu.bcvsolutions.idm.core.model.repository;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorityChange;

/**
 * Authority change repository.
 * 
 * @author Jan Helbich
 *
 */
public interface IdmAuthorityChangeRepository extends
		AbstractEntityRepository<IdmAuthorityChange> {

	IdmAuthorityChange findOneByIdentity_Id(UUID identityId);
	
}
