package eu.bcvsolutions.idm.core.security.auth.filter;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.core.AuthenticationException;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.security.exception.IdmAuthenticationException;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Authentication exception context test.
 * @author Jan Helbich
 */
public class AuthenticationExceptionContextTest extends AbstractUnitTest {

	@Test
	public void testExpired() {
		ResultCodeException e = new ResultCodeException(CoreResultCode.AUTH_EXPIRED);
		
		AuthenticationExceptionContext ctx = new AuthenticationExceptionContext();
		ctx.setCodeEx(e);
		
		Assert.assertFalse(ctx.isAuthoritiesChanged());
		Assert.assertFalse(ctx.isDisabledOrNotExists());
		Assert.assertTrue(ctx.isExpired());
	}
	
	@Test
	public void testAuthoritiesChanged() {
		ResultCodeException e = new ResultCodeException(CoreResultCode.AUTHORITIES_CHANGED);
		
		AuthenticationExceptionContext ctx = new AuthenticationExceptionContext();
		ctx.setCodeEx(e);
		
		Assert.assertTrue(ctx.isAuthoritiesChanged());
		Assert.assertFalse(ctx.isDisabledOrNotExists());
		Assert.assertFalse(ctx.isExpired());
	}

	@Test
	public void testDisabledOrNotFound() {
		AuthenticationException e = new IdmAuthenticationException("test");
		
		AuthenticationExceptionContext ctx = new AuthenticationExceptionContext();
		ctx.setAuthEx(e);
		
		Assert.assertFalse(ctx.isAuthoritiesChanged());
		Assert.assertTrue(ctx.isDisabledOrNotExists());
		Assert.assertFalse(ctx.isExpired());
	}
}
