package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonBackReference;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Intersection table beetween role and identity - guarantee of role
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Entity
@Table(name = "idm_role_guarantee", indexes = {
				@Index(name = "idx_role_guarantee_guarantee", columnList = "guarantee_id"), 
				@Index(name = "idx_role_guarantee_role", columnList = "role_id") } )
public class IdmRoleGuarantee extends AbstractEntity {

	private static final long serialVersionUID = 6106304497345109366L;

	@NotNull
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "guarantee_id", referencedColumnName = "id")
	private IdmIdentity guarantee;

	@NotNull
	@Audited
	@JsonBackReference
	@ManyToOne(optional = false)
	@JoinColumn(name = "role_id", referencedColumnName = "id")
	private IdmRole role;

	public IdmIdentity getGuarantee() {
		return guarantee;
	}

	public void setGuarantee(IdmIdentity guarantee) {
		this.guarantee = guarantee;
	}

	public IdmRole getRole() {
		return role;
	}

	public void setRole(IdmRole role) {
		this.role = role;
	}
}
