package eu.bcvsolutions.idm.core.model.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.joda.time.LocalDate;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.AuditSearchable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;

/**
 * Assigned identity role
 * - roles are related to identity's contract
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_identity_role", indexes = {
		@Index(name = "idx_idm_identity_role_ident_c", columnList = "identity_contract_id"),
		@Index(name = "idx_idm_identity_role_role", columnList = "role_id"),
		@Index(name = "idx_idm_identity_role_aut_r", columnList = "automatic_role_id"),
		@Index(name = "idx_idm_identity_role_ext_id", columnList = "external_id"),
		@Index(name = "idx_idm_identity_role_d_r_id", columnList = "direct_role_id"),
		@Index(name = "idx_idm_identity_role_comp_id", columnList = "role_composition_id")
})
public class IdmIdentityRole extends AbstractEntity implements ValidableEntity, AuditSearchable, ExternalIdentifiable {

	private static final long serialVersionUID = 9208706652291035265L;
	
	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "external_id", length = DefaultFieldLengths.NAME)
	private String externalId;
	
	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "identity_contract_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmIdentityContract identityContract;
	
	@NotNull
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmRole role;
	
	@Audited
	@ManyToOne
	@JoinColumn(name = "automatic_role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmAutomaticRole automaticRole; // Assigned role depends on automatic role
	
	@Audited
	@Column(name = "valid_from")
	private LocalDate validFrom;
	
	@Audited
	@Column(name = "valid_till")
	private LocalDate validTill;
	
	@Audited
	@ManyToOne
	@JoinColumn(name = "direct_role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmIdentityRole directRole;
	
	@Audited
	@ManyToOne
	@JoinColumn(name = "role_composition_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmRoleComposition roleComposition;

	public IdmIdentityRole() {
	}

	public IdmIdentityRole(UUID id) {
		super(id);
	}
	
	public IdmIdentityRole(IdmIdentityContract identityContract) {
		Assert.notNull(identityContract);
		//
		this.identityContract = identityContract;
		this.validFrom = identityContract.getValidFrom();
		this.validTill = identityContract.getValidTill();
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

	public IdmRole getRole() {
		return role;
	}

	public void setRole(IdmRole role) {
		this.role = role;
	}
	
	public IdmIdentityContract getIdentityContract() {
		return identityContract;
	}
	
	public void setIdentityContract(IdmIdentityContract identityContract) {
		this.identityContract = identityContract;
	}
	
	public IdmAutomaticRole getAutomaticRole() {
		return automaticRole;
	}

	public void setAutomaticRole(IdmAutomaticRole automaticRole) {
		this.automaticRole = automaticRole;
	}

	/**
	 * Check if this entity is valid from now
	 * @return
	 */
	public boolean isValid() {
		return EntityUtils.isValid(this);
	}

	@Override
	public String getOwnerId() {
		return this.getIdentityContract().getIdentity().getId().toString();
	}

	@Override
	public String getOwnerCode() {
		return this.getIdentityContract().getIdentity().getCode();
	}

	@Override
	public String getOwnerType() {
		return IdmIdentity.class.getName();
	}

	@Override
	public String getSubOwnerId() {
		return this.getRole().getId().toString();
	}

	@Override
	public String getSubOwnerCode() {
		return this.getRole().getCode();
	}

	@Override
	public String getSubOwnerType() {
		return IdmRole.class.getName();
	}
	
	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	@Override
	public String getExternalId() {
		return externalId;
	}

	public IdmIdentityRole getDirectRole() {
		return directRole;
	}

	public void setDirectRole(IdmIdentityRole directRole) {
		this.directRole = directRole;
	}

	public IdmRoleComposition getRoleComposition() {
		return roleComposition;
	}

	public void setRoleComposition(IdmRoleComposition roleComposition) {
		this.roleComposition = roleComposition;
	}
}
