package eu.bcvsolutions.idm.core.exception;

import eu.bcvsolutions.idm.core.exception.CoreException;

public class ModuleNotDisableableException extends CoreException {
	
	private static final long serialVersionUID = 1L;
	private final String moduleId;	
	
	public ModuleNotDisableableException(String moduleId) {
		super("Module [" + moduleId + "] is not disableable");
		this.moduleId = moduleId;
	}
	
	public String getModuleId() {
		return moduleId;
	}

}
