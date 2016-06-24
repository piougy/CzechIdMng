package eu.bcvsolutions.idm.core.security.domain;

import org.springframework.security.core.Authentication;

/**
 * Default authentication with support current and original user name (for login as)
 * 
 * @author svandav
 */
public abstract class AbstractAuthentication implements Authentication {

	private static final long serialVersionUID = 896638566635125212L;
	
	private String currentUsername;
	private String originalUsername;
	
	
	public AbstractAuthentication() {
	}
	
	/**
	 * Creates a new instance
	 * 
	 * @param currentUsername 
	 * @param originalUsername (for login as)
	 */
	public AbstractAuthentication(String currentUsername, String originalUsername) {
		this.originalUsername = originalUsername;
		this.currentUsername = currentUsername;
	}

	@Override
	public String getName() {
		return currentUsername;
	}
	
	@Override
	public Object getPrincipal() {
		return currentUsername;
	}

	public String getCurrentUsername() {
		return currentUsername;
	}
	
	public String getOriginalUsername() {
		return originalUsername;
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
