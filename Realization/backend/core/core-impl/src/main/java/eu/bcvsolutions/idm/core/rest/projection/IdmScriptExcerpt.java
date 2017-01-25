package eu.bcvsolutions.idm.core.rest.projection;

import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;
import eu.bcvsolutions.idm.core.model.domain.IdmScriptCategory;

public interface IdmScriptExcerpt extends AbstractDtoProjection {
	
	public String getName();
	
	public IdmScriptCategory getCategory();
	
	public String getScript();
	
	public String getDescription();
}
