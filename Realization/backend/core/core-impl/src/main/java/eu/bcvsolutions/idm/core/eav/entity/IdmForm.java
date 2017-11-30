package eu.bcvsolutions.idm.core.eav.entity;

import java.util.UUID;

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

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;

/**
 * Common eav form
 * 
 * @author Radek Tomiška
 * @since 7.6.0
 */
@Entity
@Table(name = "idm_form", indexes = { 
		@Index(name = "idx_idm_form_owner_id", columnList = "owner_id"),
		@Index(name = "idx_idm_form_owner_type", columnList = "owner_type"),
		@Index(name = "idx_idm_form_owner_code", columnList = "owner_code"),
		@Index(name = "idx_idm_form_f_definition_id", columnList = "form_definition_id")
})
public class IdmForm extends AbstractEntity implements FormableEntity {

	private static final long serialVersionUID = 1L;
	
	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME)
	private String name;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "form_definition_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmFormDefinition formDefinition;
	
	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "owner_type", length = DefaultFieldLengths.NAME, nullable = false)
	private String ownerType;
	
	@Audited
	@Column(name = "owner_id", length = 16)
	private UUID ownerId;
	
	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "owner_code", length = DefaultFieldLengths.NAME)
	private String ownerCode;

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}

	public UUID getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(UUID ownerId) {
		this.ownerId = ownerId;
	}

	public String getOwnerCode() {
		return ownerCode;
	}

	public void setOwnerCode(String ownerCode) {
		this.ownerCode = ownerCode;
	}
	
	public void setFormDefinition(IdmFormDefinition formDefinition) {
		this.formDefinition = formDefinition;
	}
	
	public IdmFormDefinition getFormDefinition() {
		return formDefinition;
	}
}
