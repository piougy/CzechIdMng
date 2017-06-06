package eu.bcvsolutions.idm.core.notification.rest.impl;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationState;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.domain.NotificationGroupPermission;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationConfigurationService;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.notification.service.api.NotificationManager;

/**
 * Read and send notifications
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/notifications")
public class IdmNotificationLogController
		extends AbstractReadWriteDtoController<IdmNotificationLogDto, NotificationFilter> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdmNotificationLogController.class);
	private final NotificationManager notificationManager;
	private final IdmNotificationLogService notificationLogService;
	private final IdmNotificationConfigurationService notificationConfigurationService;

	@Autowired
	public IdmNotificationLogController(
			IdmNotificationLogService notificationLogService, 
			NotificationManager notificationManager,
			IdmNotificationConfigurationService notificationConfigurationService) {
		super(notificationLogService);
		//
		Assert.notNull(notificationManager);
		Assert.notNull(notificationConfigurationService);
		//
		this.notificationManager = notificationManager;
		this.notificationLogService = notificationLogService;
		this.notificationConfigurationService = notificationConfigurationService;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	public Resources<?> find(@RequestParam MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	public Resources<?> findQuick(@RequestParam MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	public ResponseEntity<?> get(@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	/**
	 * Send notification
	 * 
	 * @param dto
	 */
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_CREATE + "')"
			+ " or hasAuthority('" + NotificationGroupPermission.NOTIFICATION_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	public IdmNotificationLogDto postDto(@RequestBody @NotNull IdmNotificationLogDto dto) {
		LOG.debug("Notification log [{}] was created and notification will be send.", dto);
		final IdmNotificationLogDto result = notificationManager.send(dto);
		// TODO: send method should result notification or ex to prevent another loading
		return getDto(result.getId());
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	@RequestMapping(value = "/{backendId}/recipients", method = RequestMethod.GET)
	public List<IdmNotificationRecipientDto> getRecipients(@PathVariable @NotNull String backendId) {
		return notificationLogService.getRecipientsForNotification(backendId);
	}
	
	@Override
	protected NotificationFilter toFilter(MultiValueMap<String, Object> parameters) {
		NotificationFilter filter = new NotificationFilter();
		filter.setText(getParameterConverter().toString(parameters, "text"));
		filter.setSender(getParameterConverter().toString(parameters, "sender"));
		filter.setRecipient(getParameterConverter().toString(parameters, "recipient"));
		filter.setState(getParameterConverter().toEnum(parameters, "state", NotificationState.class));
		filter.setFrom(getParameterConverter().toDateTime(parameters, "from"));
		filter.setTill(getParameterConverter().toDateTime(parameters, "till"));
		filter.setSent(getParameterConverter().toBoolean(parameters, "sent"));
		filter.setNotificationType(notificationConfigurationService.toSenderType(getParameterConverter().toString(parameters, "notificationType")));
		filter.setParent(getParameterConverter().toUuid(parameters, "parent"));
		return filter;
	}
}
