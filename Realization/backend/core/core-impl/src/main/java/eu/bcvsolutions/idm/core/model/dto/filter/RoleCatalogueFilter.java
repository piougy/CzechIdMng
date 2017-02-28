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

	public UUID getParentId() {
		return parentId;
	}

	public void setParent(UUID parentId) {
		this.parentId = parentId;
	}
}
