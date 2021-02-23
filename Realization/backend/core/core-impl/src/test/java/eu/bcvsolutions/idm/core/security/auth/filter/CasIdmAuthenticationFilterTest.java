package eu.bcvsolutions.idm.core.security.auth.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.AssertionImpl;
import org.jasig.cas.client.validation.TicketValidationException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.config.domain.CasConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.exception.TwoFactorAuthenticationRequiredException;
import eu.bcvsolutions.idm.core.security.api.service.JwtAuthenticationService;
import eu.bcvsolutions.idm.core.security.service.impl.DefaultCasValidationService;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Test authentication using CAS.
 *
 * @author Roman Kučera
 */
@Transactional
public class CasIdmAuthenticationFilterTest extends AbstractUnitTest {

	private String TEST_TOKEN = "token-123";
	private String CAS_USER = "casSsoTestUser";

	private String CAS_URL = "http://cas/cas";
	private String IDM_URL = "http://idm/idm";

	@Mock
	private DefaultCasValidationService casValidationService;
	@Mock
	private IdmIdentityService identityService;
	@Mock
	private JwtAuthenticationService jwtAuthenticationService;
	@Mock
	private CasConfiguration casConfiguration;

	@InjectMocks
	private CasIdmAuthenticationFilter casIdmAuthenticationFilter;

	@Test
	public void testAuthorizeSuccess() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		IdmIdentityDto idmIdentityDto = new IdmIdentityDto(CAS_USER);
		LoginDto loginDto = new LoginDto(idmIdentityDto);

		AttributePrincipalImpl attributePrincipal = new AttributePrincipalImpl(CAS_USER);
		AssertionImpl assertion = new AssertionImpl(attributePrincipal);

		when(casConfiguration.getPropertyCasUrl()).thenReturn(CAS_URL);
		when(casConfiguration.getPropertyIdmUrl()).thenReturn(IDM_URL);

		when(casValidationService.validate(TEST_TOKEN, IDM_URL, CAS_URL)).thenReturn(assertion);
		when(identityService.getByUsername(CAS_USER)).thenReturn(idmIdentityDto);

		when(jwtAuthenticationService.createJwtAuthenticationAndAuthenticate(any(LoginDto.class), eq(idmIdentityDto),
				eq(CoreModuleDescriptor.MODULE_ID))).thenReturn(loginDto);

		boolean authorizeResult = casIdmAuthenticationFilter.authorize(TEST_TOKEN, request, response);

		Assert.assertTrue(authorizeResult);
	}

	@Test
	public void testAuthorizeFailedNoConfProperties() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		boolean authorizeResult = casIdmAuthenticationFilter.authorize(TEST_TOKEN, request, response);

		Assert.assertFalse(authorizeResult);
	}

	@Test
	public void testAuthorizeFailedNoToken() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		when(casConfiguration.getPropertyCasUrl()).thenReturn(CAS_URL);
		when(casConfiguration.getPropertyIdmUrl()).thenReturn(IDM_URL);

		boolean authorizeResult = casIdmAuthenticationFilter.authorize(null, request, response);

		Assert.assertFalse(authorizeResult);
	}

	@Test
	public void testAuthorizeFailedAssertionNull() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		when(casConfiguration.getPropertyCasUrl()).thenReturn(CAS_URL);
		when(casConfiguration.getPropertyIdmUrl()).thenReturn(IDM_URL);

		boolean authorizeResult = casIdmAuthenticationFilter.authorize("Unknowntoken", request, response);

		Assert.assertFalse(authorizeResult);
	}

	@Test
	public void testAuthorizeFailedAssertionException() throws TicketValidationException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		when(casConfiguration.getPropertyCasUrl()).thenReturn(CAS_URL);
		when(casConfiguration.getPropertyIdmUrl()).thenReturn(IDM_URL);

		when(casValidationService.validate(TEST_TOKEN, IDM_URL, CAS_URL)).thenThrow(TicketValidationException.class);

		boolean authorizeResult = casIdmAuthenticationFilter.authorize(TEST_TOKEN, request, response);

		Assert.assertFalse(authorizeResult);
	}

	@Test()
	public void testAuthorizeFailedNullIdentity() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		AttributePrincipalImpl attributePrincipal = new AttributePrincipalImpl(CAS_USER);
		AssertionImpl assertion = new AssertionImpl(attributePrincipal);

		when(casConfiguration.getPropertyCasUrl()).thenReturn(CAS_URL);
		when(casConfiguration.getPropertyIdmUrl()).thenReturn(IDM_URL);

		when(casValidationService.validate(TEST_TOKEN, IDM_URL, CAS_URL)).thenReturn(assertion);
		when(identityService.getByUsername(CAS_USER)).thenReturn(null);

		boolean authorizeResult = casIdmAuthenticationFilter.authorize(TEST_TOKEN, request, response);

		Assert.assertFalse(authorizeResult);
	}

	@Test(expected = TwoFactorAuthenticationRequiredException.class)
	public void testTwoFactorAuthenticationRequiredException() throws TicketValidationException {

		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		IdmIdentityDto idmIdentityDto = new IdmIdentityDto(CAS_USER);

		AttributePrincipalImpl attributePrincipal = new AttributePrincipalImpl(CAS_USER);
		AssertionImpl assertion = new AssertionImpl(attributePrincipal);

		when(casConfiguration.getPropertyCasUrl()).thenReturn(CAS_URL);
		when(casConfiguration.getPropertyIdmUrl()).thenReturn(IDM_URL);

		when(casValidationService.validate(TEST_TOKEN, IDM_URL, CAS_URL)).thenReturn(assertion);
		when(identityService.getByUsername(CAS_USER)).thenReturn(idmIdentityDto);

		when(jwtAuthenticationService.createJwtAuthenticationAndAuthenticate(any(LoginDto.class), eq(idmIdentityDto),
				eq(CoreModuleDescriptor.MODULE_ID))).thenThrow(TwoFactorAuthenticationRequiredException.class);

		casIdmAuthenticationFilter.authorize(TEST_TOKEN, request, response);
	}

}