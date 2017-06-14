package eu.bcvsolutions.idm.core.notification.rest.impl;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.domain.NotificationGroupPermission;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationTemplateFilter;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.rest.impl.DefaultReadWriteDtoController;

/**
 * Read and write email templates (Apache velocity engine)
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/notification-templates")
public class IdmNotificationTemplateController extends DefaultReadWriteDtoController<IdmNotificationTemplateDto, NotificationTemplateFilter> {
	
	private final IdmNotificationTemplateService notificationTemplateService;
	
	@Autowired
	public IdmNotificationTemplateController(IdmNotificationTemplateService notificationTemplateService) {
		super(notificationTemplateService);
		this.notificationTemplateService = notificationTemplateService;
	}
	

	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ + "')")
	public Resources<?> findQuick(@RequestParam MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return this.find(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ + "')")
	public ResponseEntity<?> get(@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_CREATE + "') or hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE + "')")
	public ResponseEntity<?> post(@Valid @RequestBody @NotNull IdmNotificationTemplateDto dto)
			throws HttpMessageNotReadableException {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE + "')")
	public ResponseEntity<?> put(@PathVariable @NotNull String backendId,
								 @Valid @RequestBody @NotNull IdmNotificationTemplateDto dto) throws HttpMessageNotReadableException {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_DELETE + "')")
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@ResponseBody
	@RequestMapping(value = "/{backendId}/redeploy", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ + "')")
	public ResponseEntity<?> redeployEntity(@PathVariable @NotNull String backendId) {
		IdmNotificationTemplateDto template = notificationTemplateService.get(backendId);
		if (template == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, backendId);
		}
		template = notificationTemplateService.redeployDto(template);
		return new ResponseEntity<>(toResource(template), HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(value = "/{backendId}/backup", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ + "')")
	public ResponseEntity<?> backupEntity(@PathVariable @NotNull String backendId) {
		IdmNotificationTemplateDto template = notificationTemplateService.get(backendId);
		if (template == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, backendId);
		}
		notificationTemplateService.backupDto(template, null);
		return new ResponseEntity<>(toResource(template), HttpStatus.OK);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE + "')")
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId,
								   HttpServletRequest nativeRequest) throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}
}
