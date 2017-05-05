package eu.bcvsolutions.idm.core.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationRecipientFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationRecipient;

/**
 * Repository for notification recipients
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmNotificationRecipientRepository extends AbstractEntityRepository<IdmNotificationRecipient, NotificationRecipientFilter> {

	/**
	 * Clears identity id from all recipient (raw recipient remains)
	 * 
	 * @param identity
	 * @return
	 */
	@Modifying
	@Query("update #{#entityName} e set e.identityRecipient = null where e.identityRecipient = :identity")
	int clearIdentity(@Param("identity") IdmIdentity identity);
	

	@Override
	@Query("select e from IdmNotificationRecipient e left join e.notification n where (n.id = ?#{[0].notification})")
	Page<IdmNotificationRecipient> find(NotificationRecipientFilter filter, Pageable pageable);
}
