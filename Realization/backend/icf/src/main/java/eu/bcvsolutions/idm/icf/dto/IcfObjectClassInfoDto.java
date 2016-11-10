package eu.bcvsolutions.idm.icf.dto;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.icf.api.IcfAttributeInfo;
import eu.bcvsolutions.idm.icf.api.IcfObjectClass;
import eu.bcvsolutions.idm.icf.api.IcfObjectClassInfo;

/**
 * Defines type or category of connector object. Unlike {@link IcfObjectClass}
 * describing definitions of attributes.
 * 
 * @author svandav
 *
 */
public class IcfObjectClassInfoDto implements IcfObjectClassInfo {

	private String type;
	private List<IcfAttributeInfo> attributeInfos;
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
	public List<IcfAttributeInfo> getAttributeInfos() {
		if(attributeInfos == null){
			attributeInfos = new ArrayList<>();
		}
		return attributeInfos;
	}

	public void setAttributeInfos(List<IcfAttributeInfo> attributeInfos) {
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
