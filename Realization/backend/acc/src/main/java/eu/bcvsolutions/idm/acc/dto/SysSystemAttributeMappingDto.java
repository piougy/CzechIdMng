package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

@Relation(collectionRelation = "systemAttributeMappings")
public class SysSystemAttributeMappingDto extends AbstractDto implements AttributeMapping {

	private static final long serialVersionUID = 8415424250836600530L;
	
	private String name;
	private String idmPropertyName;
	@Embedded(dtoClass = SysSystemMappingDto.class)
	private UUID systemMapping;
	@Embedded(dtoClass = SysSchemaAttributeDto.class)
	private UUID schemaAttribute;
	private boolean disabledAttribute = false;
	private boolean extendedAttribute = false;
	private boolean entityAttribute = true;
	private boolean confidentialAttribute = false;
	private boolean uid = false;
	private String transformFromResourceScript;
	private String transformToResourceScript;
	private AttributeMappingStrategyType strategyType = AttributeMappingStrategyType.SET;
	private boolean sendAlways = false;
	private boolean sendOnlyIfNotNull = false;
	private boolean authenticationAttribute = false;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdmPropertyName() {
		return idmPropertyName;
	}

	public void setIdmPropertyName(String idmPropertyName) {
		this.idmPropertyName = idmPropertyName;
	}

	public UUID getSystemMapping() {
		return systemMapping;
	}

	public void setSystemMapping(UUID systemMapping) {
		this.systemMapping = systemMapping;
	}

	public UUID getSchemaAttribute() {
		return schemaAttribute;
	}

	public void setSchemaAttribute(UUID schemaAttribute) {
		this.schemaAttribute = schemaAttribute;
	}

	public boolean isDisabledAttribute() {
		return disabledAttribute;
	}

	public void setDisabledAttribute(boolean disabledAttribute) {
		this.disabledAttribute = disabledAttribute;
	}

	public boolean isExtendedAttribute() {
		return extendedAttribute;
	}

	public void setExtendedAttribute(boolean extendedAttribute) {
		this.extendedAttribute = extendedAttribute;
	}

	public boolean isEntityAttribute() {
		return entityAttribute;
	}

	public void setEntityAttribute(boolean entityAttribute) {
		this.entityAttribute = entityAttribute;
	}

	public boolean isConfidentialAttribute() {
		return confidentialAttribute;
	}

	public void setConfidentialAttribute(boolean confidentialAttribute) {
		this.confidentialAttribute = confidentialAttribute;
	}

	public boolean isUid() {
		return uid;
	}

	public void setUid(boolean uid) {
		this.uid = uid;
	}

	public String getTransformFromResourceScript() {
		return transformFromResourceScript;
	}

	public void setTransformFromResourceScript(String transformFromResourceScript) {
		this.transformFromResourceScript = transformFromResourceScript;
	}

	public String getTransformToResourceScript() {
		return transformToResourceScript;
	}

	public void setTransformToResourceScript(String transformToResourceScript) {
		this.transformToResourceScript = transformToResourceScript;
	}

	public AttributeMappingStrategyType getStrategyType() {
		return strategyType;
	}

	public void setStrategyType(AttributeMappingStrategyType strategyType) {
		this.strategyType = strategyType;
	}

	public boolean isSendAlways() {
		return sendAlways;
	}

	public void setSendAlways(boolean sendAlways) {
		this.sendAlways = sendAlways;
	}

	public boolean isSendOnlyIfNotNull() {
		return sendOnlyIfNotNull;
	}

	public void setSendOnlyIfNotNull(boolean sendOnlyIfNotNull) {
		this.sendOnlyIfNotNull = sendOnlyIfNotNull;
	}

	public boolean isAuthenticationAttribute() {
		return authenticationAttribute;
	}

	public void setAuthenticationAttribute(boolean authenticationAttribute) {
		this.authenticationAttribute = authenticationAttribute;
	}

}
