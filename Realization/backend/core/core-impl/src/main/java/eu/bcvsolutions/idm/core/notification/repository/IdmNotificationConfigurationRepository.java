package eu.bcvsolutions.idm.core.notification.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationConfiguration;

/**
 * Configuration for notification routing
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmNotificationConfigurationRepository extends AbstractEntityRepository<IdmNotificationConfiguration, EmptyFilter> {
	
	@Override
	@Query(value = "select e from IdmNotificationConfiguration e")
	Page<IdmNotificationConfiguration> find(EmptyFilter filter, Pageable pageable);
	
	List<IdmNotificationConfiguration> findAllByNotificationType(@Param("notificationType") String notificationType);
	
	@Query(value = "select distinct(e.notificationType) from IdmNotificationConfiguration e where e.topic = :topic and (e.level is null or e.level = :level)")
	List<String> findTypes(@Param("topic") String topic, @Param("level") NotificationLevel level);
	
	@Query(value = "SELECT e FROM IdmNotificationConfiguration e WHERE "
			+ "(:topic is null or e.topic = :topic) "
			+ "AND  "
			+ "(:level is null or e.level = :level) ")
	IdmNotificationConfiguration findNotificationByTopicLevel(@Param("topic") String topic, @Param("level") NotificationLevel level);
	
	Long countByTopic(@Param("topic") String topic);
}
