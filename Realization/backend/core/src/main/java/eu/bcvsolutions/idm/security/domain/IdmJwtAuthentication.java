package eu.bcvsolutions.idm.security.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;

/**
 * Identity JWT authentication
 * 
 * @author svandav
 */
public class IdmJwtAuthentication extends AbstractAuthentication {

	private static final long serialVersionUID = -63165487654324844L;

	private Date expiration;
	private Collection<DefaultGrantedAuthority> authorities;

	
	public IdmJwtAuthentication(String username, Date expiration,
			Collection<DefaultGrantedAuthority> authorities) {
		this(username, username, expiration, authorities);
	}

	public IdmJwtAuthentication(String currentUsername, String originalUsername, Date expiration,
			Collection<DefaultGrantedAuthority> authorities) {
		super(currentUsername, originalUsername);

		this.expiration = expiration;
		this.authorities = Collections.unmodifiableList(new ArrayList<>(authorities));
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}
	
	public Date getExpiration() {
		return expiration;
	}

}
