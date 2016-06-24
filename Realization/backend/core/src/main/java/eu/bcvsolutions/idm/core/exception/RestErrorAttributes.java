package eu.bcvsolutions.idm.core.exception;

import java.util.Map;

import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.web.context.request.RequestAttributes;

/**
 * We want return the same error everywhere. This class overrides default spring errors (4xx)
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
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
            		errorModel = new DefaultErrorModel(CoreResultCode.LOG_IN, new Object[]{errorAttributes.get("path")});
            		break;
            	}
            	case 403: {
            		errorModel = new DefaultErrorModel(CoreResultCode.FORBIDDEN, new Object[]{errorAttributes.get("path"), errorAttributes.get("message")});
            		break;
            	}
            	case 404: {
            		errorModel = new DefaultErrorModel(CoreResultCode.ENDPOINT_NOT_FOUND, new Object[]{errorAttributes.get("path"), errorAttributes.get("message")});
            		break;
            	}
            	case 400:
            	case 405: {
            		errorModel = new DefaultErrorModel(CoreResultCode.METHOD_NOT_ALLOWED, new Object[]{errorAttributes.get("path"), errorAttributes.get("message")});
            		break;
            	}
            }     
        }	            
        if (errorModel == null) {
        	log.error("Error not resolved - errorAttributes needs extension for error attrs [{}]", errorAttributes);
        	return errorAttributes;
        }
        errorAttributes.clear();
        errorAttributes.put("error", errorModel);
        return errorAttributes;	            
    }
}
