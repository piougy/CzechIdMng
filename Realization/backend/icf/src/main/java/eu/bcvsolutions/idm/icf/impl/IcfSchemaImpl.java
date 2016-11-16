    package eu.bcvsolutions.idm.icf.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.bcvsolutions.idm.icf.api.IcfObjectClassInfo;
import eu.bcvsolutions.idm.icf.api.IcfSchema;

/**
 * Schema for connector
 * @author svandav
 *
 */
public class IcfSchemaImpl implements IcfSchema {

	private List<IcfObjectClassInfo> declaredObjectClasses = new ArrayList<>();
	private Map<String, List<String>> supportedObjectClassesByOperation = new HashMap<>();

	@Override
	public List<IcfObjectClassInfo> getDeclaredObjectClasses() {
		return declaredObjectClasses;
	}

	public void setDeclaredObjectClasses(List<IcfObjectClassInfo> declaredObjectClasses) {
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
