package eu.bcvsolutions.idm.core.exception;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.config.ModelMapperChecker;

/**
 * Configuration property is disabled
 * 
 * @author Radek Tomiška
 * @see ModelMapperChecker
 */
public class ModelMapperServiceInitException extends ResultCodeException  {

	private static final long serialVersionUID = 1L;
	private final String service;

	public ModelMapperServiceInitException(String service, Exception ex) {
		super(CoreResultCode.MODEL_MAPPER_SERVICE_INIT_FAILED, ImmutableMap.of("service", service), ex);
		//
		this.service = service;
	}

	public String getService() {
		return service;
	}

}
