package eu.bcvsolutions.idm.security.api.domain;

import org.springframework.security.core.Authentication;

import eu.bcvsolutions.idm.core.api.dto.IdentityDto;

/**
 * Default authentication with support current and original user name (for login as)
 * 
 * @author svandav
 */
public abstract class AbstractAuthentication implements Authentication {

	private static final long serialVersionUID = 896638566635125212L;
	
	private final IdentityDto currentIdentity;
	private final IdentityDto originalIdentity;
	
	/**
	 * Creates a new instance
	 * 
	 * @param currentIdentity 
	 * @param originalIdentity (for login as)
	 */
	public AbstractAuthentication(IdentityDto currentIdentity, IdentityDto originalIdentity) {
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
	
	public IdentityDto getCurrentIdentity() {
		return currentIdentity;
	}

	public String getCurrentUsername() {
		return currentIdentity == null ? null : currentIdentity.getUsername();
	}
	
	public IdentityDto getOriginalIdentity() {
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
	
}
