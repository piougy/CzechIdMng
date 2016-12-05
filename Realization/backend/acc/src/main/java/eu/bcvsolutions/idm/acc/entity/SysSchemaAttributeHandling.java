package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;

import com.sun.istack.NotNull;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * <i>SysSchemaAttributeHandling</i> is responsible for mapping schema attribute
 * to idm entity
 * 
 * @author svandav
 *
 */
@Entity
@Table(name = "sys_schema_attribute_handling", indexes = {
		@Index(name = "ux_schema_atth_pname_enth", columnList = "idm_property_name,system_entity_handling_id", unique = true),
		@Index(name = "ux_schema_atth_name_enth", columnList = "name,system_entity_handling_id", unique = true)})

public class SysSchemaAttributeHandling extends AbstractEntity {

	private static final long serialVersionUID = -8492560756893726050L;

	@Audited
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = true)
	private String name;

	@Audited
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "idm_property_name", length = DefaultFieldLengths.NAME, nullable = true)
	private String idmPropertyName;

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "schema_attribute_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in
										// hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private SysSchemaAttribute schemaAttribute;

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "system_entity_handling_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in
										// hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private SysSystemEntityHandling systemEntityHandling;

	@Audited
	@Column(name = "extended_attribute", nullable = false)
	private boolean extendedAttribute = false;

	@Audited
	@Column(name = "entity_attribute", nullable = false)
	private boolean entityAttribute = true;

	@Audited
	@Column(name = "confidential_attribute", nullable = false)
	private boolean confidentialAttribute = true;

	@Audited
	@Column(name = "uid", nullable = false)
	private boolean uid = false;

	@Audited
	@Lob
	@Column(name = "transform_from_res_script")
	private String transformFromResourceScript;

	@Audited
	@Lob
	@Column(name = "transform_to_res_script")
	private String transformToResourceScript;

	public String getIdmPropertyName() {
		return idmPropertyName;
	}

	public void setIdmPropertyName(String idmPropertyName) {
		this.idmPropertyName = idmPropertyName;
	}

	public SysSchemaAttribute getSchemaAttribute() {
		return schemaAttribute;
	}

	public void setSchemaAttribute(SysSchemaAttribute schemaAttribute) {
		this.schemaAttribute = schemaAttribute;
	}

	public boolean isExtendedAttribute() {
		return extendedAttribute;
	}

	public void setExtendedAttribute(boolean extendedAttribute) {
		this.extendedAttribute = extendedAttribute;
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

	public SysSystemEntityHandling getSystemEntityHandling() {
		return systemEntityHandling;
	}

	public void setSystemEntityHandling(SysSystemEntityHandling systemEntityHandling) {
		this.systemEntityHandling = systemEntityHandling;
	}

	public boolean isUid() {
		return uid;
	}

	public void setUid(boolean uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
}
