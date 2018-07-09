package eu.bcvsolutions.idm.core.security.api.dto;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;

/**
 * Granted authority = GROUP_BASE permission
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultGrantedAuthorityDto implements BaseDto {
	
	private static final long serialVersionUID = 1L;
	//
	private String authority;
	
	public DefaultGrantedAuthorityDto() {
	}
	
	public DefaultGrantedAuthorityDto(String authority) {
		this.authority = authority;
	}
	
	@Override
	public String getId() {
		return authority;
	}

	@Override
	public void setId(Serializable id) {
		this.authority = id == null ? null : id.toString();
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				 .append(authority)
				 .toHashCode();
	}
	
	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof DefaultGrantedAuthorityDto)) {
			return false;
		}
		DefaultGrantedAuthorityDto that = (DefaultGrantedAuthorityDto) o;
		
		EqualsBuilder builder = new EqualsBuilder();
	
		return builder
				.append(authority, that.authority)
				.isEquals();
	}
}
