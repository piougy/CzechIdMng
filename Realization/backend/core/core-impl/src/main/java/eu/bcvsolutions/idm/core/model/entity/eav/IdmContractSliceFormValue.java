package eu.bcvsolutions.idm.core.model.entity.eav;

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
import eu.bcvsolutions.idm.core.model.entity.IdmContractSlice;

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
public class IdmContractSliceFormValue extends AbstractFormValue<IdmContractSlice> {

	private static final long serialVersionUID = 1L;
	
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "owner_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
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

}
