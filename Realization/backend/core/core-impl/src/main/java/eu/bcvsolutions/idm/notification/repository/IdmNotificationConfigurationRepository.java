package eu.bcvsolutions.idm.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationConfiguration;

/**
 * Configuration for notification routing
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "notificationConfigurations", //
		path = "notification-configurations", //
		itemResourceRel = "notificationConfiguration",
		exported = false
	)
public interface IdmNotificationConfigurationRepository extends AbstractEntityRepository<IdmNotificationConfiguration, EmptyFilter> {
	
	@Override
	@Query(value = "select e from IdmNotificationConfiguration e")
	Page<IdmNotificationConfiguration> find(EmptyFilter filter, Pageable pageable);
}
