package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * SuperiorRole inculde subRole (Admin > User)
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@Entity
@Table(name = "idm_role_composition")
public class IdmRoleComposition extends AbstractEntity {

	private static final long serialVersionUID = -1594762884461330895L;
	
	@NotNull
	@JsonBackReference
	@JoinColumn(name = "superior_role_id", referencedColumnName = "id")
	@ManyToOne(optional = false)
	private IdmRole superiorRole;
	
	@NotNull
	@JoinColumn(name = "sub_role_id", referencedColumnName = "id")
	@ManyToOne(optional = false)
	private IdmRole subRole;

	public IdmRoleComposition() {
	}

	public IdmRoleComposition(Long id) {
		super(id);
	}
	
	public IdmRoleComposition(IdmRole superiorRole, IdmRole subRole) {
		this.superiorRole = superiorRole;
		this.subRole = subRole;
	}

	public IdmRole getSuperiorRole() {
		return superiorRole;
	}

	public void setSuperiorRole(IdmRole superiorRole) {
		this.superiorRole = superiorRole;
	}

	public IdmRole getSubRole() {
		return subRole;
	}

	public void setSubRole(IdmRole subRole) {
		this.subRole = subRole;
	}

}