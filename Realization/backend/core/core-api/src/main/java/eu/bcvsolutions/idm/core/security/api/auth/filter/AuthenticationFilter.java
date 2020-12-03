package eu.bcvsolutions.idm.core.security.api.auth.filter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.filter.GenericFilterBean;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.rest.PublicController;
import eu.bcvsolutions.idm.core.api.utils.HttpFilterUtils;
import eu.bcvsolutions.idm.core.security.api.filter.IdmAuthenticationFilter;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Base class for Idm authentication filters. All authentication
 * filters are called from this class, which handles the HTTP request
 * and authentication flow.
 *
 * @author Jan Helbich
 * @author Radek Tomiška
 */
public class AuthenticationFilter extends GenericFilterBean {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AuthenticationFilter.class);
	//
	@Autowired private ApplicationContext context;
	@Autowired private SecurityService securityService;
	@Autowired @Lazy private List<IdmAuthenticationFilter> filters;
	@Autowired private EnabledEvaluator enabledEvaluator;
	//
	private Set<RequestMatcher> publicPathRequestMatchers = null;

	/**
	 * Authentication flow implementation.
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = HttpFilterUtils.asHttp(req);
		//
		boolean isPublicPath = false;
		if (getPublicPathRequestMatchers().stream().anyMatch(requestMatcher -> requestMatcher.matches(request))) {
			LOG.debug("Authentication filter will be optional for public path [{}].", request.getServletPath());
			isPublicPath = true;
		} 
		//
		HttpServletResponse response = HttpFilterUtils.asHttp(res);
		try {
			filters.stream()
				.filter(f -> enabledEvaluator.isEnabled(f))
				.filter(f -> !f.isDisabled())
				.filter(f -> isAuthenticated()
						|| res.isCommitted()
						|| f.authorize(request, response)
						|| handleAuthenticationHeader(request, response, f))
				.findFirst();
		} catch (Exception ex) {
			if (!isPublicPath) {
				// not public => authentication is required
				throw ex;
			}
			// public path => authentication is optional
			LOG.debug("Exception is occured by authentication filters on public page, authentication will not be set.", ex);
		}
		
		if (!res.isCommitted()) {
			chain.doFilter(req, res);
		}
	}
	
	/**
	 * Resolve public paths, which will not be filtered by authentication filter.
	 * 
	 * @return all public paths available without authentication is required as request matchers
	 * @since 10.7.0
	 */
	public Set<RequestMatcher> getPublicPathRequestMatchers() {
		if (publicPathRequestMatchers == null) {
			publicPathRequestMatchers = getPublicPaths()
					.stream()
					.map(pathPattern -> new AntPathRequestMatcher(pathPattern))
					.collect(Collectors.toSet());
		}
		//
		return publicPathRequestMatchers;
	}
	
	/**
	 * Resolve public paths, which will not be filtered by authentication filter.
	 * 
	 * @return all public paths available without authentication is required
	 * @since 10.7.0
	 */
	protected Set<String> getPublicPaths() {
		Set<String> publicPaths = Sets.newHashSet(
				BaseDtoController.BASE_PATH + "/public/**", // controllers with public prefix is public by default
				BaseDtoController.BASE_PATH + "/websocket-info/**",
				BaseDtoController.BASE_PATH, // endpoint with supported services list
				BaseDtoController.BASE_PATH + "/authentication", // login
				BaseDtoController.BASE_PATH + "/authentication/two-factor", // login two factor
				"/error/**",
				BaseDtoController.BASE_PATH + "/doc", // documentation is public
				BaseDtoController.BASE_PATH + "/doc/**"); // websockets has their own security configuration
		//
		context
			.getBeansOfType(PublicController.class)
			.values()
			.forEach(publicController -> {
				Class<?> clazz = AopUtils.getTargetClass(publicController);
			    if (clazz.isAnnotationPresent(RequestMapping.class)) {
			    	RequestMapping mapping = clazz.getAnnotation(RequestMapping.class);
					publicPaths.addAll(Sets.newHashSet(mapping.value()));
					publicPaths.addAll(Sets.newHashSet(mapping.path()));
				}
				// controller methods mapping should be public too
				while (clazz != Object.class) {
			        for (Method method : clazz.getDeclaredMethods()) {
			            if (method.isAnnotationPresent(RequestMapping.class)) {
			            	RequestMapping mapping = method.getAnnotation(RequestMapping.class);
		                	publicPaths.addAll(Sets.newHashSet(mapping.value()));
		                	publicPaths.addAll(Sets.newHashSet(mapping.path()));
			            }
			        }
			        clazz = clazz.getSuperclass();
			    }
			});
		//
		LOG.debug("Resolved public paths [{}]", publicPaths);
		return publicPaths;
	}

	/**
	 * Generic method for parsing the authentication token out of HTTP request.
	 * If specified token is found, it is passed to the authentication method
	 * of specific filter (implementing {@link IdmAuthenticationFilter}).
	 *
	 * @return is user authenticated
	 */
	protected boolean handleAuthenticationHeader(HttpServletRequest req, HttpServletResponse res,
			IdmAuthenticationFilter filter) {

		// transform the token from all matching auth. headers and find first successful
		Optional<String> succToken = HttpFilterUtils
				.filterTransformHeaders(getTokens(req, filter), filter.getAuthorizationHeaderPrefix())
				.stream()
				.filter(token -> filter.authorize(token, req, res))
				.findFirst();

		return succToken.isPresent();
	}

	protected SecurityService getSecurityService() {
		return securityService;
	}

	protected List<IdmAuthenticationFilter> getFilters() {
		return Collections.unmodifiableList(Lists.newArrayList(filters));
	}

	private boolean isAuthenticated() {
		return securityService.isAuthenticated();
	}

	private List<String> getTokens(HttpServletRequest request, IdmAuthenticationFilter filter) {
		List<String> tokens = new ArrayList<>();
		String token = request.getParameter(filter.getTokenParameterName());
		if (StringUtils.isNotBlank(token)) {
			LOG.trace("Authorization token found in url parameter [{}].", filter.getTokenParameterName());
			tokens.add(token);
		}
		tokens.addAll(HttpFilterUtils.getHeaders(request, filter.getAuthorizationHeaderName()));
		//
		LOG.trace("Authorization token found [{}].", tokens.size());
		//
		return tokens;
	}
}
