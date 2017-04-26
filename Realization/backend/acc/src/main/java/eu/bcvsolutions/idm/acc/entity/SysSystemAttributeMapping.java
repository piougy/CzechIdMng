package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.validation.constraints.NotNull;

import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmScript;

/**
 * <i>SysSystemAttributeMapping</i> is responsible for mapping schema attribute
 * to idm entity
 * 
 * @author svandav
 *
 */
@Entity
@Table(name = "sys_system_attribute_mapping", indexes = {
		@Index(name = "ux_sys_attr_m_attr", columnList = "system_mapping_id,schema_attribute_id,strategy_type", unique = true),
		@Index(name = "ux_sys_attr_m_name_enth", columnList = "name,system_mapping_id", unique = true)})
public class SysSystemAttributeMapping extends AbstractEntity implements AttributeMapping {

	private static final long serialVersionUID = -8492560756893726050L;

	@Audited
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false)
	private String name;

	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "idm_property_name", length = DefaultFieldLengths.NAME, nullable = true)
	private String idmPropertyName;
	
	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "system_mapping_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private SysSystemMapping systemMapping;

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "schema_attribute_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private SysSchemaAttribute schemaAttribute;

	@Audited
	@Column(name = "disabled_attribute", nullable = false)
	private boolean disabledAttribute = false;
	
	@Audited
	@Column(name = "extended_attribute", nullable = false)
	private boolean extendedAttribute = false;

	@Audited
	@Column(name = "entity_attribute", nullable = false)
	private boolean entityAttribute = true;

	@Audited
	@Column(name = "confidential_attribute", nullable = false)
	private boolean confidentialAttribute = false;

	@Audited
	@Column(name = "uid", nullable = false)
	private boolean uid = false;

	@Audited
	@Type(type = "org.hibernate.type.StringClobType") // TODO: test on oracle/ mysql
	@Column(name = "transform_from_res_script")
	private String transformFromResourceScript;

	@Audited
	@Type(type = "org.hibernate.type.StringClobType") // TODO: test on oracle/ mysql
	@Column(name = "transform_to_res_script")
	private String transformToResourceScript;
	
	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "strategy_type", nullable = false)
	private AttributeMappingStrategyType strategyType = AttributeMappingStrategyType.SET;

	@Audited
	@Column(name = "send_always", nullable = false)
	private boolean sendAlways = false;
	
	@Audited
	@Column(name = "send_only_if_not_null", nullable = false)
	private boolean sendOnlyIfNotNull = false;
	
	@Audited
	@Column(name = "authentication_attribute", nullable = false)
	private boolean authenticationAttribute = false;
	
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

	public SysSystemMapping getSystemMapping() {
		return systemMapping;
	}

	public void setSystemMapping(SysSystemMapping systemMapping) {
		this.systemMapping = systemMapping;
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
		return disabledAttribute;
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

	public boolean isAuthenticationAttribute() {
		return authenticationAttribute;
	}

	public void setAuthenticationAttribute(boolean authenticationAttribute) {
		this.authenticationAttribute = authenticationAttribute;
	}
}
