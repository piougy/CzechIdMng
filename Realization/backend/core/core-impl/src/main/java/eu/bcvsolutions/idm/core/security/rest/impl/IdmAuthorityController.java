package eu.bcvsolutions.idm.core.security.rest.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.dto.BasePermissionDto;
import eu.bcvsolutions.idm.core.security.dto.GroupPermissionDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

/**
 * Provides autorities configurable for idm roles
 * 
 * @author Radek Tomiška 
 *
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/authorities")
@Api(value = IdmAuthorityController.TAG, description = "Role authorities", tags = { IdmAuthorityController.TAG })
public class IdmAuthorityController implements BaseController {
	
	protected static final String TAG = "Authorities";	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdmAuthorityController.class);
	//
	@Autowired private ModuleService moduleService;
	
	/**
	 * Provides configurable authorities for roles
	 * 
	 * @return
	 */
	@ApiOperation(
			value = "Provides configurable authorities for roles", 
			nickname = "authorities", 
			tags={ IdmAuthorityController.TAG }, 
			authorizations = {
				@Authorization(SwaggerConfig.AUTHENTICATION_BASIC),
				@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST)
			})
	@RequestMapping(path = "/search/available", method = RequestMethod.GET)
	public List<GroupPermissionDto> getAvailableAuthorities() {
		LOG.debug("Loading all available authorities");
		Map<String, GroupPermissionDto> distinctAuthorities = new HashMap<>();
		List<GroupPermission> groupPermissions = moduleService.getAvailablePermissions();		
		groupPermissions.forEach(groupPermission -> {
			if (!distinctAuthorities.containsKey(groupPermission.getName())) {
				distinctAuthorities.put(groupPermission.getName(), new GroupPermissionDto(groupPermission));
			}
			groupPermission.getPermissions().forEach(basePermission -> {
				distinctAuthorities.get(groupPermission.getName()).getPermissions().add(new BasePermissionDto(basePermission));
			});
		});
		LOG.debug("Loaded all available authorities [groups:{}]", distinctAuthorities.size());
		return new ArrayList<>(distinctAuthorities.values());
	}

}
