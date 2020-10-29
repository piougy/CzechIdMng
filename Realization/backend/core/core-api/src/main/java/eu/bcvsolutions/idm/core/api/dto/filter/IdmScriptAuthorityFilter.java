package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.IdmScriptAuthorityDto;

/**
 * Filter for  {@link IdmScriptAuthorityDto}.
 * Filtering by script.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class IdmScriptAuthorityFilter implements BaseFilter {
	
	private UUID scriptId;

	public UUID getScriptId() {
		return scriptId;
	}

	public void setScriptId(UUID scriptId) {
		this.scriptId = scriptId;
	}
}
