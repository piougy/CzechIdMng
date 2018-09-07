package eu.bcvsolutions.idm.core.api.utils;

import java.util.List;

import org.springframework.util.Assert;

import com.google.common.base.Throwables;

import eu.bcvsolutions.idm.core.api.exception.CoreException;
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
		List<Throwable> causes = Throwables.getCausalChain(ex);
		// If is some cause instance of ResultCodeException, then we will use only it
		// (for better show on frontend)
		Throwable result = causes.stream()
				.filter(cause -> cause instanceof ResultCodeException)
				.findFirst().orElse(null);
		
		if(result != null) {
			return result;
		}
		
		// If ResultCodeException was not found, then we try to find CoreException
		result = causes.stream()
				.filter(cause -> cause instanceof CoreException)
				.findFirst().orElse(null);

		return result != null ? result : ex;
	}
	
}
