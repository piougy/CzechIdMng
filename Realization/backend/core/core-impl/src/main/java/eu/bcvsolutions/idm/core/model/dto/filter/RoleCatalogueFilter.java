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
	
	private String niceName;
	
	private String technicalName;
	
	public String getNiceName() {
		return niceName;
	}

	public String getTechnicalName() {
		return technicalName;
	}

	public void setNiceName(String niceName) {
		this.niceName = niceName;
	}

	public void setTechnicalName(String technicalName) {
		this.technicalName = technicalName;
	}

	public UUID getParentId() {
		return parentId;
	}

	public void setParent(UUID parentId) {
		this.parentId = parentId;
	}
}
