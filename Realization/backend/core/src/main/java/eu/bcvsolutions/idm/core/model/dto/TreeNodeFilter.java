package eu.bcvsolutions.idm.core.model.dto;

/**
 * Filter for tree node
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class TreeNodeFilter extends QuickFilter implements BaseFilter {
	
	private Long treeType;
	
	private Long treeNode;

	public Long getTreeType() {
		return treeType;
	}

	public void setTreeType(Long treeType) {
		this.treeType = treeType;
	}
	
	public void setTreeType(String treeType) {
		this.treeType = this.parseToLong(treeType);
	}

	public Long getTreeNode() {
		return treeNode;
	}

	public void setTreeNode(Long treeNode) {
		this.treeNode = treeNode;
	}
	
	public void setTreeNode(String treeNode) {
		this.treeNode = this.parseToLong(treeNode);
	}
	
	private Long parseToLong(String number) {
		if (number != null) {
			return Long.parseLong(number);
		} else{
			return null;
		}
	}
}
