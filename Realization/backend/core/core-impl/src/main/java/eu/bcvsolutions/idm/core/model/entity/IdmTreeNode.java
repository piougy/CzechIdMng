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

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

@Entity
@Table(name = "idm_tree_node", indexes = { @Index(name = "ux_tree_node_name", columnList = "name") })
public class IdmTreeNode extends AbstractEntity {
	
	private static final long serialVersionUID = -3099001738101202320L;

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
	
}
