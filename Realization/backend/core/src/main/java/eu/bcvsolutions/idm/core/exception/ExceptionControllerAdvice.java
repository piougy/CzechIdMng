package eu.bcvsolutions.idm.core.exception;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import eu.bcvsolutions.idm.InitApplication;
import eu.bcvsolutions.idm.core.security.exception.IdmAuthenticationException;

@ControllerAdvice
public class ExceptionControllerAdvice {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InitApplication.class);
	
	@ExceptionHandler(RestApplicationException.class)
    ResponseEntity<RestErrors> handle(RestApplicationException ex) {
		if (ex.getStatus().is5xxServerError()) {
			log.error("[" + ex.getId() + "] ", ex);
		} else if(ex.getStatus().is2xxSuccessful()) {
			// nothing
			// TODO: refactor restApplicationException to different types or reimplement 2xx event throwing mechanism
		} else {
			log.warn("[" + ex.getId() + "] ", ex);
		}
		return new ResponseEntity<>(ex.getError(), new HttpHeaders(), ex.getStatus());
	}
	
	@ExceptionHandler(IdmAuthenticationException.class)
	ResponseEntity<RestErrors> handle(IdmAuthenticationException ex) {
		ErrorModel errorModel = new DefaultErrorModel(CoreResultCode.AUTH_FAILED, new Object[]{ }); // source exception message is shown only in log 
		log.warn("[" + errorModel.getId() + "] ", ex);
        return new ResponseEntity<>(new RestErrors(errorModel), new HttpHeaders(), HttpStatus.UNAUTHORIZED);
    }
	
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	ResponseEntity<RestErrors> handle(HttpRequestMethodNotSupportedException ex) {
		ErrorModel errorModel = new DefaultErrorModel(CoreResultCode.METHOD_NOT_ALLOWED, ex.getMessage(), new Object[]{ ex.getMethod(), MessageFormat.format("Supported methods are: {0}", StringUtils.join(ex.getSupportedMethods())) });
		log.warn("[" + errorModel.getId() + "] ", ex);
        return new ResponseEntity<>(new RestErrors(errorModel), new HttpHeaders(), HttpStatus.METHOD_NOT_ALLOWED);
    }
	
	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<RestErrors> handle(HttpMessageNotReadableException ex) {
		ErrorModel errorModel = new DefaultErrorModel(CoreResultCode.BAD_REQUEST, ex.getMessage(), new Object[]{ });
		log.warn("[" + errorModel.getId() + "] ", ex);
        return new ResponseEntity<>(new RestErrors(errorModel), new HttpHeaders(), HttpStatus.METHOD_NOT_ALLOWED);
    }
	
	@ExceptionHandler(RepositoryConstraintViolationException.class)
	ResponseEntity<RestErrors> handle(RepositoryConstraintViolationException ex) {		
		List<ErrorModel> errorModels = ex.getErrors().getFieldErrors().stream()
			.map(fieldError -> new FieldErrorModel(fieldError))
			.peek(errorModel -> log.warn("[" + errorModel.getId() + "] ", ex))
			.collect(Collectors.toList());
		// TODO: global errors
		// TODO: better errorModel logging - move source exception to errorModel?		
        return new ResponseEntity<>(new RestErrors(errorModels), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<RestErrors> handle(MethodArgumentNotValidException ex) {		
		List<ErrorModel> errorModels = ex.getBindingResult().getFieldErrors().stream()
			.map(fieldError -> new FieldErrorModel(fieldError))
			.peek(errorModel -> log.warn("[" + errorModel.getId() + "] ", ex))
			.collect(Collectors.toList());
		// TODO: global errors
		// TODO: better errorModel logging - move source exception to errorModel?	
        return new ResponseEntity<>(new RestErrors(errorModels), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
	
	@ExceptionHandler(CoreException.class)
	ResponseEntity<RestErrors> handle(CoreException ex) {	
		ErrorModel errorModel = new DefaultErrorModel(CoreResultCode.INTERNAL_SERVER_ERROR, ex.getMessage(), ex.getDetails());
		log.error("[" + errorModel.getId() + "] ", ex);
        return new ResponseEntity<>(new RestErrors(errorModel), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
	
	@ExceptionHandler(Exception.class)
	ResponseEntity<RestErrors> handle(Exception ex) {
		ErrorModel errorModel = new DefaultErrorModel(CoreResultCode.INTERNAL_SERVER_ERROR, ex.getMessage());
		log.error("[" + errorModel.getId() + "] ", ex);
        return new ResponseEntity<>(new RestErrors(errorModel), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}