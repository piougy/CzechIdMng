package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Formula;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

@Entity
@Table(name = "idm_tree_node", indexes = { 
		@Index(name = "ux_tree_node_code", columnList = "tree_type_id,code", unique = true),
		@Index(name = "idx_idm_tree_node_parent", columnList = "parent_id"),
		@Index(name = "idx_idm_tree_node_type", columnList = "tree_type_id")
})
public class IdmTreeNode extends AbstractEntity {

	private static final long serialVersionUID = -3099001738101202320L;

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

	@Audited
	@NotNull
	@Column(name = "disabled", nullable = false)
	private boolean disabled = false;

	@Version
	@JsonIgnore
	private Long version; // Optimistic lock - will be used with ETag

	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "parent_id", referencedColumnName = "id")
	private IdmTreeNode parent;

	@NotNull
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "tree_type_id", referencedColumnName = "id")
	private IdmTreeType treeType;

	@JsonProperty(access = Access.READ_ONLY)
	@Column(insertable = false, updatable = false)
	@Formula("(select coalesce(count(1),0) from idm_tree_node e where e.parent_id = id)")
	private Integer childrenCount;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public void setParent(IdmTreeNode parent) {
		this.parent = parent;
	}

	public IdmTreeNode getParent() {
		return this.parent;
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

	public void setChildrenCount(Integer childrenCount) {
		this.childrenCount = childrenCount;
	}

	public Integer getChildrenCount() {
		return childrenCount;
	}
}
