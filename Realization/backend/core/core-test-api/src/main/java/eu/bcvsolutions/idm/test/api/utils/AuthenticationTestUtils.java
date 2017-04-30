package eu.bcvsolutions.idm.test.api.utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.springframework.security.core.GrantedAuthority;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.dto.DefaultGrantedAuthorityDto;
import eu.bcvsolutions.idm.core.security.api.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.api.utils.IdmAuthorityUtils;

public class AuthenticationTestUtils {

	public static IdmJwtAuthenticationDto getAuthDto(String user, Collection<GrantedAuthority> authorities) {
		IdmJwtAuthenticationDto d = new IdmJwtAuthenticationDto();
		d.setCurrentUsername(user);
		d.setIssuedAt(getIat());
		d.setExpiration(getExp());
		d.setAuthorities(new ArrayList<>());
		d.setFromModule("test");
		d.setAuthorities(authorities.stream()
				.map(a -> new DefaultGrantedAuthorityDto(a.toString()))
				.collect(Collectors.toList()));
		return d;
	}
	
	public static IdmJwtAuthentication getSystemAuthentication() {
		return getSystemAuthentication(SecurityService.SYSTEM_NAME);
	}
	
	public static IdmJwtAuthentication getSystemAuthentication(String username) {
		return getAuth(new IdmIdentityDto(username),
				Lists.newArrayList(IdmAuthorityUtils.getAdminAuthority()));
	}

	public static IdmJwtAuthentication getAuth(String username, Collection<GrantedAuthority> authorities) {
		return getAuth(new IdmIdentityDto(username), authorities);
	}
	
	public static IdmJwtAuthentication getAuth(UUID userId, Collection<GrantedAuthority> authorities) {
		return getAuth(new IdmIdentityDto(userId, null), authorities);
	}
	
	public static String getSelfPath(String user) {
		return BaseDtoController.BASE_PATH + "/identities/" + user;
	}

	public static String getBasicAuth(String user, String password) throws UnsupportedEncodingException {
		return Base64.encodeBase64String((user + ":" + password).getBytes("utf-8"));
	}
	
	private static IdmJwtAuthentication getAuth(IdmIdentityDto identity, Collection<GrantedAuthority> authorities) {
		DateTime iat = getIat();
		DateTime exp = getExp();
		return new IdmJwtAuthentication(identity, identity, exp, iat, authorities, "test");
	}

	private static DateTime getIat() {
		return DateTime.now();
	}

	private static DateTime getExp() {
		return DateTime.now().plus(100000);
	}

}
