	package eu.bcvsolutions.idm.core.notification.rest.impl;

	import java.util.Collections;
	import java.util.List;
	import java.util.Set;

	import javax.servlet.http.HttpServletRequest;
	import javax.validation.Valid;
	import javax.validation.constraints.NotNull;

	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.data.domain.Pageable;
	import org.springframework.data.web.PageableDefault;
	import org.springframework.hateoas.Resources;
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

	import com.google.common.collect.Lists;
	import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
	import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
	import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
	import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
	import eu.bcvsolutions.idm.core.notification.domain.NotificationGroupPermission;
	import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationConfigurationService;

/**
 * Configuration for notification routing
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/notification-configurations")
public class IdmNotificationConfigurationController extends AbstractReadWriteDtoController<NotificationConfigurationDto, EmptyFilter> {

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
	public Resources<?> find(@RequestParam MultiValueMap<String, Object> parameters, @PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONCONFIGURATION_READ + "')")
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	public Resources<?> findQuick(@RequestParam MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONCONFIGURATION_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	public ResponseEntity<?> get(@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONCONFIGURATION_CREATE + "') or hasAuthority('" + NotificationGroupPermission.NOTIFICATIONCONFIGURATION_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> post(@Valid @RequestBody @NotNull NotificationConfigurationDto dto) throws HttpMessageNotReadableException {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONCONFIGURATION_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	public ResponseEntity<?> put(
			@PathVariable @NotNull String backendId,
			@Valid @RequestBody @NotNull NotificationConfigurationDto dto) throws HttpMessageNotReadableException {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONCONFIGURATION_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONCONFIGURATION_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	/**
	 * Returns registered senders notification types
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/all/notification-types", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONCONFIGURATION_READ + "')")
	public List<String> getSupportedNotificationTypes() {
		Set<String> types = configurationService.getSupportedNotificationTypes();
		List<String> results = Lists.newArrayList(types);
		Collections.sort(results);
		return results;
	}
}
