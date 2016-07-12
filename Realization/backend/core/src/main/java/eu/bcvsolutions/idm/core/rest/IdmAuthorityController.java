package eu.bcvsolutions.idm.core.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.model.domain.CustomGroupPermission;
import eu.bcvsolutions.idm.core.model.domain.GroupPermission;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.GroupPermissionDto;

@RestController
@RequestMapping(value = "/api/authorities/")
public class IdmAuthorityController {
	
	@RequestMapping(path = "/available", method = RequestMethod.GET)
	public List<GroupPermissionDto> availableGroupPermissions() {
		Map<String, Set<String>> distinctPermissions = new HashMap<>();
		//
		// TODO: SPI / osgi for register module permissions
		// TODO: move to service layer
		List<GroupPermission> groupPermissions = new ArrayList<>();
		groupPermissions.addAll(Arrays.asList(IdmGroupPermission.values()));
		groupPermissions.addAll(Arrays.asList(CustomGroupPermission.values()));
		
		groupPermissions.forEach(groupPermission -> {
			if (!distinctPermissions.containsKey(groupPermission.getName())) {
				distinctPermissions.put(groupPermission.getName(), new LinkedHashSet<>());
			}
			groupPermission.getPermissions().forEach(basePermission -> {
				distinctPermissions.get(groupPermission.getName()).add(basePermission.getName());
			});
		});
		List<GroupPermissionDto> availablePermissions = new ArrayList<>();
		distinctPermissions.forEach((k,v) -> {
			List<String> permissions = new ArrayList<String>(v);
			Collections.sort(permissions);
			availablePermissions.add(new GroupPermissionDto(k, permissions));
		});
		return availablePermissions;
	}

}
