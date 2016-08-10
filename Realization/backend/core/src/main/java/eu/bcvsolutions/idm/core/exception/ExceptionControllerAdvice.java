package eu.bcvsolutions.idm.core.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.model.dto.ResultModels;
import eu.bcvsolutions.idm.core.security.exception.IdmAuthenticationException;

@ControllerAdvice
public class ExceptionControllerAdvice {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExceptionControllerAdvice.class);
	
	@ExceptionHandler(RestApplicationException.class)
    ResponseEntity<ResultModels> handle(RestApplicationException ex) {
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
	ResponseEntity<ResultModels> handle(IdmAuthenticationException ex) {
		ErrorModel errorModel = new DefaultErrorModel(CoreResultCode.AUTH_FAILED);
		 // source exception message is shown only in log 
		log.warn("[" + errorModel.getId() + "] ", ex);
        return new ResponseEntity<>(new ResultModels(errorModel), new HttpHeaders(), errorModel.getStatus());
    }
	
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	ResponseEntity<ResultModels> handle(HttpRequestMethodNotSupportedException ex) {
		ErrorModel errorModel = new DefaultErrorModel(CoreResultCode.METHOD_NOT_ALLOWED, ex.getMessage(),
				ImmutableMap.of( //
						"errorMethod", ex.getMethod(), //
						"supportedMethods", StringUtils.join(ex.getSupportedMethods(), ", ")));
		log.warn("[" + errorModel.getId() + "] ", ex);
		return new ResponseEntity<>(new ResultModels(errorModel), new HttpHeaders(), errorModel.getStatus());
	}
	
	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ResultModels> handle(HttpMessageNotReadableException ex) {
		ErrorModel errorModel = new DefaultErrorModel(CoreResultCode.METHOD_NOT_ALLOWED, ex.getMessage());
		log.warn("[" + errorModel.getId() + "] ", ex);
        return new ResponseEntity<>(new ResultModels(errorModel), new HttpHeaders(), errorModel.getStatus());
    }
	
	@ExceptionHandler(RepositoryConstraintViolationException.class)
	ResponseEntity<ResultModels> handle(RepositoryConstraintViolationException ex) {
		List<ErrorModel> errorModels = ex.getErrors().getFieldErrors().stream()
			.map(fieldError -> new FieldErrorModel(fieldError))
			.peek(errorModel -> log.warn("[" + errorModel.getId() + "] ", ex))
			.collect(Collectors.toList());
		// TODO: global errors
		// TODO: better errorModel logging - move source exception to errorModel?		
        return new ResponseEntity<>(new ResultModels(errorModels), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ResultModels> handle(MethodArgumentNotValidException ex) {		
		List<ErrorModel> errorModels = ex.getBindingResult().getFieldErrors().stream()
			.map(fieldError -> new FieldErrorModel(fieldError))
			.peek(errorModel -> log.warn("[" + errorModel.getId() + "] ", ex))
			.collect(Collectors.toList());
		// TODO: global errors
		// TODO: better errorModel logging - move source exception to errorModel?	
        return new ResponseEntity<>(new ResultModels(errorModels), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
	
	@ExceptionHandler(DataIntegrityViolationException.class)
	ResponseEntity<ResultModels> handle(DataIntegrityViolationException ex) {		
		ErrorModel errorModel = new DefaultErrorModel(CoreResultCode.CONFLICT, ex.getMessage());
		log.error("[" + errorModel.getId() + "] ", ex);
		return new ResponseEntity<>(new ResultModels(errorModel), new HttpHeaders(), errorModel.getStatus());
    }
	
	
	@ExceptionHandler(CoreException.class)
	ResponseEntity<ResultModels> handle(CoreException ex) {	
		ErrorModel errorModel = new DefaultErrorModel(CoreResultCode.INTERNAL_SERVER_ERROR, ex.getMessage(), ex.getDetails());
		log.error("[" + errorModel.getId() + "] ", ex);
        return new ResponseEntity<>(new ResultModels(errorModel), new HttpHeaders(), errorModel.getStatus());
    }
	
	@ResponseBody
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	ResultModels handle(Exception ex) {
		ErrorModel errorModel = new DefaultErrorModel(CoreResultCode.INTERNAL_SERVER_ERROR, ex.getMessage());
		log.error("[" + errorModel.getId() + "] ", ex);
        return new ResultModels(errorModel);
    }
}