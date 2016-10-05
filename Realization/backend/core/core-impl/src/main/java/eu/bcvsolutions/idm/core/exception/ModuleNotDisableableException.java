package eu.bcvsolutions.idm.core.exception;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;

public class ModuleNotDisableableException extends ResultCodeException {
	
	private static final long serialVersionUID = 1L;
	private final String moduleId;	
	
	public ModuleNotDisableableException(String moduleId) {
		super(CoreResultCode.MODULE_NOT_DISABLEABLE, ImmutableMap.of("module", moduleId));
		this.moduleId = moduleId;
	}
	
	public String getModuleId() {
		return moduleId;
	}

}
