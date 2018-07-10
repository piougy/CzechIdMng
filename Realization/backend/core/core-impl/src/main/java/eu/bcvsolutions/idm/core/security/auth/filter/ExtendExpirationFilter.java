package eu.bcvsolutions.idm.core.security.auth.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.exception.DefaultErrorModel;
import eu.bcvsolutions.idm.core.api.exception.ErrorModel;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.utils.HttpFilterUtils;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.service.impl.JwtAuthenticationMapper;

/**
 * Servlet filter bean handling the extension of JWT authentication token
 * expiration period. The token is extended if the configuration flag
 * is set to true, it is a GET request and the client is successfully
 * authenticated.
 * 
 * @author Jan Helbich
 * @author Radek Tomiška
 *
 */
public class ExtendExpirationFilter extends GenericFilterBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(ExtendExpirationFilter.class);
	public static final String PROPERTY_EXTEND_TOKEN_EXPIRATION = "idm.sec.security.jwt.token.extend.expiration";
	
	@Autowired private SecurityService securityService;
	@Autowired private JwtAuthenticationMapper jwtTokenMapper;
	@Autowired private AuthenticationExceptionContext ctx;
	@Autowired private ConfigurationService configService;
	@Autowired 
	@Qualifier("objectMapper") 
	private ObjectMapper mapper;
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = HttpFilterUtils.asHttp(req);
		HttpServletResponse response = HttpFilterUtils.asHttp(res);
		if (isHttpGet(request) && ctx.isHasIdMToken()) {
			handleToken(request, response);
		}
		
		// response is only committed on error
		if (!res.isCommitted()) {
			chain.doFilter(req, res);
		}
	}

	private void handleToken(HttpServletRequest req, HttpServletResponse res) {
		if (securityService.isAuthenticated() && isExtendExpiration()) {
			doExtendExpiration(req, res);
		} else {
			doCheckTokenExpired(req, res);
		}
	}

	private void doCheckTokenExpired(HttpServletRequest req, HttpServletResponse res) {
		if (ctx.isExpired() || ctx.isAuthoritiesChanged()) {
			sendErrorModel(req, res, ctx.getCodeEx().getError().getError(), ctx.getCodeEx());
		}
		
		if (ctx.isDisabledOrNotExists()) {
			sendErrorModel(req, res, new DefaultErrorModel(CoreResultCode.AUTH_FAILED), ctx.getAuthEx());
		}
	}

	/**
	 * Extends token expiration time. There two types of extensions,
	 * either by just setting new expiration time or by issuing
	 * a fresh token. A fresh token is issued only if the original
	 * one in HTTP request is expired or authorities change and
	 * user signed in by other means than IdM JWT token (remote OAuth / Basic...).
	 * 
	 * The token with extended expiration is set into a response header.
	 * 
	 * @param req
	 * @param res
	 */
	private void doExtendExpiration(HttpServletRequest req, HttpServletResponse res) {
		if (ctx.isDisabledOrNotExists()) {
			// paranoia check - if user is authenticated in spring security context
			// he cannot be disabled or nonexistent
			return;
		}
		
		IdmJwtAuthenticationDto token;
		
		// token either expired or authorities were changed, but user
		// is authenticated by other method then IdM JWT token, therefore
		// this is a valid state and we only issue a fresh IdM token
		if (ctx.isExpired() || ctx.isAuthoritiesChanged()) {
			token = jwtTokenMapper.toDto((IdmJwtAuthentication) 
					SecurityContextHolder.getContext().getAuthentication());
		} else {
			// prolong expiration
			token = jwtTokenMapper.prolongExpiration(ctx.getToken());
		}
		//
		try {
			res.setHeader(JwtAuthenticationMapper.AUTHENTICATION_TOKEN_NAME,
				jwtTokenMapper.writeToken(token));
		} catch (IOException e) {
			LOG.warn("Cannot write token with extended expiration header!");
		}
	}

	private boolean isExtendExpiration() {
		return configService.getBooleanValue(PROPERTY_EXTEND_TOKEN_EXPIRATION, true);
	}

	/**
	 * Send JSON error as response.
	 * 
	 * @param httpResponse
	 * @param errorModel
	 * @param ex
	 * @throws IOException
	 */
	private void sendErrorModel(HttpServletRequest httpRequest, HttpServletResponse httpResponse, 
			ErrorModel errorModel, Exception ex) {
		
		try {
			httpResponse.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
			httpResponse.setStatus(errorModel.getStatus().value());
			httpResponse.getWriter().print(mapper.writeValueAsString(new ResultModels(errorModel)));
			httpResponse.flushBuffer();
		} catch (IOException e) {
			LOG.error("An error [{}] occurred while sending error response status.", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
	
	private boolean isHttpGet(HttpServletRequest request) {
		return request.getMethod().equalsIgnoreCase(HttpMethod.GET.name());
	}

}
