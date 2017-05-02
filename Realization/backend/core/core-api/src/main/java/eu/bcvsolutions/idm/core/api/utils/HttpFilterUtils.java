package eu.bcvsolutions.idm.core.api.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.SignerVerifier;

/**
 * Utilities for HTTP authentication.
 *  
 * @author Jan Helbich
 *
 */
public class HttpFilterUtils {
	
	private static final Logger LOG = LoggerFactory.getLogger(HttpFilterUtils.class);
	
	public static final String AUTHORIZATION_TYPE_BEARER_PREFIX = "Bearer ";
	
	public static final String JWT_AUD = "aud";
	public static final String JWT_CLIENT_ID = "client_id";
	public static final String JWT_EXP = "exp";
	public static final String JWT_JTI = "jti";
	public static final String JWT_GRANT_TYPE = "grant_type";
	public static final String JWT_ATI = "ati";
	public static final String JWT_USER_NAME = "user_name";
	public static final String JWT_SCOPE = "scope";
	public static final String JWT_AUTHORITIES = "authorities";
	
	
	public static Optional<Jwt> parseToken(String tokenString) {
		try {
			return Optional.of(JwtHelper.decode(tokenString));
		} catch (Exception e) {
			LOG.debug("Cannot parse token [{}] as JWT token.", tokenString, e);
		}
		return Optional.empty();
	}
	
	public static void verifyToken(Jwt token, SignerVerifier verifier) {
		token.verifySignature(verifier);
	}
	
	public static HttpServletRequest asHttp(ServletRequest request) {
		return (HttpServletRequest) request;
	}

	public static HttpServletResponse asHttp(ServletResponse response) {
		return (HttpServletResponse) response;
	}
	
	public static List<String> getHeaders(HttpServletRequest request, String headerName) {
		Enumeration<String> authHeaders = request.getHeaders(headerName);
		List<String> authHeadersList = new ArrayList<>();
		if (authHeaders != null) {
			authHeadersList = Collections.list(authHeaders);
		}
		return authHeadersList;
	}
	
	public static Set<String> filterTransformHeaders(List<String> headers) {
		return filterTransformHeaders(headers, "");
	}
	
	public static Set<String> filterTransformHeaders(List<String> headers, String headerPrefix) {
		return headers.stream()
			.filter(h -> !StringUtils.isEmpty(h))
			.filter(h -> h.contains(headerPrefix))
			.map(h -> h.substring(getHeaderSubstringIndex(h, headerPrefix)))
			.collect(Collectors.toSet());
	}
	
	private static int getHeaderSubstringIndex(String header, String prefix) {
		return header.indexOf(prefix) + prefix.length();
	}
	
}
