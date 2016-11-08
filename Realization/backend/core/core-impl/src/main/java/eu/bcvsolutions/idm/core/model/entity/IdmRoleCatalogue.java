package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.IdentifiableByName;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.BaseTreeEntity;

/**
 * Role catalogue entity
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Entity
@Table(name = "idm_role_catalogue", indexes = { 
		@Index(name = "ux_role_catalogue_name", columnList = "name"),
		@Index(name = "idx_idm_role_catalogue_parent", columnList = "parent_id")})
public class IdmRoleCatalogue extends AbstractEntity implements IdentifiableByName, BaseTreeEntity<IdmRoleCatalogue> {

	private static final long serialVersionUID = 1883443149941011579L;

	@Audited(withModifiedFlag=true)
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false, unique = true)
	private String name;
	
	@Audited(withModifiedFlag=true)
	@ManyToOne(optional = true)
	@JoinColumn(name = "parent_id", referencedColumnName = "id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private IdmRoleCatalogue parent;
	
	@Audited(withModifiedFlag=true)
	@Column(name = "description")
	private String description;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public IdmRoleCatalogue getParent() {
		return parent;
	}
	
	@Override
	public void setParent(IdmRoleCatalogue parent) {
		this.parent = parent;
		
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
