package eu.bcvsolutions.idm.notification.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationRecipient;

/**
 * Repository for notification recipients
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(exported = false)
public interface IdmNotificationRecipientRepository extends CrudRepository<IdmNotificationRecipient, UUID>{

	/**
	 * Clears identity id from all recipient (raw recipient remains)
	 * 
	 * @param identity
	 * @return
	 */
	@Modifying
	@Query("update #{#entityName} e set e.identityRecipient = null where e.identityRecipient = :identity")
	int clearIdentity(@Param("identity") IdmIdentity identity);
	
}
