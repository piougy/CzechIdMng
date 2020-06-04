package eu.bcvsolutions.idm.core.exception;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.config.ModelMapperChecker;

/**
 * Check model mapper is properly initialized to prevent:
 * org.modelmapper.MappingException: ModelMapper mapping errors: Converter org.modelmapper.internal.converter.CollectionConverter@7214dbf8 failed to convert 
 * 
 * @author Radek Tomiška
 * @see ModelMapperChecker
 * @deprecated @since 10.4.0 ModelMapper updated to new version 2.3.7, where issue above is fixed
 */
@Deprecated
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
