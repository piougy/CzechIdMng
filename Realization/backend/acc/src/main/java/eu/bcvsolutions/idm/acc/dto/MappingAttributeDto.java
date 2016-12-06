package eu.bcvsolutions.idm.acc.dto;

import eu.bcvsolutions.idm.acc.domain.MappingAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;

/**
 * Dto for mapped attribute. Used for overloading schema attribute handling in provisioning
 * @author svandav
 *
 */
public class MappingAttributeDto  implements MappingAttribute {


	private String name;
	private String idmPropertyName;
	private SysSchemaAttribute schemaAttribute;
	private boolean extendedAttribute = false;
	private boolean entityAttribute = true;
	private boolean confidentialAttribute = true;
	private boolean uid = false;
	private String transformFromResourceScript;
	private String transformToResourceScript;

	@Override
	public String getIdmPropertyName() {
		return idmPropertyName;
	}

	@Override
	public void setIdmPropertyName(String idmPropertyName) {
		this.idmPropertyName = idmPropertyName;
	}

	@Override
	public SysSchemaAttribute getSchemaAttribute() {
		return schemaAttribute;
	}

	@Override
	public void setSchemaAttribute(SysSchemaAttribute schemaAttribute) {
		this.schemaAttribute = schemaAttribute;
	}

	@Override
	public boolean isExtendedAttribute() {
		return extendedAttribute;
	}

	@Override
	public void setExtendedAttribute(boolean extendedAttribute) {
		this.extendedAttribute = extendedAttribute;
	}

	@Override
	public String getTransformFromResourceScript() {
		return transformFromResourceScript;
	}

	@Override
	public void setTransformFromResourceScript(String transformFromResourceScript) {
		this.transformFromResourceScript = transformFromResourceScript;
	}

	@Override
	public String getTransformToResourceScript() {
		return transformToResourceScript;
	}

	@Override
	public void setTransformToResourceScript(String transformToResourceScript) {
		this.transformToResourceScript = transformToResourceScript;
	}

	@Override
	public boolean isUid() {
		return uid;
	}

	@Override
	public void setUid(boolean uid) {
		this.uid = uid;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean isEntityAttribute() {
		return entityAttribute;
	}

	@Override
	public void setEntityAttribute(boolean entityAttribute) {
		this.entityAttribute = entityAttribute;
	}

	@Override
	public boolean isConfidentialAttribute() {
		return confidentialAttribute;
	}

	@Override
	public void setConfidentialAttribute(boolean confidentialAttribute) {
		this.confidentialAttribute = confidentialAttribute;
	}
}
