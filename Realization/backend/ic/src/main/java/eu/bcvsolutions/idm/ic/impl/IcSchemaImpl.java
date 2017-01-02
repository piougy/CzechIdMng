    package eu.bcvsolutions.idm.ic.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.bcvsolutions.idm.ic.api.IcObjectClassInfo;
import eu.bcvsolutions.idm.ic.api.IcSchema;

/**
 * Schema for connector
 * @author svandav
 *
 */
public class IcSchemaImpl implements IcSchema {

	private List<IcObjectClassInfo> declaredObjectClasses = new ArrayList<>();
	private Map<String, List<String>> supportedObjectClassesByOperation = new HashMap<>();

	@Override
	public List<IcObjectClassInfo> getDeclaredObjectClasses() {
		return declaredObjectClasses;
	}

	public void setDeclaredObjectClasses(List<IcObjectClassInfo> declaredObjectClasses) {
		this.declaredObjectClasses = declaredObjectClasses;
	}

	@Override
	public Map<String, List<String>> getSupportedObjectClassesByOperation() {
		return supportedObjectClassesByOperation;
	}

	public void setSupportedObjectClassesByOperation(
			Map<String, List<String>> supportedObjectClassesByOperation) {
		this.supportedObjectClassesByOperation = supportedObjectClassesByOperation;
	}

}
