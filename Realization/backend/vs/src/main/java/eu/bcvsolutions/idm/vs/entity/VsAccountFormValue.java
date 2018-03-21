package eu.bcvsolutions.idm.vs.entity;

import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;

/**
 * Account extended attributes
 * 
 * @author Svanda
 *
 */
@Entity
@Table(name = "vs_account_form_value", indexes = { 
		@Index(name = "idx_vs_account_form_a", columnList = "owner_id"),
		@Index(name = "idx_vs_account_form_a_def", columnList = "attribute_id"),
		@Index(name = "idx_vs_account_form_stxt", columnList = "short_text_value"),
		@Index(name = "idx_vs_account_form_uuid", columnList = "uuid_value") })
public class VsAccountFormValue extends AbstractFormValue<VsAccount> {

	private static final long serialVersionUID = -6873566385389649927L;

	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "owner_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in
										// hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private VsAccount owner;

	public VsAccountFormValue() {
	}

	public VsAccountFormValue(IdmFormAttribute formAttribute) {
		super(formAttribute);
	}

	@Override
	public VsAccount getOwner() {
		return owner;
	}

	@Override
	public void setOwner(VsAccount owner) {
		this.owner = owner;
	}

}
