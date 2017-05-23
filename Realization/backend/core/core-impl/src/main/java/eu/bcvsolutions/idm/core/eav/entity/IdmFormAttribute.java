package eu.bcvsolutions.idm.core.eav.entity;

import javax.persistence.Basic;
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
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.UnmodifiableEntity;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;

/**
 * Single attribute definition in one form defition
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Audited
@Table(name = "idm_form_attribute", indexes = {
		@Index(name = "idx_idm_f_a_definition_def", columnList = "definition_id"),
		@Index(name = "ux_idm_f_a_definition_name", columnList = "definition_id, code", unique = true) })
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class IdmFormAttribute extends AbstractEntity implements UnmodifiableEntity, Codeable {

	private static final long serialVersionUID = 6037781154742359100L;
	//	
	@ManyToOne(optional = false)
	@JoinColumn(name = "definition_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmFormDefinition formDefinition;
	
	@NotEmpty
	@Basic(optional = false)
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "code", nullable = false, length = DefaultFieldLengths.NAME)
	private String code; 
	
	@NotEmpty
	@Basic(optional = false)
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", nullable = false, length = DefaultFieldLengths.NAME)
	private String name;
	
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "description", nullable = true)
	private String description;	
	
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "placeholder", nullable = true)
	private String placeholder;	
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "persistent_type", length = 45, nullable = false)
	private PersistentType persistentType;
	
	@NotNull
	@Column(name = "multiple", nullable = false)
	private boolean multiple;
	
	@NotNull
	@Column(name = "required", nullable = false)
	private boolean required;
	
	@NotNull
	@Column(name = "readonly", nullable = false)
	private boolean readonly;
	
	@NotNull
	@Column(name = "confidential", nullable = false)
	private boolean confidential;
	
	@Max(99999)
	@Column(name = "seq")
	private Short seq;
	
	@Type(type = "org.hibernate.type.StringClobType")
	@Column(name = "default_value", nullable = true)
	private String defaultValue;
	
	@NotNull
	@Column(name = "unmodifiable", nullable = false)
	private boolean unmodifiable = false;

	public IdmFormAttribute() {
	}
	
	/**
	 * Code / key - unique in one form defifinition
	 */
	@Override
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * User friendly name (label)
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Form definition
	 * 
	 * @return
	 */
	public IdmFormDefinition getFormDefinition() {
		return formDefinition;
	}
	
	public void setFormDefinition(IdmFormDefinition formDefinition) {
		this.formDefinition = formDefinition;
	}

	/**
	 * Data type
	 * 
	 * @return
	 */
	public PersistentType getPersistentType() {
		return persistentType;
	}

	public void setPersistentType(PersistentType persistentType) {
		this.persistentType = persistentType;
	}

	/**
	 * Multi values (list)
	 * @return
	 */
	public boolean isMultiple() {
		return multiple;
	}

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	/**
	 * Required attribute
	 * 
	 * @return
	 */
	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	/**
	 * Order on FE form
	 * 
	 * @return
	 */
	public Short getSeq() {
		return seq;
	}

	public void setSeq(Short seq) {
		this.seq = seq;
	}

	/**
	 * Attribute cannot be changed by user 
	 * 
	 * @return
	 */
	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}	

	/**
	 * User friendly description (tooltip)
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * If attribute value is secured (password, token, etc.)
	 * @return
	 */
	public boolean isConfidential() {
		return confidential;
	}

	public void setConfidential(boolean confidential) {
		this.confidential = confidential;
	}
	
	/**
	 * Default value (toString)
	 * 
	 * @return
	 */
	public String getDefaultValue() {
		return defaultValue;
	}
	
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	/**
	 * Attribute placeholder
	 * 
	 * @return
	 */
	public String getPlaceholder() {
		return placeholder;
	}
	
	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	@Override
	public boolean isUnmodifiable() {
		return this.unmodifiable;
	}

	@Override
	public void setUnmodifiable(boolean unmodifiable) {
		this.unmodifiable = unmodifiable;
	}	
}
