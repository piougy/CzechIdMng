package eu.bcvsolutions.idm.security.api.dto;

public class DefaultGrantedAuthorityDto {
	
	private String authority;
	
	public DefaultGrantedAuthorityDto() {
	}
	
	public DefaultGrantedAuthorityDto(String authority) {
		this.authority = authority;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

}
