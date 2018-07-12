package eu.bcvsolutions.idm.core.security.api.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.security.core.GrantedAuthority;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;

/**
 * Identity JWT authentication
 * 
 * @author svandav
 */
public class IdmJwtAuthentication extends AbstractAuthentication {

	private static final long serialVersionUID = -63165487654324844L;
	//
	public static final String TOKEN_TYPE = "authentication";
	//
	private DateTime issuedAt; // issued at
	private DateTime expiration; // expiration
	private Collection<GrantedAuthority> authorities;
	private String fromModule;
	
	public IdmJwtAuthentication(
			IdmIdentityDto currentIdentity,
			DateTime expiration,
			Collection<GrantedAuthority> authorities,
			String fromModule) {
		this(currentIdentity, currentIdentity, expiration, DateTime.now(), authorities, fromModule);
	}

	public IdmJwtAuthentication(
			IdmIdentityDto currentIdentity, 
			IdmIdentityDto originalIdentity, 
			DateTime expiration,
			DateTime issuedAt,
			Collection<GrantedAuthority> authorities,
			String fromModule) {
		this(null, currentIdentity, currentIdentity, expiration, DateTime.now(), authorities, fromModule);
	}
	
	public IdmJwtAuthentication(
			UUID tokenId,
			IdmIdentityDto currentIdentity, 
			IdmIdentityDto originalIdentity, 
			DateTime expiration,
			DateTime issuedAt,
			Collection<GrantedAuthority> authorities,
			String fromModule) {
		super(tokenId, currentIdentity, originalIdentity);
		//
		this.fromModule = fromModule;
		this.issuedAt = issuedAt;
		this.expiration = expiration;
		if (authorities == null) {
			this.authorities = new ArrayList<>();
		} else {
			this.authorities = Collections.unmodifiableList(new ArrayList<>(authorities));
		}
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}
	
	public void setAuthorities(Collection<GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

	public DateTime getExpiration() {
		return expiration;
	}
	
	public void setExpiration(DateTime expiration) {
		this.expiration = expiration;
	}
	
	public DateTime getIssuedAt() {
		return issuedAt;
	}
	
	public void setIssuedAt(DateTime issuedAt) {
		this.issuedAt = issuedAt;
	}

	public boolean isExpired() {
		if (expiration == null) {
			return false;
		}
		return expiration.isBefore(DateTime.now());
	}

	public String getFromModule() {
		return fromModule;
	}

	public void setFromModule(String fromModule) {
		this.fromModule = fromModule;
	}
}
