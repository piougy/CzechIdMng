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
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest;

/**
 * Role request concept extended attributes
 * 
 * @author Vít Švanda
 *
 */
@Entity
@Table(name = "idm_concept_role_form_value", indexes = {
		@Index(name = "idx_concept_rol_form_a", columnList = "owner_id"),
		@Index(name = "idx_concept_rol_form_a_def", columnList = "attribute_id"),
		@Index(name = "idx_concept_rol_form_stxt", columnList = "short_text_value"),
		@Index(name = "idx_concept_rol_form_uuid", columnList = "uuid_value") })
public class IdmConceptRoleRequestFormValue extends AbstractFormValue<IdmConceptRoleRequest> {

	private static final long serialVersionUID = 1L;

	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "owner_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmConceptRoleRequest owner;

	public IdmConceptRoleRequestFormValue() {
	}

	public IdmConceptRoleRequestFormValue(IdmFormAttribute formAttribute) {
		super(formAttribute);
	}

	@Override
	public IdmConceptRoleRequest getOwner() {
		return owner;
	}

	public void setOwner(IdmConceptRoleRequest owner) {
		this.owner = owner;
	}

}
