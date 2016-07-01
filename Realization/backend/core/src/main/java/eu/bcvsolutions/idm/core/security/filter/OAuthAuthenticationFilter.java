package eu.bcvsolutions.idm.core.security.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.InvalidSignatureException;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.security.jwt.crypto.sign.SignerVerifier;
import org.springframework.web.filter.GenericFilterBean;

import com.fasterxml.jackson.core.JsonParseException;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.DefaultErrorModel;
import eu.bcvsolutions.idm.core.exception.ErrorModel;
import eu.bcvsolutions.idm.core.exception.RestApplicationException;
import eu.bcvsolutions.idm.core.model.dto.ResultModels;
import eu.bcvsolutions.idm.core.security.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.core.security.service.GrantedAuthoritiesFactory;
import eu.bcvsolutions.idm.core.security.service.impl.OAuthAuthenticationManager;

/**
 * OAuth authentication filter
 * 
 * @author svandav
 *
 */
public class OAuthAuthenticationFilter extends GenericFilterBean {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OAuthAuthenticationFilter.class);
	public static final String AUTHENTICATION_TOKEN_NAME = "CIDMST";

	@Value("${security.jwt.secretPhrase:idmSecret}")
	private String secret;
	
	@Value("#{'${allowed-origins:http://localhost:3000}'.replaceAll(\"\\s*\",\"\").split(',')}")
	private List<String> allowedOrigins;

	@Autowired
	private OAuthAuthenticationManager authenticationManager;

	@Autowired
	private ObjectMapper jsonMapper;
	
	@Autowired
	private GrantedAuthoritiesFactory grantedAuthoritiesFactory;

	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;

		String tokenHeader = httpRequest.getHeader(AUTHENTICATION_TOKEN_NAME);
		String tokenParameter = httpRequest.getParameter(AUTHENTICATION_TOKEN_NAME);

		if (tokenHeader == null && tokenParameter == null) {
			chain.doFilter(request, response);
			return;
		}
		String token = tokenHeader != null ? tokenHeader : tokenParameter;

		SignerVerifier verifier = new MacSigner(secret);

		try {
			String decoded = JwtHelper.decodeAndVerify(token, verifier).getClaims();
			IdmJwtAuthenticationDto authenticationDto = jsonMapper.readValue(decoded, IdmJwtAuthenticationDto.class);

			if (authenticationDto == null) {
				return;
			}

			Authentication newAuthentication = authenticationManager.authenticate(grantedAuthoritiesFactory.getIdmJwtAuthentication(authenticationDto));
			SecurityContextHolder.getContext().setAuthentication(newAuthentication);
		} catch (RestApplicationException ex) {
			sendErrorModel(httpRequest, httpResponse, ex.getError().getError(), ex);
			return;
		} catch (AuthenticationException | InvalidSignatureException ex) {
			ErrorModel errorModel = new DefaultErrorModel(CoreResultCode.AUTH_FAILED, new Object[]{ }); // source exception message will be shown only in log
			sendErrorModel(httpRequest, httpResponse, errorModel, ex);
			return;
		} catch (JsonParseException | IllegalArgumentException ex) {
			ErrorModel errorModel = new DefaultErrorModel(CoreResultCode.BAD_REQUEST, ex.getLocalizedMessage());  
			sendErrorModel(httpRequest, httpResponse, errorModel, ex);
			return;
		}
		chain.doFilter(request, response);
	}
	
	/**
	 * Send json error as response
	 * 
	 * @param httpResponse
	 * @param errorModel
	 * @param ex
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private void sendErrorModel(HttpServletRequest httpRequest, HttpServletResponse httpResponse, ErrorModel errorModel, Exception ex) 
			throws JsonGenerationException, JsonMappingException, IOException {
		log.error("[" + errorModel.getId() + "] ", ex);
		httpResponse.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
		httpResponse.setStatus(errorModel.getStatus().value());
		httpResponse.getWriter().print(jsonMapper.writeValueAsString(new ResultModels(errorModel)));
	}
}
