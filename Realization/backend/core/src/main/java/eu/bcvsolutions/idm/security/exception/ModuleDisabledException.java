package eu.bcvsolutions.idm.security.exception;

import eu.bcvsolutions.idm.core.exception.CoreException;

public class ModuleDisabledException extends CoreException {
	
	private static final long serialVersionUID = 1L;
	private final String moduleId;	
	
	public ModuleDisabledException(String moduleId) {
		super("Module [" + moduleId + "] is disabled");
		this.moduleId = moduleId;
	}
	
	public String getModuleId() {
		return moduleId;
	}

}
