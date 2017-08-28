package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Entity for connection between role catalogue and role
 * Role can be in one or more role catalogues
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Entity
@Table(name = "idm_role_catalogue_role", indexes = {
		@Index(name = "idx_idm_role_catalogue_id", columnList = "role_catalogue_id"),
		@Index(name = "idx_idm_role_id", columnList = "role_id")
})
public class IdmRoleCatalogueRole extends AbstractEntity {

	private static final long serialVersionUID = 1573061147058224605L;
	
	@NotNull
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "role_catalogue_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )	
	private IdmRoleCatalogue roleCatalogue;
	
	@NotNull
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmRole role;

	public IdmRole getRole() {
		return role;
	}

	public IdmRoleCatalogue getRoleCatalogue() {
		return roleCatalogue;
	}

	public void setRole(IdmRole role) {
		this.role = role;
	}

	public void setRoleCatalogue(IdmRoleCatalogue roleCatalogue) {
		this.roleCatalogue = roleCatalogue;
	}
}
