package eu.bcvsolutions.idm.core.model.entity;

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
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.joda.time.LocalDate;

import eu.bcvsolutions.idm.core.api.domain.AuditSearchable;
import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;

/**
 * Contract time slice
 * 
 * @author svandav
 */
@Entity
@Table(name = "idm_contract_slice", indexes = {
		@Index(name = "idx_idm_contract_slice_idnt", columnList = "identity_id"),
		@Index(name = "idx_idm_contract_slice_wp", columnList = "work_position_id")})
public class IdmContractSlice extends AbstractEntity implements ValidableEntity, FormableEntity, Disableable, AuditSearchable {

	private static final long serialVersionUID = 1L;
	
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
	
	@Audited
	@ManyToOne
	@JoinColumn(name = "parent_contract_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in
										// hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmIdentityContract parentContract;
	
	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "contract_code", length = DefaultFieldLengths.NAME)
	private String contractCode; // Identifier of the main contract on the source system
	
	@Audited
	@NotNull
	@Column(name = "using_as_contract", nullable = false)
	private boolean usingAsContract; // Is this slice actually using as the contract?
	
	@Audited
	@Column(name = "contract_valid_from")
	private LocalDate contractValidFrom;
	
	@Audited
	@Column(name = "contract_valid_till")
	private LocalDate contractValidTill;

	public IdmContractSlice() {
	}
	
	public IdmContractSlice(UUID id) {
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

	public IdmIdentityContract getParentContract() {
		return parentContract;
	}

	public void setParentContract(IdmIdentityContract parentContract) {
		this.parentContract = parentContract;
	}

	public boolean isUsingAsContract() {
		return usingAsContract;
	}

	public void setUsingAsContract(boolean usingAsContract) {
		this.usingAsContract = usingAsContract;
	}

	public String getContractCode() {
		return contractCode;
	}

	public void setContractCode(String contractCode) {
		this.contractCode = contractCode;
	}

	public LocalDate getContractValidFrom() {
		return contractValidFrom;
	}

	public void setContractValidFrom(LocalDate contractValidFrom) {
		this.contractValidFrom = contractValidFrom;
	}

	public LocalDate getContractValidTill() {
		return contractValidTill;
	}

	public void setContractValidTill(LocalDate contractValidTill) {
		this.contractValidTill = contractValidTill;
	}	
}
