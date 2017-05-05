package eu.bcvsolutions.idm.core.model.entity;

import java.util.UUID;

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

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.bcvsolutions.forest.index.domain.ForestContent;
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
public class IdmRoleCatalogue extends AbstractEntity implements BaseTreeEntity<IdmRoleCatalogue>, ForestContent<IdmRoleCatalogue, IdmForestIndexEntity, UUID> {

	private static final long serialVersionUID = 1883443149941011579L;
	public static final String FOREST_TREE_TYPE = "role-catalogue";

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
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "description", length = DefaultFieldLengths.DESCRIPTION)
	private String description;
	
	// TODO: formule for roles in folder count
//	@JsonProperty(access = Access.READ_ONLY)
//	@Column(insertable = false, updatable = false)
//	@Formula("(select coalesce(count(1),0) from idm_role_catalogue e where e.parent_id = id)")
//	private int childrenCount;
	
	@Audited
	@Column(name = "url")
	private String url;
	
	@Audited
	@Column(name = "url_title")
	private String urlTitle;
	
	@JsonIgnore
	@ManyToOne(optional = true)
	@JoinColumn(name = "id", referencedColumnName = "content_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmForestIndexEntity forestIndex;

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

	/**
	 * Children count based on index
	 * 
	 * @return
	 */
	public Integer getChildrenCount() {
		if (forestIndex == null) {
			return null;
		}
		return forestIndex.getChildrenCount();
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
	
	@JsonIgnore
	public long getLft() {
		if (forestIndex == null || forestIndex.getLft() == null) {
			// we don't need check null pointers in all queries
			return 0L;
		}
		return forestIndex.getLft();
	}
	
	@JsonIgnore
	public long getRgt() {
		if (forestIndex == null || forestIndex.getRgt() == null) {
			// we don't need check null pointers in all queries
			return 0L;
		}
		return forestIndex.getRgt();
	}

	@Override
	public IdmForestIndexEntity getForestIndex() {
		return forestIndex;
	}

	@Override
	public void setForestIndex(IdmForestIndexEntity forestIndex) {
		this.forestIndex = forestIndex;
	}

	/**
	 * Returns static {@value #FOREST_TREE_TYPE} value.
	 * 
	 */
	@Override
	@JsonIgnore
	public String getForestTreeType() {
		return FOREST_TREE_TYPE;
	}
}
