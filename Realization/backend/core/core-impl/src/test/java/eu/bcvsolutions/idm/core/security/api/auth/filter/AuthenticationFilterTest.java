package eu.bcvsolutions.idm.core.security.api.auth.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.core.api.rest.PublicController;
import eu.bcvsolutions.idm.core.security.api.filter.IdmAuthenticationFilter;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.auth.filter.BasicIdmAuthenticationFilter;
import eu.bcvsolutions.idm.core.security.auth.filter.JwtIdmAuthenticationFilter;
import eu.bcvsolutions.idm.test.api.AbstractVerifiableUnitTest;

/**
 * @author Jan Helbich
 */
public class AuthenticationFilterTest extends AbstractVerifiableUnitTest {

	@Mock
	private ApplicationContext context;
	@Mock
	private SecurityService securityService;
	@Mock
	private EnabledEvaluator enabledEvaluator;
	@Mock
	private List<IdmAuthenticationFilter> filters;
	@Spy
	@InjectMocks
	private AuthenticationFilter authenticationFilter;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testIgnoreDisabledFilters() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);
		JwtIdmAuthenticationFilter jwtFilter = new JwtIdmAuthenticationFilter();
		TestAuthFilter testAuthFilter = new TestAuthFilter();
		when(request.getServletPath())
				.thenReturn("/mock");
		when(context.getBeansOfType(PublicController.class))
				.thenReturn(new HashMap<>());
		when(filters.stream())
				.thenReturn(Stream.of(jwtFilter, testAuthFilter));
		when(enabledEvaluator.isEnabled(testAuthFilter))
				.thenReturn(false);
		when(enabledEvaluator.isEnabled(jwtFilter))
				.thenReturn(true);
		when(securityService.isAuthenticated())
			.thenReturn(false);
		when(response.isCommitted())
				.thenReturn(false);
		doNothing()
				.when(chain).doFilter(any(), any());

		authenticationFilter.doFilter(request, response, chain);

		verify(context, times(1)).getBeansOfType(PublicController.class);
		verify(enabledEvaluator, times(1)).isEnabled(jwtFilter);
		verify(enabledEvaluator, times(1)).isEnabled(testAuthFilter);
		verify(authenticationFilter, never()).handleAuthenticationHeader(any(), any(), eq(testAuthFilter));
		verify(authenticationFilter, times(1)).handleAuthenticationHeader(any(), any(), eq(jwtFilter));
		verify(authenticationFilter, times(1)).doFilter(any(), any(), any());
		verify(filters, times(1)).stream();
		verify(securityService, times(1)).isAuthenticated();
	}

	public static class TestAuthFilter extends BasicIdmAuthenticationFilter {
	}
}