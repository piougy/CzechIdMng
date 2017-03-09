package eu.bcvsolutions.idm.core.security.api.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;

import eu.bcvsolutions.idm.core.api.dto.IdentityDto;

/**
 * Identity JWT authentication
 * 
 * @author svandav
 */
public class IdmJwtAuthentication extends AbstractAuthentication {

	private static final long serialVersionUID = -63165487654324844L;

	private Date expiration;
	private Collection<GrantedAuthority> authorities;
	private String fromModule;
	
	public IdmJwtAuthentication(IdentityDto currentIdentity, Date expiration,
			Collection<GrantedAuthority> authorities, String fromModule) {
		this(currentIdentity, currentIdentity, expiration, authorities, fromModule);
	}

	public IdmJwtAuthentication(IdentityDto currentIdentity, IdentityDto originalIdentity, Date expiration,
			Collection<GrantedAuthority> authorities, String fromModule) {
		super(currentIdentity, originalIdentity);
		//
		this.fromModule = fromModule;
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
	
	public boolean isExpired() {
		if (expiration == null) {
			return false;
		}
		return expiration.before(new Date());
	}

	public String getFromModule() {
		return fromModule;
	}

	public void setFromModule(String fromModule) {
		this.fromModule = fromModule;
	}
}
