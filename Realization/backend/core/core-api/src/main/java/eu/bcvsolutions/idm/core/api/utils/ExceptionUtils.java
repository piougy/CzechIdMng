package eu.bcvsolutions.idm.core.api.utils;

import java.util.List;

import org.springframework.util.Assert;

import com.google.common.base.Throwables;

import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;

/**
 * Exception utils
 *
 * @author svandav
 */
public abstract class ExceptionUtils {
	
	/**
	 * If exception causal chain contains cause instance of ResultCodeException,
	 * then is return primary.
	 * 
	 * @param ex
	 * @return
	 */
	public static Throwable resolveException(Throwable ex) {
		Assert.notNull(ex);
		Throwable exceptionToLog = null;
		List<Throwable> causes = Throwables.getCausalChain(ex);
		// If is some cause instance of ResultCodeException, then we will use only it
		// (for better show on frontend)
		Throwable resultCodeException = causes.stream().filter(cause -> {
			if (cause instanceof ResultCodeException) {
				return true;
			}
			return false;
		}).findFirst().orElse(null);

		exceptionToLog = resultCodeException != null ? resultCodeException : ex;
		return exceptionToLog;
	}
	
}
