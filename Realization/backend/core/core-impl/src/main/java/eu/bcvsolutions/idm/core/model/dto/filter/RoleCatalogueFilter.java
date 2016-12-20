package eu.bcvsolutions.idm.core.model.dto.filter;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;

/**
 * Default filter for role catalogue, parent
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class RoleCatalogueFilter extends QuickFilter {
	
	private IdmRoleCatalogue parent;

	public IdmRoleCatalogue getParent() {
		return parent;
	}

	public void setParent(IdmRoleCatalogue parent) {
		this.parent = parent;
	}
}
