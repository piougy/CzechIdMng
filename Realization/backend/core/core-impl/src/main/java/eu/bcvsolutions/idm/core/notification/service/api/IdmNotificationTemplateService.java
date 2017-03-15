package eu.bcvsolutions.idm.core.notification.service.api;

import java.util.List;

import eu.bcvsolutions.idm.core.api.service.IdentifiableByNameEntityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationTemplateFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationTemplate;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Interface with basic method for apache velocity templates engine
 * Initialization velocity engine in constructor.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmNotificationTemplateService extends ReadWriteEntityService<IdmNotificationTemplate, NotificationTemplateFilter>, IdentifiableByNameEntityService<IdmNotificationTemplate> {
	
	static final String PARAMETER_DELIMITIER = ",";
	
	/**
	 * Return {@link IdmNotificationTemplate} by given code.
	 * 
	 * @param code
	 * @return
	 */
	IdmNotificationTemplate getTemplateByCode(String code);

	/**
	 * Return {@link IdmMessage}, generate by {@link IdmNotificationTemplate} from {@link IdmMessage}.
	 * For generate new message will be used parameters given in {@link IdmMessage}.
	 * Object instance of {@link GuardedString} is show or hide by parameter showGuardedString
	 * 
	 * @param message
	 * @param showGuardedString
	 * @return
	 */
	IdmMessage buildMessage(IdmMessage message, boolean showGuardedString);
	
	/**
	 * Return {@link IdmMessage} generate by {@link IdmNotificationTemplate} from {@link IdmMessage}.
	 * Object instance of {@link GuardedString} will be replace by asterix.
	 * For generate new message will be used parameters given in {@link IdmMessage}.
	 * 
	 * @param template
	 * @return
	 */
	IdmMessage buildMessage(IdmMessage message);
	
	/**
	 * Method find all system templates.
	 * 
	 * @return
	 */
	List<IdmNotificationTemplate> findAllSystemTemplates();
	
	/**
	 * Method load system templates from resources by all classpath defined by application property,
	 * save all new templates into database, all found templates will be saved as systems.
	 * 
	 */
	void initSystemTemplates();
	
	/**
	 * Method find template for topic and level with help by notification cofiguration.
	 * 
	 * @param topic
	 * @param level
	 * @return
	 */
	IdmNotificationTemplate resolveTemplate(String topic, NotificationLevel level);
}
