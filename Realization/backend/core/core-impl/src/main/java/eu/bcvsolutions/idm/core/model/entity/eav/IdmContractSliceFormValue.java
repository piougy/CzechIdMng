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
import eu.bcvsolutions.idm.core.model.entity.IdmContractSlice;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;

/**
 * Contract slices extended attributes
 * 
 * @author svandav
 *
 */
@Entity
@Table(name = "idm_con_slice_form_value", indexes = {
		@Index(name = "idx_idm_con_slice_form_a", columnList = "owner_id"),
		@Index(name = "idx_idm_con_slice_form_a_def", columnList = "attribute_id"),
		@Index(name = "idx_idm_con_slice_form_stxt", columnList = "short_text_value"),
		@Index(name = "idx_idm_con_slice_form_uuid", columnList = "uuid_value") })
public class IdmContractSliceFormValue extends AbstractFormValue<IdmContractSlice> implements AuditSearchable {

	private static final long serialVersionUID = 1L;
	
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "owner_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private IdmContractSlice owner;
	
	public IdmContractSliceFormValue() {
	}
	
	public IdmContractSliceFormValue(IdmFormAttribute formAttribute) {
		super(formAttribute);
	}
	
	@Override
	public IdmContractSlice getOwner() {
		return owner;
	}
	
	public void setOwner(IdmContractSlice owner) {
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
		return this.getOwner().getSubOwnerCode();
	}

	@Override
	public String getSubOwnerType() {
		// Audit field sub owner is contract
		return IdmIdentityContract.class.getName();
	}

}
