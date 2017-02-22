package eu.bcvsolutions.idm.core.notification.service.api;

import java.util.Map;

import eu.bcvsolutions.idm.core.api.service.IdentifiableByNameEntityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationTemplateFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationTemplate;

/**
 * Interface with basic method for apache velocity templates engine
 * Initialization velocity engine in constructor.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmNotificationTemplateService extends ReadWriteEntityService<IdmNotificationTemplate, NotificationTemplateFilter>, IdentifiableByNameEntityService<IdmNotificationTemplate> {
	
	/**
	 * Return {@link IdmNotificationTemplate} by given code.
	 * 
	 * @param code
	 * @return
	 */
	IdmNotificationTemplate getTemplateByCode(String code);
	
	/**
	 * Return {@link IdmMessage} with evaluated template and parameters given in model.
	 * Entity {@link IdmNotificationTemplate} is get from repository by given code.
	 * 
	 * @param code
	 * @param model
	 * @return
	 */
	IdmMessage getMessage(String code, Map<String, Object> model);
	
	/**
	 * Return {@link IdmMessage} evaluated template and parameters given in model.
	 * 
	 * @param template
	 * @param model
	 * @return
	 */
	IdmMessage getMessage(IdmNotificationTemplate template, Map<String, Object> model);
	
	/**
	 * Return {@link IdmMessage} by given {@link IdmNotificationTemplate}
	 * 
	 * @param template
	 * @return
	 */
	IdmMessage getMessage(IdmNotificationTemplate template);
	
	/**
	 * Return {@link IdmMessage} by given code.
	 * 
	 * @param code
	 * @return
	 */
	IdmMessage getMessage(String code);
}
