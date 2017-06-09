package eu.bcvsolutions.idm.core.api.dto.filter;

import eu.bcvsolutions.idm.core.api.domain.IdmScriptCategory;

/**
 * Filter for search in scripts. Attributes:
 * * text(from quick filter) - name
 * * category
 * * description
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 */

public class ScriptFilter extends QuickFilter {

    private String description;

    private IdmScriptCategory category;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IdmScriptCategory getCategory() {
        return category;
    }

    public void setCategory(IdmScriptCategory category) {
        this.category = category;
    }
}
