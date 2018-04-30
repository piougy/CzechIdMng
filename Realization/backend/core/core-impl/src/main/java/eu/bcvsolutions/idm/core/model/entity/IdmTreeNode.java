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
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.bcvsolutions.forest.index.domain.ForestContent;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.BaseTreeEntity;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;

/**
 * Tree nodes
 * 
 * @author Ondřej Kopr
 */
@Entity
@Table(name = "idm_tree_node", indexes = { 
		@Index(name = "ux_tree_node_code", columnList = "tree_type_id,code", unique = true),
		@Index(name = "idx_idm_tree_node_parent", columnList = "parent_id"),
		@Index(name = "idx_idm_tree_node_type", columnList = "tree_type_id"),
		@Index(name = "idx_idm_tree_node_ext_id", columnList = "external_id")
})
public class IdmTreeNode 
		extends AbstractEntity 
		implements BaseTreeEntity<IdmTreeNode>, ForestContent<IdmForestIndexEntity, UUID>, FormableEntity, Disableable, ExternalIdentifiable {

	private static final long serialVersionUID = -3099001738101202320L;
	public static final String TREE_TYPE_PREFIX = "tree-type-";

	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "external_id", length = DefaultFieldLengths.NAME)
	private String externalId;
	
	@Audited
	@NotEmpty
	@Size(min = 0, max = DefaultFieldLengths.NAME)
	@Column(name = "code", length = DefaultFieldLengths.NAME, nullable = false)
	private String code;

	@Audited
	@NotEmpty
	@Size(min = 0, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false)
	private String name;

	@Version
	@JsonIgnore
	private Long version; // Optimistic lock - will be used with ETag

	@Audited
	@JsonProperty("parent") // required - BaseTreeEntity vs ForestContent setter are in conflict
	@ManyToOne(optional = true)
	@JoinColumn(name = "parent_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmTreeNode parent;

	@NotNull
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "tree_type_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmTreeType treeType;
	
	@JsonIgnore
	@ManyToOne(optional = true)
	@JoinColumn(name = "id", referencedColumnName = "content_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmForestIndexEntity forestIndex;
	
	@Audited
	@NotNull
	@Column(name = "disabled", nullable = false)
	private boolean disabled;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public void setParent(IdmTreeNode parent) {
		this.parent = parent;
	}
	
	@Override
	public IdmTreeNode getParent() {
		return this.parent;
	}
	
	@Override
	public UUID getParentId() {
		return parent == null ? null : parent.getId();
	}

	public IdmTreeType getTreeType() {
		return treeType;
	}

	public void setTreeType(IdmTreeType treeType) {
		this.treeType = treeType;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
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
	 * Returns tree type code with static {@value #TREE_TYPE_PREFIX} prefix.
	 * 
	 */
	@Override
	@JsonIgnore
	public String getForestTreeType() {
		return toForestTreeType(treeType);
	}
	
	@Override
	public boolean isDisabled() {
		return disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	/**
	 * Return tree type code from forest tree type
	 * 
	 * @param forestTreeType
	 * @return
	 */
	public static UUID toTreeTypeId(String forestTreeType) {
		Assert.hasLength(forestTreeType);
		//
		return UUID.fromString(forestTreeType.replaceFirst(TREE_TYPE_PREFIX, ""));
	}
	
	/**
	 * Returns forest tree type from tree type id
	 * 
	 * @param treeType
	 * @return
	 */
	public static String toForestTreeType(IdmTreeType treeType) {
		Assert.notNull(treeType);
		//
		return toForestTreeType(treeType.getId());
	}
	
	/**
	 * Returns forest tree type from tree type id
	 * 
	 * @param treeType
	 * @return
	 */
	public static String toForestTreeType(UUID treeTypeId) {
		Assert.notNull(treeTypeId);
		//
		return String.format("%s%s", TREE_TYPE_PREFIX, treeTypeId);
	}
	
	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	@Override
	public String getExternalId() {
		return externalId;
	}
}
