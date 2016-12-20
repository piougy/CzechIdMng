package eu.bcvsolutions.idm.security.rest.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.InvalidSignatureException;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.security.jwt.crypto.sign.SignerVerifier;
import org.springframework.web.filter.GenericFilterBean;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.exception.DefaultErrorModel;
import eu.bcvsolutions.idm.core.api.exception.ErrorModel;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.security.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.security.service.GrantedAuthoritiesFactory;
import eu.bcvsolutions.idm.security.service.impl.OAuthAuthenticationManager;

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

	@Autowired
	private OAuthAuthenticationManager authenticationManager;

	@Autowired
	@Qualifier("objectMapper")
	private ObjectMapper mapper;
	
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
			IdmJwtAuthenticationDto authenticationDto = mapper.readValue(decoded, IdmJwtAuthenticationDto.class);

			if (authenticationDto == null) {
				return;
			}

			authenticationManager.authenticate(grantedAuthoritiesFactory.getIdmJwtAuthentication(authenticationDto));
		} catch (ResultCodeException ex) {			
			sendErrorModel(httpRequest, httpResponse, ex.getError().getError(), ex);
			return;
		} catch (AuthenticationException | InvalidSignatureException ex) {
			ErrorModel errorModel = new DefaultErrorModel(CoreResultCode.AUTH_FAILED); // source exception message will be shown only in log
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
	 * @throws IOException
	 */
	private void sendErrorModel(HttpServletRequest httpRequest, HttpServletResponse httpResponse, ErrorModel errorModel, Exception ex) throws IOException {
		if (errorModel.getStatus().is5xxServerError()) {
			log.error("[" + errorModel.getId() + "] ", ex);
		} else {
			log.warn("[" + errorModel.getId() + "] ", ex);
		}
		httpResponse.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
		httpResponse.setStatus(errorModel.getStatus().value());
		httpResponse.getWriter().print(mapper.writeValueAsString(new ResultModels(errorModel)));
	}
}
