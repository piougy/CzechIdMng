package eu.bcvsolutions.idm.core.notification.service.api;

import java.util.List;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
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

public interface IdmNotificationTemplateService
		extends ReadWriteDtoService<IdmNotificationTemplateDto, NotificationTemplateFilter> {

	static final String PARAMETER_DELIMITIER = ",";

	/**
	 * Return {@link IdmNotificationTemplateDto} by given code.
	 * 
	 * @param code
	 * @return
	 */
	IdmNotificationTemplateDto getTemplateByCode(String code);

	/**
	 * Return {@link IdmMessage}, generate by {@link IdmNotificationTemplate}
	 * from {@link IdmMessage}. For generate new message will be used parameters
	 * given in {@link IdmMessage}. Object instance of {@link GuardedString} is
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
	 * Method load system templates from resources by all classpath defined by
	 * application property, save all new templates into database, all found
	 * templates will be saved as systems.
	 * 
	 */
	void init();

	/**
	 * Method find template for topic and level with help by notification
	 * cofiguration.
	 * 
	 * @param topic
	 * @param level
	 * @return
	 */
	IdmNotificationTemplateDto resolveTemplate(String topic, NotificationLevel level);

	/**
	 * Backup {@link IdmNotificationTemplateDto} to directory given in
	 * parameters, if directory isn't defined (null value) it will be used
	 * operation system temporary directory. Return void or throw errors.
	 * 
	 * @param dtos
	 * @param directory
	 */
	void backup(IdmNotificationTemplateDto dto, String directory);

	/**
	 * Redeploy {@link IdmNotificationTemplateDto}. Redeployed will be only
	 * {@link IdmNotificationTemplateDto}, that has template in resource,
	 * another templates will be skipped (and not returned). Before save newly
	 * loaded notification will be backup the old template into default
	 * temporary directory. Return newly deployed DTO for DTO given in
	 * parameter. Or null if redeploy was failed.
	 * 
	 * @param dto
	 */
	IdmNotificationTemplateDto redeploy(IdmNotificationTemplateDto dto);
}
