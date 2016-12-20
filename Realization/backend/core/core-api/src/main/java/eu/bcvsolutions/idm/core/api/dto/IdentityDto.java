package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.domain.Auditable;

/**
 * Dto for identity
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityDto extends AbstractDto {

	private static final long serialVersionUID = -8904175841258184550L;
	private String username;
	
	// TODO: add all identity properties
	
	public IdentityDto() {
	}
	
	public IdentityDto(String username) {
		this.username = username;
	}
	
	public IdentityDto(UUID id, String username) {
		super(id);
		this.username = username;
	}
	
	public IdentityDto(Auditable auditable, String username) {
		super(auditable);
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
}
