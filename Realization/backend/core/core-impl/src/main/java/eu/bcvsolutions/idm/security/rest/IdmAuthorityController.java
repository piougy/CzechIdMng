package eu.bcvsolutions.idm.security.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.security.api.service.SecurityService;
import eu.bcvsolutions.idm.security.dto.GroupPermissionDto;

/**
 * Provides autorities configurable for idm roles
 * 
 * @author Radek Tomiška 
 *
 */
@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/authorities/")
public class IdmAuthorityController {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdmAuthorityController.class);
	
	@Autowired
	private SecurityService securityService;
	
	/**
	 * Provides authorities configurable for idm roles
	 * 
	 * @return
	 */
	@RequestMapping(path = "/available", method = RequestMethod.GET)
	public List<GroupPermissionDto> getAvailableAuthorities() {
		log.debug("Loading all available authorities");
		Map<String, Set<String>> distinctAuthorities = new HashMap<>();
		List<GroupPermission> groupPermissions = securityService.getAvailableGroupPermissions();		
		groupPermissions.forEach(groupPermission -> {
			if (!distinctAuthorities.containsKey(groupPermission.getName())) {
				distinctAuthorities.put(groupPermission.getName(), new LinkedHashSet<>());
			}
			groupPermission.getPermissions().forEach(basePermission -> {
				distinctAuthorities.get(groupPermission.getName()).add(basePermission.getName());
			});
		});
		List<GroupPermissionDto> availablePermissions = new ArrayList<>();
		distinctAuthorities.forEach((k,v) -> {
			List<String> permissions = new ArrayList<String>(v);
			Collections.sort(permissions);
			availablePermissions.add(new GroupPermissionDto(k, permissions));
		});
		log.debug("Loaded all available authorities [groups:{}]", distinctAuthorities.size());
		return availablePermissions;
	}

}
