package eu.bcvsolutions.idm.core.exception;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.util.WebUtils;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.exception.DefaultErrorModel;
import eu.bcvsolutions.idm.core.api.exception.ErrorModel;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.utils.ExceptionUtils;
import eu.bcvsolutions.idm.core.security.api.exception.IdmAuthenticationException;

/**
 * Handles application exceptions and translate them to result codes.
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@ControllerAdvice
public class ExceptionControllerAdvice implements ErrorController {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ExceptionControllerAdvice.class);
	
	@Autowired private ConfigurationService configurationService;
	@Autowired private CorsConfiguration corsConfiguration;
	
	@ExceptionHandler(ResultCodeException.class)
    public ResponseEntity<ResultModels> handle(ResultCodeException ex) {
		ExceptionUtils.log(LOG, ex);
		//
		return new ResponseEntity<>(ex.getError(), new HttpHeaders(), ex.getStatus());
	}
	
	@ExceptionHandler(IdmAuthenticationException.class)
	public ResponseEntity<ResultModels> handle(IdmAuthenticationException ex) {
		ErrorModel errorModel = new DefaultErrorModel(CoreResultCode.AUTH_FAILED);
		 // source exception message is shown only in log 
		LOG.warn("[" + errorModel.getId() + "] ", ex);
        return new ResponseEntity<>(new ResultModels(errorModel), new HttpHeaders(), errorModel.getStatus());
    }
	
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ResultModels> handle(HttpRequestMethodNotSupportedException ex) {
		ErrorModel errorModel = new DefaultErrorModel(CoreResultCode.METHOD_NOT_ALLOWED, ex.getMessage(),
				ImmutableMap.of( //
						"errorMethod", ex.getMethod(), //
						"supportedMethods", StringUtils.join(ex.getSupportedMethods(), ", ")));
		LOG.warn("[" + errorModel.getId() + "] ", ex);
		return new ResponseEntity<>(new ResultModels(errorModel), new HttpHeaders(), errorModel.getStatus());
	}
	
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ResultModels> handle(HttpMessageNotReadableException ex) {
		ErrorModel errorModel = new DefaultErrorModel(CoreResultCode.METHOD_NOT_ALLOWED, ex.getMessage());
		LOG.warn("[" + errorModel.getId() + "] ", ex);
        return new ResponseEntity<>(new ResultModels(errorModel), new HttpHeaders(), errorModel.getStatus());
    }
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ResultModels> handle(MethodArgumentNotValidException ex) {		
		List<ErrorModel> errorModels = ex.getBindingResult().getFieldErrors().stream()
			.map(fieldError -> new FieldErrorModel(fieldError))
			.peek(errorModel -> LOG.warn("[" + errorModel.getId() + "] ", ex))
			.collect(Collectors.toList());
		// TODO: global errors
		// TODO: better errorModel logging - move source exception to errorModel?	
        return new ResponseEntity<>(new ResultModels(errorModels), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
	
	@ExceptionHandler(javax.validation.ConstraintViolationException.class)
	public ResponseEntity<ResultModels> handle(javax.validation.ConstraintViolationException ex) {		
		List<ErrorModel> errorModels = ex.getConstraintViolations().stream()
			.map(constraintViolation -> new FieldErrorModel(constraintViolation))
			.peek(errorModel -> LOG.warn("[" + errorModel.getId() + "] ", ex))
			.collect(Collectors.toList());
		// TODO: global errors
		// TODO: better errorModel logging - move source exception to errorModel?
        return new ResponseEntity<>(new ResultModels(errorModels), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
	
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ResultModels> handle(DataIntegrityViolationException ex) {
		ErrorModel errorModel = null;
		//
		if (ex.getCause() != null && ex.getCause() instanceof ConstraintViolationException){
			ConstraintViolationException constraintEx = (ConstraintViolationException) ex.getCause();
			// TODO: registrable constraint error codes
			if (constraintEx.getConstraintName() != null && constraintEx.getConstraintName().contains("name")) {
				errorModel = new DefaultErrorModel(CoreResultCode.NAME_CONFLICT, ImmutableMap.of("name", constraintEx.getConstraintName()));
			} else if (constraintEx.getConstraintName() != null && constraintEx.getConstraintName().contains("code")) {
				errorModel = new DefaultErrorModel(CoreResultCode.CODE_CONFLICT, ImmutableMap.of("name", constraintEx.getConstraintName()));
			} else if (constraintEx.getConstraintName() == null) {
				errorModel = new DefaultErrorModel(CoreResultCode.CONFLICT, ImmutableMap.of("name", "..."));
			} else {
				errorModel = new DefaultErrorModel(CoreResultCode.CONFLICT, ImmutableMap.of("name", StringUtils.trimToEmpty(constraintEx.getConstraintName())));
			}
		} else {
			errorModel = new DefaultErrorModel(CoreResultCode.CONFLICT, ex.getMostSpecificCause().getMessage());
		}
		LOG.error("[" + errorModel.getId() + "] ", ex);
		return new ResponseEntity<>(new ResultModels(errorModel), new HttpHeaders(), errorModel.getStatus());
    }
	
	@ExceptionHandler(PersistenceException.class)
	public ResponseEntity<ResultModels> handle(PersistenceException ex) {
		ErrorModel errorModel = null;
		//
		if (ex.getCause() != null && ex.getCause() instanceof ConstraintViolationException){
			ConstraintViolationException constraintEx = (ConstraintViolationException) ex.getCause();
			// TODO: registrable contstrain error codes
			if (constraintEx.getConstraintName().contains("name")) {
				errorModel = new DefaultErrorModel(CoreResultCode.NAME_CONFLICT, ImmutableMap.of("name", constraintEx.getConstraintName()));
			} else if (constraintEx.getConstraintName().contains("code")) {
				errorModel = new DefaultErrorModel(CoreResultCode.CODE_CONFLICT, ImmutableMap.of("name", constraintEx.getConstraintName()));
			} else {
				errorModel = new DefaultErrorModel(CoreResultCode.CONFLICT, ImmutableMap.of("name", constraintEx.getConstraintName()));
			}
		} else {
			errorModel = new DefaultErrorModel(CoreResultCode.CONFLICT, ex.getMessage());
		}
		LOG.error("[" + errorModel.getId() + "] ", ex);
		return new ResponseEntity<>(new ResultModels(errorModel), new HttpHeaders(), errorModel.getStatus());
	}
	
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ResultModels> handle(AccessDeniedException ex) {	
		ErrorModel errorModel = new DefaultErrorModel(CoreResultCode.FORBIDDEN, ex.getMessage());
		LOG.warn("[" + errorModel.getId() + "] ", ex);
        return new ResponseEntity<>(new ResultModels(errorModel), new HttpHeaders(), errorModel.getStatus());
    }
	
	@ExceptionHandler(CoreException.class)
	public ResponseEntity<ResultModels> handle(CoreException ex) {	
		ErrorModel errorModel = new DefaultErrorModel(CoreResultCode.INTERNAL_SERVER_ERROR, ex.getMessage(), ex.getDetails());
		LOG.error("[" + errorModel.getId() + "] ", ex);
        return new ResponseEntity<>(new ResultModels(errorModel), new HttpHeaders(), errorModel.getStatus());
    }
	
	/**
	 * Upload file size exceeded.
	 * 
	 * @param ex handled  exception
	 * @return error model
	 * @since 10.2.0
	 */
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ResultModels> handle(MaxUploadSizeExceededException ex) {
		long maxUploadSize = ex.getMaxUploadSize();
		//
		String effectiveMaxUploadSize;
		if (maxUploadSize <= 0) {
			effectiveMaxUploadSize = configurationService.getValue(
					"spring.servlet.multipart.max-file-size", 
					String.valueOf(maxUploadSize)); // -1 as default
		} else {
			effectiveMaxUploadSize = FileUtils.byteCountToDisplaySize(maxUploadSize);
		}
		//
		ErrorModel errorModel = new DefaultErrorModel(CoreResultCode.ATTACHMENT_SIZE_LIMIT_EXCEEDED, ex.getMessage(),
				ImmutableMap.of("actualSize", effectiveMaxUploadSize));
		LOG.warn("[" + errorModel.getId() + "] ", ex);
        return new ResponseEntity<>(new ResultModels(errorModel), new HttpHeaders(), errorModel.getStatus());
    }
	
	/**
	 * Optimistic lock exception - convert to result code exception.
	 * 
	 * @param ex
	 * @return
	 * @since 9.6.3
	 */
	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	public ResponseEntity<ResultModels> handle(ObjectOptimisticLockingFailureException ex) {
		ErrorModel errorModel = 
				new DefaultErrorModel(
						CoreResultCode.OPTIMISTIC_LOCK_ERROR,
						ex.getMessage(),
						ImmutableMap.of(
								"entityType", String.valueOf(ex.getPersistentClassName()),
								"entityId", ex.getIdentifier() != null ? ex.getIdentifier().toString() : ""));
		LOG.warn("[" + errorModel.getId() + "] ", ex);
        return new ResponseEntity<>(new ResultModels(errorModel), new HttpHeaders(), errorModel.getStatus());
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ResultModels> handle(Exception ex) {
		Throwable cause = Throwables.getRootCause(ex);
		// If is cause instance of ResultCodeException, then we will log exception and throw only ResultCodeException (for better show on frontend)
		if (cause instanceof ResultCodeException){
			return handle((ResultCodeException) cause);
		} else {
			ErrorModel errorModel = new DefaultErrorModel(CoreResultCode.INTERNAL_SERVER_ERROR, ex.getMessage());
			ExceptionUtils.log(LOG, errorModel, ex);
			//
			return new ResponseEntity<>(new ResultModels(errorModel), new HttpHeaders(), errorModel.getStatus());
		}
    }
	
	/**
	 * Handle exceptions from http (~ authentication) filters.
	 * 
	 * @see RestErrorAttributes
	 * @param request
	 * @param response
	 * @return error models
	 * @since 10.7.0
	 */
	@RequestMapping(path = { "/error" })
	public ResponseEntity<?> handleError(HttpServletRequest request, HttpServletResponse response) {
		Object exception = request.getAttribute(javax.servlet.RequestDispatcher.ERROR_EXCEPTION);
		//
		if (exception == null || !(exception instanceof Throwable)) {
			// if source exception is not set (e.g. from controller security ~ forbidden), return base error attributes.
			RestErrorAttributes attributes = new RestErrorAttributes();
			attributes.resolveException(request, response, null, null);
			Map<String, Object> errorAttributes = attributes.getErrorAttributes(new ServletWebRequest(request, response), false);
			//
			// fill http status
			HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			if (errorAttributes.containsKey(RestErrorAttributes.ATTRIBUTE_STATUS)) {
				int statusCode = (int) errorAttributes.get(RestErrorAttributes.ATTRIBUTE_STATUS);
				httpStatus = HttpStatus.valueOf(statusCode);
			}
			//
			// move error as IdM errors
			if (errorAttributes.containsKey(RestErrorAttributes.ATTRIBUTE_ERROR)) {
				errorAttributes.put(
						ResultModels.ATTRIBUTE_ERRORS, 
						Lists.newArrayList(errorAttributes.get(RestErrorAttributes.ATTRIBUTE_ERROR))
				);
				errorAttributes.remove(RestErrorAttributes.ATTRIBUTE_ERROR);
			}
			//
			// Return original error attributes => to cover all cases, when even "unknown" exception can be thrown (without our error).
			return new ResponseEntity<>(errorAttributes, createHttpHeaders(request, response), httpStatus);
		}
		//
		Throwable cause = Throwables.getRootCause((Throwable) exception);
		HttpHeaders httpHeaders = createHttpHeaders(request, response);
		//
		if (cause instanceof ResultCodeException) {
			ResultCodeException resultCodeException = (ResultCodeException) cause;
			ExceptionUtils.log(LOG, resultCodeException);
			//
			return new ResponseEntity<>(resultCodeException.getError(), httpHeaders, resultCodeException.getStatus());
		}
		//
		ErrorModel errorModel = new DefaultErrorModel(CoreResultCode.INTERNAL_SERVER_ERROR, cause.getMessage());
		ExceptionUtils.log(LOG, errorModel, cause);
		//
		return new ResponseEntity<>(new ResultModels(errorModel), httpHeaders, errorModel.getStatus());
	}
	
	@Override
	public String getErrorPath() {
		return null;
	}
	
	/**
	 * TODO: CorsProcessor should be registered before filters with exception => this method can be removed after.
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@SuppressWarnings("resource")
	private HttpHeaders createHttpHeaders(HttpServletRequest request, HttpServletResponse response) {
		// cors is not required
		if (!CorsUtils.isCorsRequest(request)) {
			return new HttpHeaders();
		}
		// already filled
		ServletServerHttpResponse serverResponse = new ServletServerHttpResponse(response);
		HttpHeaders httpHeaders = serverResponse.getHeaders();
		if (responseHasCors(serverResponse)) {
			return httpHeaders;
		}
		// request is from same origin
		ServletServerHttpRequest serverRequest = new ServletServerHttpRequest(request);
		if (WebUtils.isSameOrigin(serverRequest)) {
			return httpHeaders;
		}
		//
		String requestOrigin = serverRequest.getHeaders().getOrigin();
		String allowOrigin = corsConfiguration.checkOrigin(requestOrigin);
		if (allowOrigin == null) {
			return httpHeaders;
		}
		//
		// append required cors headers
		httpHeaders.addAll(HttpHeaders.VARY, Arrays.asList(HttpHeaders.ORIGIN,
				HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS));
		httpHeaders.setAccessControlAllowOrigin(allowOrigin);
		if (Boolean.TRUE.equals(corsConfiguration.getAllowCredentials())) {
			httpHeaders.setAccessControlAllowCredentials(true);
		}
		//
		return httpHeaders;
	}
	
	private boolean responseHasCors(ServerHttpResponse response) {
		try {
			return (response.getHeaders().getAccessControlAllowOrigin() != null);
		} catch (NullPointerException npe) {
			// SPR-11919 and https://issues.jboss.org/browse/WFLY-3474
			return false;
		}
	}
}