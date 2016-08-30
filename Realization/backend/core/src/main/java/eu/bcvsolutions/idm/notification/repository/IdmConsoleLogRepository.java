package eu.bcvsolutions.idm.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import eu.bcvsolutions.idm.core.model.repository.BaseRepository;
import eu.bcvsolutions.idm.notification.domain.NotificationGroupPermission;
import eu.bcvsolutions.idm.notification.entity.IdmConsoleLog;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationLog;

/**
 * Test repository for console logs
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@RepositoryRestResource(exported = false)
public interface IdmConsoleLogRepository extends BaseRepository<IdmConsoleLog> {
	
	@Override
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	Iterable<IdmConsoleLog> findAll();
	
	@Override
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	Page<IdmConsoleLog> findAll(Pageable pageable);
	
	@Override
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	Iterable<IdmConsoleLog> findAll(Sort sort);
	
	@Override
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	IdmConsoleLog findOne(@Param("id") Long id);
	
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
