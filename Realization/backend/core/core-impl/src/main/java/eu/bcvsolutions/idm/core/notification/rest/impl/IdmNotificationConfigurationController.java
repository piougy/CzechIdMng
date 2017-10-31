package eu.bcvsolutions.idm.core.notification.rest.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationConfigurationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationConfigurationService;
import eu.bcvsolutions.idm.core.notification.domain.NotificationGroupPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Configuration for notification routing
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/notification-configurations")
@Api(
		value = IdmNotificationConfigurationController.TAG, 
		description = "Configure message sending",
		tags = { IdmNotificationConfigurationController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmNotificationConfigurationController extends AbstractReadWriteDtoController<NotificationConfigurationDto, IdmNotificationConfigurationFilter> {

	protected static final String TAG = "Notification configuration";
	private final IdmNotificationConfigurationService configurationService;
	
	@Autowired
	public IdmNotificationConfigurationController(IdmNotificationConfigurationService configurationService) {
		super(configurationService);
		this.configurationService = configurationService;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONCONFIGURATION_READ + "')")
	@ApiOperation(
			value = "Search notification configuration items (/search/quick alias)", 
			nickname = "searchNotificationConfigurations", 
			tags = { IdmNotificationConfigurationController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONCONFIGURATION_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONCONFIGURATION_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONCONFIGURATION_READ + "')")
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search notification configuration items", 
			nickname = "searchQuickNotificationConfigurations", 
			tags = { IdmNotificationConfigurationController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONCONFIGURATION_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONCONFIGURATION_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONCONFIGURATION_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@ApiOperation(
			value = "Notification configuration item detail", 
			nickname = "getNotificationConfiguration", 
			response = NotificationConfigurationDto	.class, 
			tags = { IdmNotificationConfigurationController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONCONFIGURATION_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONCONFIGURATION_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Item's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONCONFIGURATION_CREATE + "')"
			+ " or hasAuthority('" + NotificationGroupPermission.NOTIFICATIONCONFIGURATION_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@ApiOperation(
			value = "Create / update notification configuration item", 
			nickname = "postNotificationConfiguration", 
			response = NotificationConfigurationDto.class, 
			tags = { IdmNotificationConfigurationController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONCONFIGURATION_CREATE, description = ""),
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONCONFIGURATION_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONCONFIGURATION_CREATE, description = ""),
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONCONFIGURATION_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@Valid @RequestBody @NotNull NotificationConfigurationDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONCONFIGURATION_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Update notification configuration item", 
			nickname = "putNotificationConfiguration", 
			response = NotificationConfigurationDto.class, 
			tags = { IdmNotificationConfigurationController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONCONFIGURATION_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONCONFIGURATION_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Item's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody @NotNull NotificationConfigurationDto dto){
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONCONFIGURATION_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@ApiOperation(
			value = "Delete notification configuration item", 
			nickname = "deleteNotificationConfiguration", 
			tags = { IdmNotificationConfigurationController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONCONFIGURATION_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONCONFIGURATION_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Item's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	/**
	 * Returns registered senders notification types
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/all/notification-types", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONCONFIGURATION_READ + "')"
			+ " or hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	@ApiOperation(
			value = "Supported notification (sender) type", 
			nickname = "getSupportedNotificationTypes",
			tags = { IdmNotificationConfigurationController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONCONFIGURATION_READ, description = ""),
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATION_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONCONFIGURATION_READ, description = ""),
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATION_READ, description = "")})
				},
			notes = "Returns registered senders notification types.")
	public List<String> getSupportedNotificationTypes() {
		Set<String> types = configurationService.getSupportedNotificationTypes();
		List<String> results = Lists.newArrayList(types);
		Collections.sort(results);
		return results;
	}
}
