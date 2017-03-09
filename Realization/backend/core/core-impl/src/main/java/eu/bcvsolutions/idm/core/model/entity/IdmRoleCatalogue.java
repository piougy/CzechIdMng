package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Formula;
import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.BaseTreeEntity;

/**
 * Role catalogue entity
 * - this entity is unique identified by CODE not NAME.
 * - Attribute name is unique for all children of one parent.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Entity
@Table(name = "idm_role_catalogue", indexes = {
		@Index(name = "ux_role_catalogue_code", columnList = "code", unique = true),
		@Index(name = "ux_role_catalogue_name", columnList = "name"),
		@Index(name = "idx_idm_role_cat_parent", columnList = "parent_id")})
public class IdmRoleCatalogue extends AbstractEntity implements BaseTreeEntity<IdmRoleCatalogue> {

	private static final long serialVersionUID = 1883443149941011579L;

	@Audited
	@NotNull
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false)
	private String name;
	
	@Audited
	@NotNull
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "code", length = DefaultFieldLengths.NAME, nullable = false)
	private String code;
	
	@Audited
	@JsonProperty("parent") // required - BaseTreeEntity vs ForestContent setter are in conflict
	@ManyToOne(optional = true)
	@JoinColumn(name = "parent_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmRoleCatalogue parent;
	
	@Audited
	@Column(name = "description")
	private String description;
	
	@JsonProperty(access = Access.READ_ONLY)
	@Column(insertable = false, updatable = false)
	@Formula("(select coalesce(count(1),0) from idm_role_catalogue e where e.parent_id = id)")
	private int childrenCount;
	
	@Audited
	@Column(name = "url")
	private String url;
	
	@Audited
	@Column(name = "url_title")
	private String urlTitle;

	@Override
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	
	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
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
	
	public void setChildrenCount(int childrenCount) {
		this.childrenCount = childrenCount;
	}

	public int getChildrenCount() {
		return childrenCount;
	}

	public String getUrl() {
		return url;
	}

	public String getUrlTitle() {
		return urlTitle;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUrlTitle(String urlTitle) {
		this.urlTitle = urlTitle;
	}
}
