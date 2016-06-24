package eu.bcvsolutions.idm.core.security.dto;

public class DefaultGrantedAuthorityDto {
	
	private String roleName;
	private String authority;
	
	public DefaultGrantedAuthorityDto() {
	}
	
	public DefaultGrantedAuthorityDto(String roleName, String authority){
		this.roleName = roleName;
		this.authority = authority;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

}
