package eu.bcvsolutions.idm.core.eav.entity;

import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

/**
 * Codelist values
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_code_list_item_value", indexes = {
		@Index(name = "idx_idm_code_l_i_value_a", columnList = "owner_id"),
		@Index(name = "idx_idm_code_l_i_value_a_def", columnList = "attribute_id"),
		@Index(name = "idx_idm_code_l_i_value_stxt", columnList = "short_text_value"),
		@Index(name = "idx_idm_code_l_i_value_uuid", columnList = "uuid_value") })
public class IdmCodeListItemValue extends AbstractFormValue<IdmCodeListItem> {

	private static final long serialVersionUID = -6873566385389649927L;
	
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "owner_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmCodeListItem owner;
	
	public IdmCodeListItemValue() {
	}
	
	public IdmCodeListItemValue(IdmFormAttribute formAttribute) {
		super(formAttribute);
	}
	
	@Override
	public IdmCodeListItem getOwner() {
		return owner;
	}
	
	public void setOwner(IdmCodeListItem owner) {
		this.owner = owner;
	}
}
