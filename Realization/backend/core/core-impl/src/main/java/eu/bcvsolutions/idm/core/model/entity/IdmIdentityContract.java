package eu.bcvsolutions.idm.core.model.entity;

import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.joda.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.api.domain.AuditSearchable;
import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;

/**
 * Identity contract - working position
 * 
 * @author Radek Tomiška
 */
@Entity
@Table(name = "idm_identity_contract", indexes = {
		@Index(name = "idx_idm_identity_contract_idnt", columnList = "identity_id"),
		@Index(name = "idx_idm_identity_contract_wp", columnList = "work_position_id"),
		@Index(name = "idx_idm_contract_ext_id", columnList = "external_id")})
public class IdmIdentityContract 
		extends AbstractEntity 
		implements ValidableEntity, FormableEntity, Disableable, AuditSearchable, ExternalIdentifiable {

	private static final long serialVersionUID = 328041550861866181L;
	
	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "external_id", length = DefaultFieldLengths.NAME)
	private String externalId;

	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "identity_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmIdentity identity;
	
	@Audited
	@Column(name = "valid_from")
	private LocalDate validFrom;
	
	@Audited
	@Column(name = "valid_till")
	private LocalDate validTill;
	
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@ManyToOne(optional = true)
	@JoinColumn(name = "work_position_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmTreeNode workPosition;
	
	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "position", length = DefaultFieldLengths.NAME)
	private String position; // string position - if working position tree is not configured, then this string could be used
	
	@Audited
	@NotNull
	@Column(name = "externe", nullable = false)
	private boolean externe;
	
	@Audited
	@NotNull
	@Column(name = "main", nullable = false)
	private boolean main = true;
	
	@Audited
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "description", length = DefaultFieldLengths.DESCRIPTION)
	private String description;
	
	@Audited
	@NotNull
	@Column(name = "disabled", nullable = false)
	private boolean disabled; // redundant to state now
	
	@Audited
	@Enumerated(EnumType.STRING)
	@Column(name = "state", nullable = true, length = DefaultFieldLengths.ENUMARATION)
	private ContractState state;
	
	@JsonIgnore
	@OneToMany(mappedBy = "identityContract")
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private List<IdmIdentityRole> roles; // only for hibernate mapping - we don't want lazy lists (many roles)
	
	@JsonIgnore
	@OneToMany(mappedBy = "identityContract")
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private List<IdmContractGuarantee> guarantees; // only for hibernate mapping - we don't want lazy lists (many roles)
	
	public IdmIdentityContract() {
	}
	
	public IdmIdentityContract(UUID id) {
		super(id);
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	public LocalDate getValidTill() {
		return validTill;
	}

	public void setValidTill(LocalDate validTo) {
		this.validTill = validTo;
	}

	public IdmIdentity getIdentity() {
		return identity;
	}

	public void setIdentity(IdmIdentity identity) {
		this.identity = identity;
	}

	public IdmTreeNode getWorkPosition() {
		return workPosition;
	}

	public void setWorkPosition(IdmTreeNode workPosition) {
		this.workPosition = workPosition;
	}
	
	/**
	 * Working position (if no tree structure is defined etc.)
	 * 
	 * @return
	 */
	public String getPosition() {
		return position;
	}
	
	public void setPosition(String position) {
		this.position = position;
	}
	
	/**
	 * Externe working position
	 * 
	 * @return
	 */
	public boolean isExterne() {
		return externe;
	}
	
	public void setExterne(boolean externe) {
		this.externe = externe;
	}
	
	/**
	 * main ~= default identity contract
	 * 
	 * @return
	 */
	public boolean isMain() {
		return main;
	}
	
	public void setMain(boolean main) {
		this.main = main;
	}
	
	/**
	 * Custom description
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public boolean isDisabled() {
		return disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	@Override
	public String getOwnerId() {
		return this.getIdentity().getId().toString();
	}

	@Override
	public String getOwnerCode() {
		return this.getIdentity().getCode();
	}

	@Override
	public String getOwnerType() {
		return IdmIdentity.class.getName();
	}

	@Override
	public String getSubOwnerId() {
		if (this.getWorkPosition() == null) {
			return null;
		}
		return this.getWorkPosition().getId().toString();
	}

	@Override
	public String getSubOwnerCode() {
		if (this.getWorkPosition() == null) {
			return null;
		}
		return this.getWorkPosition().getCode();
	}

	@Override
	public String getSubOwnerType() {
		return IdmTreeNode.class.getName();
	}
	
	public void setState(ContractState state) {
		this.state = state;
	}
	
	public ContractState getState() {
		return state;
	}
	
	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	@Override
	public String getExternalId() {
		return externalId;
	}
}
