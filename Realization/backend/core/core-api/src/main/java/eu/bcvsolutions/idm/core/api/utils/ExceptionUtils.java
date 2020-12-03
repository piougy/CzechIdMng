package eu.bcvsolutions.idm.core.api.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.util.Assert;

import com.google.common.base.Throwables;

import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;

/**
 * Exception utils
 *
 * @author svandav
 * @author Radek Tomiška
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
		Assert.notNull(ex, "Original exception is required to resolve ResultCodeException.");
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
	
	/**
	 * Logs exception with level defined by excepiton's result model.
	 * 
	 * @param logger
	 * @param ex
	 * @since 9.6.0
	 */
	public static void log(Logger logger, ResultCodeException ex) {
		Assert.notNull(logger, "Logger is required.");
		Assert.notNull(ex, "Exeption is required.");
		// error is required for ResultCodeException - one error will be defined
		ex
			.getError()
			.getErrors()
			.forEach(errorModel -> {
				// TODO: log source ex only the first time?
				log(logger, errorModel, ex);
			});
	}
	
	/**
	 * Logs exception with level defined by given result model.
	 * 
	 * @param logger
	 * @param resultModel
	 * @param ex
	 * @since 9.6.0
	 */
	public static void log(Logger logger, ResultModel resultModel, Throwable ex) {
		Assert.notNull(logger, "Logger is required.");
		// model not given - log exception only, if given
		if (resultModel == null) {
			if (ex != null) {
				logger.error("", ex);
			}
			return;
		}
		NotificationLevel level = resultModel.getLevel();
		if (level != null) { // override http status code 
			switch (level) {
				case SUCCESS: {
					logger.debug(resultModel.toString(), ex);
					break;
				}
				case INFO: {
					logger.info(resultModel.toString(), ex);
					break;
				}
				case WARNING: {
					logger.warn(resultModel.toString(), ex);
					break;
				}
				default: {// error by default
					logger.error(resultModel.toString(), ex);
				}
			}
		} else if (resultModel.getStatus().is5xxServerError()) {
			logger.error(resultModel.toString(), ex);
		} else if(resultModel.getStatus().is2xxSuccessful()) {
			logger.debug(resultModel.toString(), ex);
		} else {
			logger.warn(resultModel.toString(), ex);
		}
	}
	
	/**
	 * Extracts a list of parameters according to the paramKey from the chain of
	 * ResultCodeExceptions with given resultCodes. Exceptions other than
	 * ResultCodeExceptions are skipped. Values in the result list are sorted from
	 * the latest to the earliest exception.
	 * 
	 * @param <T>
	 * @param ex
	 * @param resultCode
	 * @param paramKey
	 * @return
	 */
	public static List<Object> getParameterChainByKey(Throwable ex, String paramKey, ResultCode... resultCode) {
		Assert.notNull(paramKey, "Parameter key is required.");
		Assert.notNull(ex, "Exeption is required.");

		return getConsecutiveResultCodeExceptions(ex, resultCode).stream()
				.map(e -> {
					if (e.getError() == null || e.getError().getError() == null) {
						return null;
					} else {
						return e.getError().getError().getParameters().getOrDefault(paramKey, null);
					}
				})
				.filter(p -> p != null)
				.collect(Collectors.toList());
	}
	
	/**
	 * Extract list of ResultCodeExceptions from the chain of causal exceptions
	 * Returns only those with {@link resultCode} if argument set. 
	 * 
	 * @param ex
	 * @param resultCode
	 * @return
	 */
	public static List<ResultCodeException> getConsecutiveResultCodeExceptions(Throwable ex, ResultCode... resultCode) {
		Assert.notNull(ex, "Exeption is required.");
		Set<String> rcSet = Arrays.asList(resultCode).stream()
				.map(rc -> rc.getCode())
				.collect(Collectors.toSet());

		return Throwables.getCausalChain(ex).stream()
				.filter(e -> e instanceof ResultCodeException)
				.map(e -> (ResultCodeException) e)
				.filter(e -> {
					if (rcSet.isEmpty()) {
						return true;
					}
					if (e.getError() == null || e.getError().getError() == null) {
						return false;
					}
					return rcSet.contains(e.getError().getError().getStatusEnum());})
				.collect(Collectors.toList());
	}
}
