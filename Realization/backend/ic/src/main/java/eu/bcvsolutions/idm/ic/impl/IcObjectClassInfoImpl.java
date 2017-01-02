package eu.bcvsolutions.idm.ic.impl;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.ic.api.IcAttributeInfo;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcObjectClassInfo;

/**
 * Defines type or category of connector object. Unlike {@link IcObjectClass}
 * describing definitions of attributes.
 * 
 * @author svandav
 *
 */
public class IcObjectClassInfoImpl implements IcObjectClassInfo {

	private String type;
	private List<IcAttributeInfo> attributeInfos;
	private boolean isContainer;
	private boolean isAuxiliary;

	@Override
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public List<IcAttributeInfo> getAttributeInfos() {
		if(attributeInfos == null){
			attributeInfos = new ArrayList<>();
		}
		return attributeInfos;
	}

	public void setAttributeInfos(List<IcAttributeInfo> attributeInfos) {
		this.attributeInfos = attributeInfos;
	}

	@Override
	public boolean isContainer() {
		return isContainer;
	}

	public void setContainer(boolean isContainer) {
		this.isContainer = isContainer;
	}

	@Override
	public boolean isAuxiliary() {
		return isAuxiliary;
	}

	public void setAuxiliary(boolean isAuxiliary) {
		this.isAuxiliary = isAuxiliary;
	}

}
