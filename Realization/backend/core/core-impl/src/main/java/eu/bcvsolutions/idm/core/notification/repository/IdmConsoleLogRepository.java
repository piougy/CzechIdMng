package eu.bcvsolutions.idm.core.notification.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.notification.domain.NotificationGroupPermission;
import eu.bcvsolutions.idm.core.notification.entity.IdmConsoleLog;

/**
 * Test repository for console logs
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmConsoleLogRepository extends AbstractEntityRepository<IdmConsoleLog> {
	
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
	Optional<IdmConsoleLog> findById(@Param("id") UUID id);
}
