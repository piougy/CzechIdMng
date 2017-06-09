package eu.bcvsolutions.idm.core.security.dto;

import java.io.Serializable;

import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Base permission dto
 * 
 * @author Radek Tomiška
 *
 */
public class BasePermissionDto implements Serializable {

	private static final long serialVersionUID = -2606172302623199774L;
	private String name;
	private String module;
	
	public BasePermissionDto() {
	}
	
	public BasePermissionDto(BasePermission permission) {
		this(permission.getModule(), permission.getName());
	}
	
	public BasePermissionDto(String module, String name) {
		this.module = module;
		this.name = name;
	}

	/**
	 * Permission name
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Permission is defined in module
	 * 
	 * @return
	 */
	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}
}
