package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import java.util.UUID;
import javax.persistence.Embedded;
import org.hibernate.envers.Audited;

/**
 * Delegation entity.
 *
 * @author Vít Švanda
 *
 */
@Entity
@Table(name = "idm_delegation", indexes = {
	@Index(name = "idx_i_del_definition_id", columnList = "definition_id"),
	@Index(name = "idx_i_del_owner_id", columnList = "owner_id"),
	@Index(name = "idx_i_del_owner_type", columnList = "owner_type")})
public class IdmDelegation extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@Audited
	@ManyToOne(optional = false)
	@NotNull
	@JoinColumn(name = "definition_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT), nullable = false)
	private IdmDelegationDefinition definition;

	@Audited
	@NotNull
	@Column(name = "owner_id", nullable = false, length = 16)
	private UUID ownerId;
	
	@Audited
	@NotNull
	@Column(name = "owner_type", nullable = false, length = 255)
	private String ownerType;
	
	@Embedded
	private OperationResult ownerState;

	public IdmDelegationDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(IdmDelegationDefinition definition) {
		this.definition = definition;
	}

	public UUID getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(UUID ownerId) {
		this.ownerId = ownerId;
	}

	public String getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}

	public OperationResult getOwnerState() {
		return ownerState;
	}

	public void setOwnerState(OperationResult ownerState) {
		this.ownerState = ownerState;
	}
	
}
