package eu.bcvsolutions.idm.core.notification.api.service;

import java.util.List;

import eu.bcvsolutions.idm.core.api.service.CodeableService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.Recoverable;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationTemplateFilter;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Interface with basic method for apache velocity templates engine
 * Initialization velocity engine in constructor.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmNotificationTemplateService extends 
ReadWriteDtoService<IdmNotificationTemplateDto, IdmNotificationTemplateFilter>,
		Recoverable<IdmNotificationTemplateDto>,
		CodeableService<IdmNotificationTemplateDto> {

	static final String PARAMETER_DELIMITIER = ",";

	/**
	 * Return {@link IdmNotificationTemplateDto} by given code.
	 * 
	 * @param code
	 * @return
	 */
	IdmNotificationTemplateDto getTemplateByCode(String code);

	/**
	 * Return {@link IdmMessageDto}, generate by {@link IdmNotificationTemplateDto}
	 * from {@link IdmMessageDto}. For generate new message will be used parameters
	 * given in {@link IdmMessageDto}. Object instance of {@link GuardedString} is
	 * show or hide by parameter showGuardedString
	 * 
	 * @param message
	 * @param showGuardedString
	 * @return
	 */
	IdmMessageDto buildMessage(IdmMessageDto message, boolean showGuardedString);

	/**
	 * Return {@link IdmMessageDto} generate by
	 * {@link IdmNotificationTemplateDto} from {@link IdmMessageDto}. Object
	 * instance of {@link GuardedString} will be replace by asterix. For
	 * generate new message will be used parameters given in
	 * {@link IdmMessageDto}.
	 * 
	 * @param message
	 * @return
	 */
	IdmMessageDto buildMessage(IdmMessageDto message);

	/**
	 * Method find all system templates.
	 * 
	 * @return
	 */
	List<IdmNotificationTemplateDto> findAllSystemTemplates();

	/**
	 * Method find template for topic and level with help by notification
	 * cofiguration.
	 * 
	 * @param topic
	 * @param level
	 * @param notificationType
	 * @return
	 */
	IdmNotificationTemplateDto resolveTemplate(String topic, NotificationLevel level, String notificationType);
}
