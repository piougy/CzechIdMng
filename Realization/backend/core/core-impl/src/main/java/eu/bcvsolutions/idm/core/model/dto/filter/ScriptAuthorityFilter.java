package eu.bcvsolutions.idm.core.model.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Filter for  {@link IdmScriptAuthority}
 * Filtering by script.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class ScriptAuthorityFilter implements BaseFilter {
	
	private UUID scriptId;

	public UUID getScriptId() {
		return scriptId;
	}

	public void setScriptId(UUID scriptId) {
		this.scriptId = scriptId;
	}
}
