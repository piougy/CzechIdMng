package eu.bcvsolutions.idm.core.api.utils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.exception.DefaultErrorModel;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.test.api.AbstractVerifiableUnitTest;

/**
 * Logging unit test
 * 
 * @author Radek Tomiška
 *
 */
public class ExceptionUtilsUnitTest extends AbstractVerifiableUnitTest {
	
	@Mock
	private org.slf4j.Logger LOG;

	@Test(expected = IllegalArgumentException.class)
	public void testLogWithoutException() {
		ExceptionUtils.log(LOG, null);
	}
	
	@Test
	public void testLogWithoutModelAndException() {
		ExceptionUtils.log(LOG, null, null);
		//
		Mockito.verifyZeroInteractions(LOG);
	}
	
	@Test
	public void testLogExceptionWithoutModel() {
		ExceptionUtils.log(LOG, null, new CoreException("mock"));
		// error is logged without model is specified
		verify(LOG).error(any(String.class), any(CoreException.class));
	}
	
	@Test
	public void testLogModelWithoutException() {
		ExceptionUtils.log(LOG, new DefaultErrorModel(CoreResultCode.INTERNAL_SERVER_ERROR), null);
		// error is logged without model is specified
		verify(LOG).error(any(String.class), (Throwable) ArgumentMatchers.isNull());
	}
	
	@Test
	public void testLogModelLevelError() {
		ExceptionUtils.log(LOG, new DefaultErrorModel(CoreResultCode.INTERNAL_SERVER_ERROR), new CoreException("mock"));
		// error is logged without model is specified
		verify(LOG).error(any(String.class), any(Exception.class));
	}
	
	@Test
	public void testLogModelLevelWarn() {
		ExceptionUtils.log(LOG, new DefaultErrorModel(CoreResultCode.FORBIDDEN), new CoreException("mock"));
		// error is logged without model is specified
		verify(LOG).warn(any(String.class), any(Exception.class));
	}
	
	@Test
	public void testLogModelLevelDebug() {
		ExceptionUtils.log(LOG, new DefaultErrorModel(CoreResultCode.ACCEPTED), new CoreException("mock"));
		// error is logged without model is specified
		verify(LOG).debug(any(String.class), any(Exception.class));
	}
	
	@Test
	public void testLogExceptionLevelError() {
		ExceptionUtils.log(LOG, new ResultCodeException(new DefaultErrorModel(CoreResultCode.INTERNAL_SERVER_ERROR)));
		// error is logged without model is specified
		verify(LOG).error(any(String.class), any(Exception.class));
	}
	
	@Test
	public void testLogExceptionLevelWarn() {
		ExceptionUtils.log(LOG, new ResultCodeException(new DefaultErrorModel(CoreResultCode.FORBIDDEN)));
		// error is logged without model is specified
		verify(LOG).warn(any(String.class), any(Exception.class));
	}
	
	@Test
	public void testExceptionLevelDebug() {
		ExceptionUtils.log(LOG, new ResultCodeException(new DefaultErrorModel(CoreResultCode.ACCEPTED)));
		// error is logged without model is specified
		verify(LOG).debug(any(String.class), any(Exception.class));
	}
}
