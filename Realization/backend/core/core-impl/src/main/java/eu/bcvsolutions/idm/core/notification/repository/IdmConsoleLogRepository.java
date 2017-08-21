package eu.bcvsolutions.idm.core.notification.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.notification.domain.NotificationGroupPermission;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmConsoleLog;

/**
 * Test repository for console logs
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmConsoleLogRepository extends AbstractEntityRepository<IdmConsoleLog, NotificationFilter> {
	
	/**
	 * @deprecated use IdmConsoleLogService (uses criteria api)
	 */
	@Override
	@Deprecated
	@Query(value = "select e from #{#entityName} e")
	default Page<IdmConsoleLog> find(NotificationFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Use IdmConsoleLogService (uses criteria api)");
	}
	
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
	IdmConsoleLog findOne(@Param("id") UUID id);
	
	@Override
	@RestResource(exported = false)
	<S extends IdmConsoleLog> S save(S entity);
	
	@Override
	@RestResource(exported = false)
	void delete(UUID id);

	@Override
	@RestResource(exported = false)
	void delete(IdmConsoleLog entity);

}
