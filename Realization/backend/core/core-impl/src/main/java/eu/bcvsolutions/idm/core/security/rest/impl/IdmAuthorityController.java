package eu.bcvsolutions.idm.core.security.rest.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.dto.BasePermissionDto;
import eu.bcvsolutions.idm.core.security.dto.GroupPermissionDto;

/**
 * Provides autorities configurable for idm roles
 * 
 * @author Radek Tomiška 
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/authorities/")
public class IdmAuthorityController {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdmAuthorityController.class);
	
	@Autowired
	private SecurityService securityService;
	
	/**
	 * Provides authorities configurable for idm roles
	 * 
	 * @return
	 */
	@RequestMapping(path = "/search/available", method = RequestMethod.GET)
	public List<GroupPermissionDto> getAvailableAuthorities() {
		log.debug("Loading all available authorities");
		Map<String, GroupPermissionDto> distinctAuthorities = new HashMap<>();
		List<GroupPermission> groupPermissions = securityService.getAvailableGroupPermissions();		
		groupPermissions.forEach(groupPermission -> {
			if (!distinctAuthorities.containsKey(groupPermission.getName())) {
				distinctAuthorities.put(groupPermission.getName(), new GroupPermissionDto(groupPermission));
			}
			groupPermission.getPermissions().forEach(basePermission -> {
				distinctAuthorities.get(groupPermission.getName()).getPermissions().add(new BasePermissionDto(basePermission));
			});
		});
		log.debug("Loaded all available authorities [groups:{}]", distinctAuthorities.size());
		return new ArrayList<>(distinctAuthorities.values());
	}

}
