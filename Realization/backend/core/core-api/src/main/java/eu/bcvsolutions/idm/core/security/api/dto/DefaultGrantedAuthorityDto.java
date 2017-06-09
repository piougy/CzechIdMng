package eu.bcvsolutions.idm.core.security.api.dto;

import com.google.common.base.Objects;

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

	@Override
	public int hashCode() {
		return Objects.hashCode(getAuthority());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj || getClass() != obj.getClass()) {
			return false;
		}
		return Objects.equal(getAuthority(), ((DefaultGrantedAuthorityDto) obj).getAuthority());
	}
	
	

}
