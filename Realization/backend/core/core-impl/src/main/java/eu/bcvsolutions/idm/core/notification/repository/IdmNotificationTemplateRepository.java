package eu.bcvsolutions.idm.core.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationTemplateFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationTemplate;

/**
 * Repository for stored notification templates
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface IdmNotificationTemplateRepository extends AbstractEntityRepository<IdmNotificationTemplate> {
	
	@Query(value = "SELECT e FROM IdmNotificationTemplate e " +
	        " WHERE "
	        + "("
	        	+ "?#{[0].text} is null "
	        	+ "or lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')} "
	        	+ "or lower(e.code) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')} "
	        	+ "or lower(e.subject) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')} "
        	+ ") "
        	+ "AND "
        		+ " ?#{[0].unmodifiable} is null or e.unmodifiable = ?#{[0].unmodifiable} ")
	Page<IdmNotificationTemplate> find(NotificationTemplateFilter filter, Pageable pageable);

	/**
	 * Find one {@link IdmNotificationTemplate} by name given in parameter
	 * 
	 * @param name
	 * @return
	 */
	IdmNotificationTemplate findOneByName(String name);
	
	/**
	 * Find one {@link IdmNotificationTemplate} by code given in parameter
	 * 
	 * @param code
	 * @return
	 */
	IdmNotificationTemplate findOneByCode(String code);
}
