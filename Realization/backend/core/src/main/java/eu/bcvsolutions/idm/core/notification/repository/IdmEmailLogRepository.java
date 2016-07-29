package eu.bcvsolutions.idm.core.notification.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import eu.bcvsolutions.idm.core.model.repository.BaseRepository;
import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;

/**
 * Repository for sended emails
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@RepositoryRestResource(//
	collectionResourceRel = "emails", //
	path = "emails", //
	itemResourceRel = "email"
)
public interface IdmEmailLogRepository extends BaseRepository<IdmEmailLog> {
	
	@Override
	@RestResource(exported = false)
	<S extends IdmEmailLog> S save(S entity);
	
	@Override
	@RestResource(exported = false)
	void delete(Long id);

	@Override
	@RestResource(exported = false)
	void delete(IdmEmailLog entity);

}
