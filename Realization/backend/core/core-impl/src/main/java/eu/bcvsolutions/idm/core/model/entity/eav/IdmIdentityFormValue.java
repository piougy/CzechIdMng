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

/**
 * Identity extended attributes
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_identity_form_value", indexes = {
		@Index(name = "idx_idm_identity_form_a", columnList = "owner_id"),
		@Index(name = "idx_idm_identity_form_a_def", columnList = "attribute_id"),
		@Index(name = "idx_idm_identity_form_a_str", columnList = "string_value") })
public class IdmIdentityFormValue extends AbstractFormValue<IdmIdentity> implements AuditSearchable {

	private static final long serialVersionUID = -6873566385389649927L;
	
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "owner_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmIdentity owner;
	
	public IdmIdentityFormValue() {
	}
	
	public IdmIdentityFormValue(IdmFormAttribute formAttribute) {
		super(formAttribute);
	}
	
	@Override
	public IdmIdentity getOwner() {
		return owner;
	}
	
	public void setOwner(IdmIdentity owner) {
		this.owner = owner;
	}

	@Override
	public String getOwnerId() {
		return this.getOwner().getId().toString();
	}

	@Override
	public String getOwnerCode() {
		return this.getOwner().getCode();
	}

	@Override
	public String getOwnerType() {
		return IdmIdentity.class.getName();
	}

	@Override
	public String getSubOwnerId() {
		return this.getFormAttribute().getId().toString();
	}

	@Override
	public String getSubOwnerCode() {
		return this.getFormAttribute().getCode();
	}

	@Override
	public String getSubOwnerType() {
		return IdmFormAttribute.class.getName();
	}

}
