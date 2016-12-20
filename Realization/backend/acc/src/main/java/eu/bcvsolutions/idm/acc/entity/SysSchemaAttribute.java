package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
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
 * <i>SysSchemaAttribute</i> is meta data responsible for describing an
 * supported resource attributes
 * 
 * @author svandav
 *
 */
@Entity
@Table(name = "sys_schema_attribute", indexes = {
		@Index(name = "ux_schema_att_name_objclass", columnList = "name,object_class_id", unique = true)})

public class SysSchemaAttribute extends AbstractEntity {

	private static final long serialVersionUID = -8492560756893726050L;

	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false)
	private String name;

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "object_class_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private SysSchemaObjectClass objectClass;

	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "class_type", length = DefaultFieldLengths.NAME, nullable = false)
	private String classType;

	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "native_name", length = DefaultFieldLengths.NAME, nullable = true)
	private String nativeName;

	@Audited
	@Column(name = "required", nullable = false)
	private boolean required = false;

	@Audited
	@Column(name = "multivalued", nullable = false)
	private boolean multivalued = false;

	@Audited
	@Column(name = "createable", nullable = false)
	private boolean createable = false;

	@Audited
	@Column(name = "updateable", nullable = false)
	private boolean updateable = false;

	@Audited
	@Column(name = "readable", nullable = false)
	private boolean readable = false;

	@Audited
	@Column(name = "returned_by_default", nullable = false)
	private boolean returnedByDefault = false;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SysSchemaObjectClass getObjectClass() {
		return objectClass;
	}

	public void setObjectClass(SysSchemaObjectClass objectClass) {
		this.objectClass = objectClass;
	}

	public String getClassType() {
		return classType;
	}

	public void setClassType(String classType) {
		this.classType = classType;
	}

	public String getNativeName() {
		return nativeName;
	}

	public void setNativeName(String nativeName) {
		this.nativeName = nativeName;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isMultivalued() {
		return multivalued;
	}

	public void setMultivalued(boolean multivalued) {
		this.multivalued = multivalued;
	}

	public boolean isCreateable() {
		return createable;
	}

	public void setCreateable(boolean createable) {
		this.createable = createable;
	}

	public boolean isUpdateable() {
		return updateable;
	}

	public void setUpdateable(boolean updateable) {
		this.updateable = updateable;
	}

	public boolean isReadable() {
		return readable;
	}

	public void setReadable(boolean readable) {
		this.readable = readable;
	}

	public boolean isReturnedByDefault() {
		return returnedByDefault;
	}

	public void setReturnedByDefault(boolean returnedByDefault) {
		this.returnedByDefault = returnedByDefault;
	}

}
