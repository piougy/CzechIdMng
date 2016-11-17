package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
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
		@Index(name = "ux_schema_atth_name_enth", columnList = "idm_property_name,system_entity_handling_id", unique = true)})

public class SysSchemaAttributeHandling extends AbstractEntity {

	private static final long serialVersionUID = -8492560756893726050L;

	@Audited
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "idm_property_name", length = DefaultFieldLengths.NAME, nullable = true)
	private String idmPropertyName;

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "schema_attribute_id", referencedColumnName = "id")
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private SysSchemaAttribute schemaAttribute;

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "system_entity_handling_id", referencedColumnName = "id")
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private SysSystemEntityHandling systemEntityHandling;

	@Audited
	@Column(name = "extended_attribute", nullable = false)
	private boolean extendedAttribute = false;
	
	@Audited
	@Column(name = "uid", nullable = false)
	private boolean uid = false;

	@Audited
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "transform_from_system", length = DefaultFieldLengths.NAME)
	private String transformFromSystem;

	@Audited
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "transform_to_system", length = DefaultFieldLengths.NAME)
	private String transformToSystem;

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

	public String getTransformFromSystem() {
		return transformFromSystem;
	}

	public void setTransformFromSystem(String transformFromSystem) {
		this.transformFromSystem = transformFromSystem;
	}

	public String getTransformToSystem() {
		return transformToSystem;
	}

	public void setTransformToSystem(String transformToSystem) {
		this.transformToSystem = transformToSystem;
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

}
