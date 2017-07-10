package eu.bcvsolutions.idm.core.notification.rest.impl;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
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

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.domain.NotificationGroupPermission;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationTemplateFilter;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.rest.impl.DefaultReadWriteDtoController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Read and write email templates (Apache velocity engine)
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/notification-templates")
@Api(
		value = IdmNotificationTemplateController.TAG, 
		description = "Configure notification templates",
		tags = { IdmNotificationTemplateController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmNotificationTemplateController extends DefaultReadWriteDtoController<IdmNotificationTemplateDto, NotificationTemplateFilter> {
	
	protected static final String TAG = "Notification templates";
	private final IdmNotificationTemplateService notificationTemplateService;
	
	@Autowired
	public IdmNotificationTemplateController(IdmNotificationTemplateService notificationTemplateService) {
		super(notificationTemplateService);
		this.notificationTemplateService = notificationTemplateService;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ + "')")
	@ApiOperation(
			value = "Search notification templates (/search/quick alias)", 
			nickname = "searchNotificationTemplates", 
			tags = { IdmNotificationTemplateController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ + "')")
	@ApiOperation(
			value = "Search notification templates", 
			nickname = "searchQuickNotificationTemplates", 
			tags = { IdmNotificationTemplateController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return this.find(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ + "')")
	@ApiOperation(
			value = "Notification template detail", 
			nickname = "getNotificationTemplate", 
			response = IdmNotificationTemplateDto	.class, 
			tags = { IdmNotificationTemplateController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Template's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_CREATE + "')"
			+ " or hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE + "')")
	@ApiOperation(
			value = "Create / update notification template", 
			nickname = "postNotificationTemplate", 
			response = IdmNotificationTemplateDto.class, 
			tags = { IdmNotificationTemplateController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONTEMPLATE_CREATE, description = ""),
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONTEMPLATE_CREATE, description = ""),
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@Valid @RequestBody @NotNull IdmNotificationTemplateDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE + "')")
	@ApiOperation(
			value = "Update notification template", 
			nickname = "putNotificationTemplate", 
			response = IdmNotificationTemplateDto.class, 
			tags = { IdmNotificationTemplateController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Template's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId,
			@Valid @RequestBody @NotNull IdmNotificationTemplateDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_DELETE + "')")
	@ApiOperation(
			value = "Delete notification template", 
			nickname = "deleteNotificationTemplate", 
			tags = { IdmNotificationTemplateController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONTEMPLATE_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONTEMPLATE_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Template's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@ResponseBody
	@RequestMapping(value = "/{backendId}/redeploy", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE + "')")
	@ApiOperation(
			value = "Redeploy notification template", 
			nickname = "redeployNotificationTemplate", 
			response = IdmNotificationTemplateDto.class, 
			tags = { IdmNotificationTemplateController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE, description = "") })
				},
			notes = "Redeploy template. Redeployed will be only templates, that has pattern in resource."
					+ " Before save newly loaded DO will be backup the old template into backup directory.")
	public ResponseEntity<?> redeploy(
			@ApiParam(value = "Template's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		IdmNotificationTemplateDto template = notificationTemplateService.get(backendId);
		if (template == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, backendId);
		}
		template = notificationTemplateService.redeploy(template);
		return new ResponseEntity<>(toResource(template), HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(value = "/{backendId}/backup", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ + "')")
	@ApiOperation(
			value = "Backup notification template", 
			nickname = "backupNotificationTemplate", 
			response = IdmNotificationTemplateDto.class, 
			tags = { IdmNotificationTemplateController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ, description = "") })
				},
			notes = "Backup template to directory given in application properties.")
	public ResponseEntity<?> backup(
			@ApiParam(value = "Template's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		IdmNotificationTemplateDto template = notificationTemplateService.get(backendId);
		if (template == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, backendId);
		}
		notificationTemplateService.backup(template);
		return new ResponseEntity<>(toResource(template), HttpStatus.OK);
	}
}
