package eu.bcvsolutions.idm.core.exception;

import java.util.Map;

import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.web.context.request.RequestAttributes;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.DefaultErrorModel;
import eu.bcvsolutions.idm.core.api.exception.ErrorModel;

/**
 * We want return the same error everywhere. This class overrides default spring errors (4xx)
 * 
 * @author Radek Tomiška 
 *
 */
public class RestErrorAttributes extends DefaultErrorAttributes {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RestErrorAttributes.class);
	
	@Override
    public Map<String, Object> getErrorAttributes(RequestAttributes requestAttributes, boolean includeStackTrace) {        	
        Map<String, Object> errorAttributes = super.getErrorAttributes(requestAttributes, includeStackTrace);
        ErrorModel errorModel = null;
        if (errorAttributes.containsKey("status")) {
        	int status = (int)errorAttributes.get("status");
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
            }     
        }	            
        if (errorModel == null) {
        	log.error("Error not resolved - errorAttributes needs extension for error attrs [{}]", errorAttributes);
        	return errorAttributes;
        }
        // we need timestamp
        // errorAttributes.clear();
        errorAttributes.put("error", errorModel);
		log.warn(errorModel.toString());
        return errorAttributes;	            
    }
}
