package eu.bcvsolutions.idm.core.model.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;

/**
 * Default filter for role catalogue, parent
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class RoleCatalogueFilter extends QuickFilter {
	
	private UUID parentId;

	public UUID getParentId() {
		return parentId;
	}

	public void setParent(UUID parentId) {
		this.parentId = parentId;
	}
}
