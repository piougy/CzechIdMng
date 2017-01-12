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
 * <i>SysRoleSystemAttribute</i> is responsible for mapping attribute to target resource for connected role
 * 
 * @author svandav
 *
 */
@Entity
@Table(name = "sys_role_system_attribute", indexes = {
		@Index(name = "ux_role_sys_atth_pname", columnList = "idm_property_name,role_system_id", unique = true),
		@Index(name = "ux_role_sys_atth_name", columnList = "name,role_system_id", unique = true)})

public class SysRoleSystemAttribute extends AbstractEntity {

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
	@JoinColumn(name = "role_system_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private SysRoleSystem roleSystem;

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "system_attr_mapping_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private SysSystemAttributeMapping systemAttributeMapping;

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
	@Column(name = "disabled_default_attribute", nullable = false)
	private boolean disabledDefaultAttribute = false;
	
	@Audited
	@Column(name = "uid", nullable = false)
	private boolean uid = false;

	@Audited
	@Lob
	@Column(name = "transform_script")
	private String transformScript;

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

	public SysRoleSystem getRoleSystem() {
		return roleSystem;
	}

	public void setRoleSystem(SysRoleSystem roleSystem) {
		this.roleSystem = roleSystem;
	}

	public SysSystemAttributeMapping getSystemAttributeMapping() {
		return systemAttributeMapping;
	}

	public void setSystemAttributeMapping(SysSystemAttributeMapping systemAttributeMapping) {
		this.systemAttributeMapping = systemAttributeMapping;
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

	public String getTransformScript() {
		return transformScript;
	}

	public void setTransformScript(String transformScript) {
		this.transformScript = transformScript;
	}

	public boolean isDisabledDefaultAttribute() {
		return disabledDefaultAttribute;
	}

	public void setDisabledDefaultAttribute(boolean disabledDefaultAttribute) {
		this.disabledDefaultAttribute = disabledDefaultAttribute;
	}
	
}
