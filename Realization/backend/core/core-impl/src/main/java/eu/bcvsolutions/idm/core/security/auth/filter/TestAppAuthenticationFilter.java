package eu.bcvsolutions.idm.core.security.auth.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.security.oauth2.common.util.JsonParser;
import org.springframework.security.oauth2.common.util.JsonParserFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.api.utils.HttpFilterUtils;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.filter.IdmAuthenticationFilter;
import eu.bcvsolutions.idm.core.security.rest.impl.LoginController;
import eu.bcvsolutions.idm.core.security.service.GrantedAuthoritiesFactory;

/**
 * Example filter implementing sample remote OAuth2 provider
 * authentication.
 * 
 * @author Jan Helbich
 *
 */
//@Order(999)
//@Component
public class TestAppAuthenticationFilter implements IdmAuthenticationFilter {
	
	@Autowired
	private IdmIdentityService identityService;
	
	@Autowired
	private GrantedAuthoritiesFactory grantedAuthoritiesFactory;
	
	@Override
	public boolean authorize(String token, HttpServletRequest request, HttpServletResponse response) {
		try {
			Optional<Jwt> jwt = HttpFilterUtils.parseToken(token);
			if (!jwt.isPresent()) {
				return false;
			}
			
			Map<String, Object> claims = verifyTokenAndGetClaims(jwt.get());
			String userName = (String) claims.get(HttpFilterUtils.JWT_USER_NAME);
			IdmIdentityDto identity = identityService.getByUsername(userName);
			// not important - either new refreshed token or data are returned to user
			DateTime expiration = null; 
			
			Collection<GrantedAuthority> authorities = null;
			if (shouldGrantAuthoritiesForPath(request.getServletPath())) {
				authorities = grantedAuthoritiesFactory.getGrantedAuthoritiesForIdentity(identity.getId());
			} else {
				authorities = new ArrayList<>();
			}
			
			IdmJwtAuthentication ija = new IdmJwtAuthentication(identity, expiration, 
					authorities, EntityUtils.getModule(this.getClass()));
			SecurityContextHolder.getContext().setAuthentication(ija);
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public String getAuthorizationHeaderPrefix() {
		return HttpFilterUtils.AUTHORIZATION_TYPE_BEARER_PREFIX;
	}
	
	private boolean shouldGrantAuthoritiesForPath(String servletPath) {
		return servletPath != null && servletPath.matches(getIgnoreAuthoritiesPath());
	}
	
	private String getIgnoreAuthoritiesPath() {
		return BaseController.BASE_PATH + "/authentication" + LoginController.REMOTE_AUTH_PATH + ".*";
	}

	private Map<String, Object> verifyTokenAndGetClaims(Jwt jwt) {
		Map<String, Object> claims = getClaimsAsMap(jwt);
		
		// TODO validate claims
		if (claims.get(HttpFilterUtils.JWT_CLIENT_ID) == null || !claims.get(HttpFilterUtils.JWT_CLIENT_ID).equals("idm")) {
			throw new ValidationException("Invalid test app token.");
		}
		
		Optional<String> signingKey = getSigningKey();
		if (!signingKey.isPresent()) {
			throw new ValidationException("Cannot get signing key.");
		}
		
		jwt.verifySignature(new MacSigner(signingKey.get()));
		return claims;
	}

	private Map<String, Object> getClaimsAsMap(Jwt jwt) {
		JsonParser parser = JsonParserFactory.create();
		Map<String, Object> map = parser.parseMap(jwt.getClaims());
		if (map.containsKey(HttpFilterUtils.JWT_EXP) && map.get(HttpFilterUtils.JWT_EXP) instanceof Integer) {
			Integer intValue = (Integer) map.get(HttpFilterUtils.JWT_EXP);
			map.put(HttpFilterUtils.JWT_EXP, new Long(intValue));
		}
		return map;
	}

	
	private Optional<String> getSigningKey() {
		CredentialsProvider provider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("idm", "");
		provider.setCredentials(AuthScope.ANY, credentials);
		
		HttpClient client = HttpClientBuilder.create()
			.setDefaultCredentialsProvider(provider)
			.setDefaultRequestConfig(RequestConfig.custom()
					.setConnectTimeout(1 * 1000)
					.setSocketTimeout(1 * 1000)
					.build())
			.build();
		
		try {
			HttpResponse response = client.execute(new HttpGet("http://localhost:9080/oauth/token_key"));
			int statusCode = response.getStatusLine().getStatusCode();
			
			System.out.println("Status: " + statusCode);
			
			if (statusCode < 400) {
				ObjectMapper om = new ObjectMapper();
				KeyAlg key = om.readValue(response.getEntity().getContent(), KeyAlg.class);
				return Optional.of(key.getValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}
	
	@SuppressWarnings("serial")
	public static class KeyAlg implements Serializable {
		
		private String alg;
		private String value;

		public String getAlg() {
			return alg;
		}
		public void setAlg(String alg) {
			this.alg = alg;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		@Override
		public String toString() {
			return "KeyAlg [alg=" + alg + ", value=" + value + "]";
		}
		
	}
	
	
	// --------
	
	
	public static void main(String[] args) {
		System.out.println(new TestAppAuthenticationFilter().getSigningKey().get());
	}

}
