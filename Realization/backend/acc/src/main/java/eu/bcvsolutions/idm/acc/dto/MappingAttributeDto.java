package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;

/**
 * Dto for mapped attribute. Used for overloading schema attribute handling in provisioning
 * @author svandav
 *
 */
public class MappingAttributeDto implements AttributeMapping {

	private static final long serialVersionUID = 3813047739818544156L;
	private String name;
	private String idmPropertyName;
	private UUID schemaAttribute;
	private boolean extendedAttribute = false;
	private boolean entityAttribute = true;
	private boolean confidentialAttribute = true;
	private boolean uid = false;
	private boolean disabledAttribute = false;
	private String transformFromResourceScript;
	private String transformToResourceScript;
	private AttributeMappingStrategyType strategyType;
	private boolean sendAlways = false;
	private boolean sendOnlyIfNotNull = false;

	@Override
	public String getIdmPropertyName() {
		return idmPropertyName;
	}

	@Override
	public void setIdmPropertyName(String idmPropertyName) {
		this.idmPropertyName = idmPropertyName;
	}

	@Override
	public UUID getSchemaAttribute() {
		return schemaAttribute;
	}

	@Override
	public void setSchemaAttribute(UUID schemaAttribute) {
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

	@Override
	public boolean isDisabledAttribute() {
		return this.disabledAttribute;
	}

	@Override
	public void setDisabledAttribute(boolean disabledAttribute) {
		this.disabledAttribute = disabledAttribute;
	}

	@Override
	public AttributeMappingStrategyType getStrategyType() {
		return strategyType;
	}

	@Override
	public void setStrategyType(AttributeMappingStrategyType strategyType) {
		this.strategyType = strategyType;
	}
	
	@Override
	public boolean isSendAlways() {
		return sendAlways;
	}

	@Override
	public void setSendAlways(boolean sendAlways) {
		this.sendAlways = sendAlways;
	}

	@Override
	public boolean isSendOnlyIfNotNull() {
		return sendOnlyIfNotNull;
	}

	@Override
	public void setSendOnlyIfNotNull(boolean sendOnlyIfNotNull) {
		this.sendOnlyIfNotNull = sendOnlyIfNotNull;
	}
	
}
