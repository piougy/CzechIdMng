package eu.bcvsolutions.idm.eav.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

import org.hibernate.validator.constraints.NotEmpty;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.eav.domain.PersistentType;

@Entity
@Table(name = "idm_form_attribute_definition", indexes = {
		@Index(name = "ux_idm_form_attribute_definition_name", columnList = "definition_id, name", unique = true) })
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class IdmFormAttributeDefinition extends AbstractEntity {

	private static final long serialVersionUID = 6037781154742359100L;
	//
	@NotEmpty
	@Basic(optional = false)
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", nullable = false, length = DefaultFieldLengths.NAME)
	private String name;
	
	@JoinColumn(name = "definition_id", referencedColumnName = "id")
	@ManyToOne(optional = false)
	private IdmFormDefinition formDefinition;
	
	@NotEmpty
	@Basic(optional = false)
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "display_name", nullable = false, length = DefaultFieldLengths.NAME)
	private String displayName;
	
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "description", nullable = true, length = DefaultFieldLengths.NAME)
	private String description;	
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "persistent_type", length = 45, nullable = false)
	private PersistentType persistentType;
	
	@NotNull
	@Column(name = "multiple", nullable = false)
	private boolean multiple;
	
	@NotNull
	@Column(name = "mandatory", nullable = false)
	private boolean mandatory;
	
	@NotNull
	@Column(name = "readonly", nullable = false)
	private boolean readonly;
	
	@NotNull
	@Column(name = "confidental", nullable = false)
	private boolean confidental;
	
	@Max(99999)
	@Column(name = "SEQ")
	private short seq;

	public IdmFormAttributeDefinition() {
	}

	public IdmFormAttributeDefinition(Long id) {
		super(id);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setFormDefinition(IdmFormDefinition formDefinition) {
		this.formDefinition = formDefinition;
	}
	
	public IdmFormDefinition getFormDefinition() {
		return formDefinition;
	}

	public PersistentType getPersistentType() {
		return persistentType;
	}

	public void setPersistentType(PersistentType persistentType) {
		this.persistentType = persistentType;
	}

	public boolean isMultiple() {
		return multiple;
	}

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public short getSeq() {
		return seq;
	}

	public void setSeq(short seq) {
		this.seq = seq;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isConfidental() {
		return confidental;
	}

	public void setConfidental(boolean confidental) {
		this.confidental = confidental;
	}
}
