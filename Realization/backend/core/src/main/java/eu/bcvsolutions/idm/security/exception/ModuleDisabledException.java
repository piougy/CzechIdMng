package eu.bcvsolutions.idm.security.exception;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.service.ModuleService;

/**
 * Module is disabled.
 * 
 * @author Radek Tomiška
 * 
 * @see ModuleService
 *
 */
public class ModuleDisabledException extends ResultCodeException {
	
	private static final long serialVersionUID = 1L;
	private final String moduleId;	
	
	public ModuleDisabledException(String moduleId) {
		super(CoreResultCode.MODULE_DISABLED, ImmutableMap.of("module", moduleId));
		this.moduleId = moduleId;
	}
	
	public String getModuleId() {
		return moduleId;
	}

}
