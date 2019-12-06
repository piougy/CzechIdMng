package eu.bcvsolutions.idm.core.model.entity.eav;

import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.api.domain.AuditSearchable;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;

/**
 * Identity contracts extended attributes
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_i_contract_form_value", indexes = {
		@Index(name = "idx_idm_i_contract_form_a", columnList = "owner_id"),
		@Index(name = "idx_idm_i_contract_form_a_def", columnList = "attribute_id"),
		@Index(name = "idx_idm_i_contract_form_stxt", columnList = "short_text_value"),
		@Index(name = "idx_idm_i_contract_form_uuid", columnList = "uuid_value") })
public class IdmIdentityContractFormValue extends AbstractFormValue<IdmIdentityContract> implements AuditSearchable {

	private static final long serialVersionUID = -6873566385389649927L;
	
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "owner_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmIdentityContract owner;
	
	public IdmIdentityContractFormValue() {
	}
	
	public IdmIdentityContractFormValue(IdmFormAttribute formAttribute) {
		super(formAttribute);
	}
	
	@Override
	public IdmIdentityContract getOwner() {
		return owner;
	}
	
	public void setOwner(IdmIdentityContract owner) {
		this.owner = owner;
	}

	@Override
	public String getOwnerId() {
		// Audit field owner is identity
		return this.getOwner().getIdentity().getId().toString();
	}

	@Override
	public String getOwnerCode() {
		// Audit field owner is identity
		return this.getOwner().getIdentity().getCode();
	}

	@Override
	public String getOwnerType() {
		// Audit field owner is identity
		return IdmIdentity.class.getName();
	}

	@Override
	public String getSubOwnerId() {
		// Audit field sub owner is contract
		return this.getOwner().getId().toString();
	}

	@Override
	public String getSubOwnerCode() {
		// Audit field sub owner is contract
		return this.getOwner().getPosition();
	}

	@Override
	public String getSubOwnerType() {
		// Audit field sub owner is contract
		return IdmIdentityContract.class.getName();
	}

}
