package eu.bcvsolutions.idm.core.model.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;

/**
 * Filter for tree node
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class TreeNodeFilter extends QuickFilter {
	
	private UUID treeTypeId;
	
	private UUID treeNodeId;

	public UUID getTreeTypeId() {
		return treeTypeId;
	}

	public void setTreeTypeId(UUID treeTypeId) {
		this.treeTypeId = treeTypeId;
	}

	public UUID getTreeNodeId() {
		return treeNodeId;
	}

	public void setTreeNodeId(UUID treeNodeId) {
		this.treeNodeId = treeNodeId;
	}}
