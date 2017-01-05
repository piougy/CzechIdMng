package eu.bcvsolutions.idm.core.rest.projection;

import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;
import eu.bcvsolutions.idm.core.model.domain.IdmRuleCategory;

public interface IdmRuleExcerpt extends AbstractDtoProjection {
	
	public String getName();
	
	public IdmRuleCategory getCategory();
	
	public String getScript();
	
	public String getDescription();
}
