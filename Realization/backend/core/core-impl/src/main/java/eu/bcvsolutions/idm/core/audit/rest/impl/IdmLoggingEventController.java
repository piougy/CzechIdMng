package eu.bcvsolutions.idm.core.audit.rest.impl;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.IdmLoggingEventDto;
import eu.bcvsolutions.idm.core.api.dto.filter.LoggingEventFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.audit.model.entity.IdmLoggingEvent;
import eu.bcvsolutions.idm.core.audit.service.api.IdmLoggingEventService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Controller for {@link IdmLoggingEvent} entity and their DTO.
 * This controller use same permissions as {@link IdmAuditController}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RepositoryRestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/logging-events")
@Api(
		value = IdmLoggingEventController.TAG, 
		description = "Read / search log from LOG4J", 
		tags = { IdmLoggingEventController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmLoggingEventController extends AbstractReadDtoController<IdmLoggingEventDto, LoggingEventFilter> {

	protected static final String TAG = "Logging events";
	
	@Autowired
	public IdmLoggingEventController(IdmLoggingEventService service) {
		super(service);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUDIT_READ + "')")
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search logging events", 
			nickname = "searchQuickLoggingEvents", 
			tags = { IdmLoggingEventController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return this.find(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/{backendId}")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUDIT_READ + "')")
	@Override
	@ApiOperation(
			value = "Logging event detail", 
			nickname = "getLoggingEvent", 
			response = IdmLoggingEventDto.class, 
			tags = { IdmLoggingEventController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Logging event's identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
}
