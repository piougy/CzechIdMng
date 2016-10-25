package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonBackReference;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.EntityComposition;

/**
 * SuperiorRole inculde subRole (Admin > User)
 * 
 * @author Radek Tomiška 
 *
 */
@Entity
@Table(name = "idm_role_composition", indexes = {
		@Index(name = "idx_idm_role_composition_sub", columnList = "sub_id"),
		@Index(name = "idx_idm_role_composition_superior", columnList = "superior_id")
})
public class IdmRoleComposition extends AbstractEntity implements EntityComposition<IdmRole> {

	private static final long serialVersionUID = -1594762884461330895L;
	
	@Audited
	@NotNull
	@JsonBackReference
	@JoinColumn(name = "superior_id", referencedColumnName = "id")
	@ManyToOne(optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private IdmRole superior;
	
	@Audited
	@NotNull
	@JoinColumn(name = "sub_id", referencedColumnName = "id")
	@ManyToOne(optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private IdmRole sub;

	public IdmRoleComposition() {
	}

	public IdmRoleComposition(Long id) {
		super(id);
	}
	
	public IdmRoleComposition(IdmRole superiorRole, IdmRole subRole) {
		this.superior = superiorRole;
		this.sub = subRole;
	}

	@Override
	public IdmRole getSuperior() {
		return superior;
	}

	public void setSuperior(IdmRole superiorRole) {
		this.superior = superiorRole;
	}

	@Override
	public IdmRole getSub() {
		return sub;
	}

	public void setSub(IdmRole subRole) {
		this.sub = subRole;
	}

}