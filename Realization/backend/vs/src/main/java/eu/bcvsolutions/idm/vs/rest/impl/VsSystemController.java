package eu.bcvsolutions.idm.vs.rest.impl;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.vs.service.api.VsSystemService;
import eu.bcvsolutions.idm.vs.service.api.dto.VsSystemDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Rest methods for create virtual system (only for create for now)
 * 
 * @author Svanda
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/vs/systems")
@Api(
		value = VsSystemController.TAG, 
		tags = { VsSystemController.TAG }, 
		description = "Operations with virtual system (only for create now)", 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE, 
		consumes = MediaType.APPLICATION_JSON_VALUE) 
public class VsSystemController {

	private final VsSystemService service;
	protected static final String TAG = "Systems";

	@Autowired
	public VsSystemController(VsSystemService service) {
		Assert.notNull(service);
		this.service = service;
	}

	@ResponseBody
	@RequestMapping(value = "/", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_CREATE + "')")
	@ApiOperation(value = "Create new virtual system", nickname = "createVsSystem", response = SysSystemDto.class, tags = {
			VsSystemController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_CREATE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_CREATE, description = "") }) })
	public ResponseEntity<?> create(@RequestBody @NotNull VsSystemDto dto) {
		SysSystemDto system = this.service.create(dto);
		return new ResponseEntity<>(system, HttpStatus.OK);
	}
}
