package eu.bcvsolutions.idm.core.model.entity;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;
import java.time.LocalDate;
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

/**
 * Definition of a delegation entity.
 *
 * @author Vít Švanda
 *
 */
@Entity
@Table(name = "idm_delegation_def", indexes = {
	@Index(name = "idx_i_del_def_delegator_id", columnList = "delegator_id"),
	@Index(name = "idx_i_del_def_delegate_id", columnList = "delegate_id"),
	@Index(name = "idx_i_del_def_del_cont_id", columnList = "delegator_contract_id"),
	@Index(name = "idx_i_del_def_valid_from", columnList = "valid_from"),
	@Index(name = "idx_i_del_def_valid_till", columnList = "valid_till"),
	@Index(name = "idx_i_del_def_type", columnList = "type")})
public class IdmDelegationDefinition extends AbstractEntity implements ValidableEntity {

	private static final long serialVersionUID = 1L;

	@Audited
	@SuppressWarnings("deprecation")
	@ManyToOne(optional = true)
	@NotNull
	@JoinColumn(name = "delegator_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT), nullable = false)
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmIdentity delegator;

	@Audited
	@SuppressWarnings("deprecation")
	@ManyToOne(optional = false)
	@NotNull
	@JoinColumn(name = "delegate_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT), nullable = false)
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmIdentity delegate;

	@Audited
	@SuppressWarnings("deprecation")
	@ManyToOne(optional = true)
	@JoinColumn(name = "delegator_contract_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT), nullable = true)
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmIdentityContract delegatorContract;

	@Audited
	@NotNull
	@Column(name = "type", nullable = false, length = 255)
	private String type;

	@Audited
	@Column(name = "valid_from")
	private LocalDate validFrom;

	@Audited
	@Column(name = "valid_till")
	private LocalDate validTill;

	@Audited
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "description", length = DefaultFieldLengths.DESCRIPTION)
	private String description;

	public IdmIdentity getDelegator() {
		return delegator;
	}

	public void setDelegator(IdmIdentity delegator) {
		this.delegator = delegator;
	}

	public IdmIdentity getDelegate() {
		return delegate;
	}

	public void setDelegate(IdmIdentity delegate) {
		this.delegate = delegate;
	}

	public IdmIdentityContract getDelegatorContract() {
		return delegatorContract;
	}

	public void setDelegatorContract(IdmIdentityContract delegatorContract) {
		this.delegatorContract = delegatorContract;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	@Override
	public LocalDate getValidTill() {
		return validTill;
	}

	public void setValidTill(LocalDate validTill) {
		this.validTill = validTill;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
