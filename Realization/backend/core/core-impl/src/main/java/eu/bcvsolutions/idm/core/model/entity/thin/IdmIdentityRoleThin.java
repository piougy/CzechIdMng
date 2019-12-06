package eu.bcvsolutions.idm.core.model.entity.thin;

import java.time.LocalDate;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Immutable;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;

/**
 * Assigned identity role - thin variant.
 * - roles are related to identity's contract
 * - the only relation - role is loaded 
 * 
 * @author Radek Tomiška
 * @since 9.7.0
 */
@Entity
@Immutable
@Table(name = "idm_identity_role")
public class IdmIdentityRoleThin extends AbstractEntity implements ValidableEntity, ExternalIdentifiable, FormableEntity {

	private static final long serialVersionUID = 1L;

	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "external_id", length = DefaultFieldLengths.NAME)
	private String externalId;
	
	@NotNull
	@Column(name = "identity_contract_id")
	private UUID identityContract;
	
	@Column(name = "contract_position_id")
	private UUID contractPosition;
	
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmRoleThin role;
	
	@Column(name = "automatic_role_id")
	private UUID automaticRole; // Assigned role depends on automatic role
	
	@Column(name = "valid_from")
	private LocalDate validFrom;
	
	@Column(name = "valid_till")
	private LocalDate validTill;
	
	@Column(name = "direct_role_id")
	private UUID directRole;
	
	@Column(name = "role_composition_id")
	private UUID roleComposition;

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public UUID getIdentityContract() {
		return identityContract;
	}

	public void setIdentityContract(UUID identityContract) {
		this.identityContract = identityContract;
	}

	public UUID getContractPosition() {
		return contractPosition;
	}

	public void setContractPosition(UUID contractPosition) {
		this.contractPosition = contractPosition;
	}
	
	public IdmRoleThin getRole() {
		return role;
	}
	
	public void setRole(IdmRoleThin role) {
		this.role = role;
	}

	public UUID getAutomaticRole() {
		return automaticRole;
	}

	public void setAutomaticRole(UUID automaticRole) {
		this.automaticRole = automaticRole;
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

	public void setValidTill(LocalDate validTill) {
		this.validTill = validTill;
	}

	public UUID getDirectRole() {
		return directRole;
	}

	public void setDirectRole(UUID directRole) {
		this.directRole = directRole;
	}

	public UUID getRoleComposition() {
		return roleComposition;
	}

	public void setRoleComposition(UUID roleComposition) {
		this.roleComposition = roleComposition;
	}
}
