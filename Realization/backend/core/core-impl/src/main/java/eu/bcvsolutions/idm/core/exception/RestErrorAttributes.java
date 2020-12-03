package eu.bcvsolutions.idm.core.exception;

import java.util.Map;

import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.web.context.request.WebRequest;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.DefaultErrorModel;
import eu.bcvsolutions.idm.core.api.exception.ErrorModel;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;

/**
 * We want return the same error everywhere. This class overrides default spring errors (4xx)
 * 
 * @author Radek Tomiška 
 *
 */
public class RestErrorAttributes extends DefaultErrorAttributes {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RestErrorAttributes.class);
	//
	public static final String ATTRIBUTE_STATUS = "status";
	public static final String ATTRIBUTE_ERROR = "error";
	
	@Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
		Throwable error = getError(webRequest);
		Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, includeStackTrace);
		//
		ErrorModel errorModel = null;
		if (error instanceof ResultCodeException) {
			errorModel = ((ResultCodeException) error).getError().getError();
		} else {
	        if (errorAttributes.containsKey(ATTRIBUTE_STATUS)) {
	        	int status = (int) errorAttributes.get(ATTRIBUTE_STATUS);
	            switch(status) {
	            	case 401: {
	            		errorModel = new DefaultErrorModel(CoreResultCode.LOG_IN, ImmutableMap.of("path", errorAttributes.get("path")));
	            		break;
	            	}
	            	case 403: {
	            		errorModel = new DefaultErrorModel(CoreResultCode.FORBIDDEN, ImmutableMap.of("path", errorAttributes.get("path"), "message", errorAttributes.get("message")));
	            		break;
	            	}
	            	case 404: {
	            		errorModel = new DefaultErrorModel(CoreResultCode.ENDPOINT_NOT_FOUND, ImmutableMap.of("path", errorAttributes.get("path"), "message", errorAttributes.get("message")));
	            		break;
	            	}
	            	case 400:
	            	case 405: {
	            		errorModel = new DefaultErrorModel(CoreResultCode.METHOD_NOT_ALLOWED, ImmutableMap.of("path", errorAttributes.get("path"), "message", errorAttributes.get("message")));
	            		break;
	            	}
	            	default: {
	            		errorModel = null;
	            	}
	            }     
	        }
		}
        if (errorModel == null) {
        	LOG.error("Error not resolved - errorAttributes needs extension for error attrs [{}]", errorAttributes);
        	return errorAttributes;
        }
        errorAttributes.put(ATTRIBUTE_ERROR, errorModel);
        LOG.warn(errorModel.toString());
		//
        return errorAttributes;	            
    }
}
