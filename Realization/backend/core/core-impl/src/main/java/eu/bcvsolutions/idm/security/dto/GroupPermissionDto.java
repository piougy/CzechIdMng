package eu.bcvsolutions.idm.security.dto;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.security.api.domain.BasePermission;

/**
 * Group permission representation
 * 
 * @author Radek Tomiška 
 *
 */
public class GroupPermissionDto extends BasePermissionDto {

	private static final long serialVersionUID = -7680244766930626618L;
	
	private List<BasePermissionDto> permissions;
	
	public GroupPermissionDto() {
	}
	
	public GroupPermissionDto(BasePermission permission) {
		super(permission);
	}
	
	public GroupPermissionDto(BasePermission permission, List<BasePermissionDto> permissions) {
		this(permission);
		this.permissions = permissions;
	}
	
	public GroupPermissionDto(String module, String name, List<BasePermissionDto> permissions) {
		super(module, name);
		this.permissions = permissions;
	}

	public List<BasePermissionDto> getPermissions() {
		if (permissions == null) {
			permissions = new ArrayList<>();
		}
		return permissions;
	}
	
	public void setPermissions(List<BasePermissionDto> permissions) {
		this.permissions = permissions;
	}
}
