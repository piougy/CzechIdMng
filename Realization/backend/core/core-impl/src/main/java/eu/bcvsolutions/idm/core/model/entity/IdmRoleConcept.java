package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;



//@Audited
//@Entity
//@DiscriminatorValue("concept")
public class IdmRoleConcept extends IdmRole {

	private static final long serialVersionUID = 1L;

	@Column(name = "concept")
	private Boolean concept;

	public Boolean isConcept() {
		return concept;
	}

	public void setConcept(Boolean concept) {
		this.concept = concept;
	}
}