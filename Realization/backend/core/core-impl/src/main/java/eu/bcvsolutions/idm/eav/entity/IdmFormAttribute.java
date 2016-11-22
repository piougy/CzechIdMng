package eu.bcvsolutions.idm.eav.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.eav.domain.PersistentType;

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
		@Index(name = "ux_idm_f_a_definition_name", columnList = "definition_id, name", unique = true) })
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class IdmFormAttribute extends AbstractEntity {

	private static final long serialVersionUID = 6037781154742359100L;
	//
	@NotEmpty
	@Basic(optional = false)
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", nullable = false, length = DefaultFieldLengths.NAME)
	private String name;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "definition_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmFormDefinition formDefinition;
	
	@NotEmpty
	@Basic(optional = false)
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "display_name", nullable = false, length = DefaultFieldLengths.NAME)
	private String displayName;
	
	@Size(max = DefaultFieldLengths.LOG) // TODO: @Lob?
	@Column(name = "description", nullable = true)
	private String description;	
	
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
	private short seq;
	
	@Size(max = DefaultFieldLengths.LOG)
	@Column(name = "default_value", nullable = true, length = DefaultFieldLengths.LOG)
	private String defaultValue;

	public IdmFormAttribute() {
	}

	/**
	 * Name / key - unique in one form defifinition
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
	 * Returns empty list by persistentType
	 * 
	 * @param persistentType
	 * @return
	 */
	@JsonIgnore
	@SuppressWarnings("rawtypes")
	public List getEmptyList() {
		Assert.notNull(persistentType);
		//
		switch (persistentType) {
			case INT:
			case LONG:
				return new ArrayList<Long>();
			case BOOLEAN:
				return new ArrayList<Boolean>();
			case DATE:
			case DATE_TIME:
				return new ArrayList<Date>();
			case DOUBLE:
			case CURRENCY:
				return new ArrayList<BigDecimal>();
			default:
				return new ArrayList<String>();
		}
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
	public short getSeq() {
		return seq;
	}

	public void setSeq(short seq) {
		this.seq = seq;
	}

	/**
	 * Attribute can not be changed by user 
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
	 * User friendly name (label)
	 * 
	 * @return
	 */
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
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
}
