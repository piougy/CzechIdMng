package eu.bcvsolutions.idm.core.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationConfiguration;

/**
 * Configuration for notification routing
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmNotificationConfigurationRepository extends AbstractEntityRepository<IdmNotificationConfiguration> {
	
	List<IdmNotificationConfiguration> findAllByNotificationType(@Param("notificationType") String notificationType);
	
	@Query(value = "select distinct(e.notificationType) from IdmNotificationConfiguration e where e.topic = :topic and (e.level is null or e.level = :level)")
	List<String> findTypes(@Param("topic") String topic, @Param("level") NotificationLevel level);
	
	IdmNotificationConfiguration findByTopicAndLevelAndNotificationType(@Param("topic") String topic,
			@Param("level") NotificationLevel level, @Param("notificationType") String notificationType);
	
	@Query(value = "SELECT e FROM IdmNotificationConfiguration e WHERE "
			+ "e.topic = :topic "
			+ "AND  "
			+ "e.level = null) ")
	IdmNotificationConfiguration findWildcardForTopic(@Param("topic") String topic);
	
	Long countByTopic(@Param("topic") String topic);
}
