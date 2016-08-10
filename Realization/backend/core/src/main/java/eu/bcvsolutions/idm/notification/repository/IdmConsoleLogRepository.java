package eu.bcvsolutions.idm.notification.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import eu.bcvsolutions.idm.core.model.repository.BaseRepository;
import eu.bcvsolutions.idm.notification.entity.IdmConsoleLog;
import eu.bcvsolutions.idm.notification.entity.IdmEmailLog;

/**
 * Test repository for console logs
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@RepositoryRestResource(exported = false)
public interface IdmConsoleLogRepository extends BaseRepository<IdmConsoleLog> {
	
	@Override
	@RestResource(exported = false)
	<S extends IdmConsoleLog> S save(S entity);
	
	@Override
	@RestResource(exported = false)
	void delete(Long id);

	@Override
	@RestResource(exported = false)
	void delete(IdmConsoleLog entity);

}
