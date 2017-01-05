package eu.bcvsolutions.idm.core.model.dto.filter;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.model.domain.IdmRuleCategory;

/**
 * Filter for search in rules. Attributes:
 * * text(from quick filter) - name
 * * category
 * * description
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class RuleFilter extends QuickFilter {
	
	private String description;
	
	private IdmRuleCategory category;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public IdmRuleCategory getCategory() {
		return category;
	}

	public void setCategory(IdmRuleCategory category) {
		this.category = category;
	}
}
