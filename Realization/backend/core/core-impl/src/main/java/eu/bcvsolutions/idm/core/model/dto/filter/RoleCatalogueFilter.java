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
	
	private String name;
	
	private String code;

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public IdmRoleCatalogue getParent() {
		return parent;
	}

	public void setParent(IdmRoleCatalogue parent) {
		this.parent = parent;
	}
}
