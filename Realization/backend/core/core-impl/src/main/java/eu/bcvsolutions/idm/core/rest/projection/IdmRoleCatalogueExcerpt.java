package eu.bcvsolutions.idm.core.rest.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;

/**
 * Trimmed role catalogue entity
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Projection(name = "excerpt", types = IdmRoleCatalogue.class)
public interface IdmRoleCatalogueExcerpt extends AbstractDtoProjection {
	
	String getName();
	
	IdmRoleCatalogue getParent();
	
	String getDescription();
	
	int getChildrenCount();
	
	public String getNiceName();

	public String getUrl();
	
	public String getUrlTitle();
}
