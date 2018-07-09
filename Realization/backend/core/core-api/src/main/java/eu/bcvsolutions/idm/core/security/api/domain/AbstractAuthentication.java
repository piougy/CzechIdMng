package eu.bcvsolutions.idm.core.security.api.domain;

import java.util.UUID;

import org.springframework.security.core.Authentication;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;

/**
 * Default authentication with support current and original user name (for login as)
 * 
 * @author svandav
 */
public abstract class AbstractAuthentication implements Authentication {

	private static final long serialVersionUID = 896638566635125212L;
	//
	private UUID id; // authentication id - e.g. token id
	private final IdmIdentityDto currentIdentity;
	private final IdmIdentityDto originalIdentity;
	
	/**
	 * Creates a new instance
	 * 
	 * @param currentIdentity 
	 * @param originalIdentity (for login as)
	 */
	public AbstractAuthentication(IdmIdentityDto currentIdentity, IdmIdentityDto originalIdentity) {
		this(null, currentIdentity, originalIdentity);
	}
	
	/**
	 * Creates a new instance
	 * 
	 * @param currentIdentity 
	 * @param originalIdentity (for login as)
	 * @since 8.2.0
	 */
	public AbstractAuthentication(UUID id, IdmIdentityDto currentIdentity, IdmIdentityDto originalIdentity) {
		this.id = id;
		this.currentIdentity = currentIdentity;
		this.originalIdentity = originalIdentity;
	}

	@Override
	public String getName() {
		return getCurrentUsername();
	}
	
	@Override
	public Object getPrincipal() {
		return getName();
	}
	
	public IdmIdentityDto getCurrentIdentity() {
		return currentIdentity;
	}

	public String getCurrentUsername() {
		return currentIdentity == null ? null : currentIdentity.getUsername();
	}
	
	public IdmIdentityDto getOriginalIdentity() {
		return originalIdentity;
	}
	
	public String getOriginalUsername() {
		return originalIdentity == null ? null : originalIdentity.getUsername();
	}

	@Override
	public Object getCredentials() {
		return "";
	}

	@Override
	public Object getDetails() {
		return null;
	}

	@Override
	public boolean isAuthenticated() {
		return true;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		throw new IllegalArgumentException("setAuthenticated is not supported");
	}
	
	/**
	 * Authentication id - e.g. token id
	 * 
	 * @return
	 * @since 8.2.0
	 */
	public UUID getId() {
		return id;
	}
	
	/**
	 * Authentication id - e.g. token id
	 * 
	 * @param id
	 * @since 8.2.0
	 */
	public void setId(UUID id) {
		this.id = id;
	}	
}
