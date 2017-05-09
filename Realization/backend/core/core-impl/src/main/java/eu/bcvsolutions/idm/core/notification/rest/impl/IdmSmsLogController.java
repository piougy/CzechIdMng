	package eu.bcvsolutions.idm.core.notification.rest.impl;

	import javax.validation.constraints.NotNull;

	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.data.domain.Pageable;
	import org.springframework.data.web.PageableDefault;
	import org.springframework.hateoas.Resources;
	import org.springframework.http.ResponseEntity;
	import org.springframework.security.access.prepost.PreAuthorize;
	import org.springframework.util.MultiValueMap;
	import org.springframework.web.bind.annotation.PathVariable;
	import org.springframework.web.bind.annotation.RequestMapping;
	import org.springframework.web.bind.annotation.RequestMethod;
	import org.springframework.web.bind.annotation.RequestParam;
	import org.springframework.web.bind.annotation.ResponseBody;
	import org.springframework.web.bind.annotation.RestController;

	import eu.bcvsolutions.idm.core.api.rest.AbstractReadDtoController;
	import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
	import eu.bcvsolutions.idm.core.notification.api.dto.IdmSmsLogDto;
	import eu.bcvsolutions.idm.core.notification.domain.NotificationGroupPermission;
	import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationFilter;
	import eu.bcvsolutions.idm.core.notification.service.api.IdmSmsLogService;

	/**
	 * Read sms logs
	 *
	 * @author Peter Sourek
	 *
	 */
	@RestController
	@RequestMapping(value = BaseDtoController.BASE_PATH + "/notification-sms")
	public class IdmSmsLogController extends AbstractReadDtoController<IdmSmsLogDto, NotificationFilter> {

		@Autowired
		public IdmSmsLogController(IdmSmsLogService entityLookupService) {
			super(entityLookupService);
		}

		@Override
		@ResponseBody
		@RequestMapping(method = RequestMethod.GET)
		@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
		public Resources<?> find(@RequestParam MultiValueMap<String, Object> parameters, @PageableDefault Pageable pageable) {
			return super.find(parameters, pageable);
		}

		@ResponseBody
		@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
		@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
		public Resources<?> findQuick(@RequestParam MultiValueMap<String, Object> parameters, @PageableDefault Pageable pageable) {
			return super.find(parameters, pageable);
		}

		@Override
		@ResponseBody
		@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
		@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
		public ResponseEntity<?> get(@PathVariable @NotNull String backendId) {
			return super.get(backendId);
		}
	}
