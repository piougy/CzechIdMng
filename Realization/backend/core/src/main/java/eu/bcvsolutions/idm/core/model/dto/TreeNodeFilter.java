package eu.bcvsolutions.idm.core.model.dto;

/**
 * Filter for tree node
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class TreeNodeFilter extends QuickFilter {
	
	private Long treeType;
	
	private Long treeNode;

	public Long getTreeType() {
		return treeType;
	}

	public void setTreeType(Long treeType) {
		this.treeType = treeType;
	}

	public Long getTreeNode() {
		return treeNode;
	}

	public void setTreeNode(Long treeNode) {
		this.treeNode = treeNode;
	}}
