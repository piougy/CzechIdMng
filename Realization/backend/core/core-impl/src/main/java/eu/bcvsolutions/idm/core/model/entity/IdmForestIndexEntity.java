package eu.bcvsolutions.idm.core.model.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.bcvsolutions.forest.index.entity.AbstractForestIndex;

/**
 * Persists forest index with {@code Long} content.
 *
 * @author Radek Tomiška
 */
@Entity
@Table(name = "idm_forest_index", indexes = {
		@Index(name = "idx_forest_index_parent", columnList = "parent_id"),
		@Index(name = "idx_forest_index_content", columnList = "content_id"),
		@Index(name = "idx_forest_index_lft", columnList = "lft"),
		@Index(name = "idx_forest_index_rgt", columnList = "rgt"),
		@Index(name = "idx_forest_index_tree_type", columnList = "forest_tree_type")
		})
public class IdmForestIndexEntity extends AbstractForestIndex<IdmForestIndexEntity, UUID> {
	
	private static final long serialVersionUID = 1L;
	
	@JsonDeserialize(as = UUID.class)
	@Column(name = "content_id", nullable = true, length = 16)
	private UUID contentId;

	public IdmForestIndexEntity() {
	}
	
	public IdmForestIndexEntity(IdmForestIndexEntity parent) {
		super(null, parent);
	}
	
	@Override
	public void setContentId(UUID contentId) {
		this.contentId = contentId;
	}
	
	@Override
	public UUID getContentId() {
		return contentId;
	}
}
