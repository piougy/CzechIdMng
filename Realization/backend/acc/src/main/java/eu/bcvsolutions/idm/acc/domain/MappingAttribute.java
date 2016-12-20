package eu.bcvsolutions.idm.acc.domain;

import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;

/**
 * Interface for mapping attribute dto
 * @author svandav
 *
 */

public interface MappingAttribute {

	String getIdmPropertyName();

	void setIdmPropertyName(String idmPropertyName);

	SysSchemaAttribute getSchemaAttribute();

	void setSchemaAttribute(SysSchemaAttribute schemaAttribute);

	boolean isExtendedAttribute();

	void setExtendedAttribute(boolean extendedAttribute);

	String getTransformFromResourceScript();

	void setTransformFromResourceScript(String transformFromResourceScript);

	String getTransformToResourceScript();

	void setTransformToResourceScript(String transformToResourceScript);

	boolean isUid();

	void setUid(boolean uid);

	String getName();

	void setName(String name);

	boolean isEntityAttribute();

	void setEntityAttribute(boolean entityAttribute);

	boolean isConfidentialAttribute();

	void setConfidentialAttribute(boolean confidentialAttribute);

	boolean isDisabledAttribute();

	void setDisabledAttribute(boolean disabled);

}