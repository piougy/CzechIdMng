package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;

/**
 * Automatic role that is assignment by value in attribute.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @since 7.7.0
 *
 */

@Audited
@Entity
@Table(name = "idm_auto_role_attribute")
public class IdmAutomaticRoleAttribute extends IdmAutomaticRole {

	private static final long serialVersionUID = 1L;

	@NotNull
	@Column(name = "concept", nullable = false)
	private boolean concept;

	public boolean isConcept() {
		return concept;
	}

	public void setConcept(boolean concept) {
		this.concept = concept;
	}
}