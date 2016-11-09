package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;

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
@Table(name = "sys_schema_attribute_handling")
public class SysSchemaAttributeHandling extends AbstractEntity {

	private static final long serialVersionUID = -8492560756893726050L;

	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "idm_property_name", length = DefaultFieldLengths.NAME, nullable = false)
	private String idmPropertyName;

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "schema_attribute_id", referencedColumnName = "id")
	private SysSchemaAttribute schemaAttribute;
	
	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "system_entity_handling_id", referencedColumnName = "id")
	private SysSystemEntityHandling systemEntityHandling;

	@Audited
	@Column(name = "extended_attribute", nullable = false)
	private boolean extendedAttribute = false;

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

}
