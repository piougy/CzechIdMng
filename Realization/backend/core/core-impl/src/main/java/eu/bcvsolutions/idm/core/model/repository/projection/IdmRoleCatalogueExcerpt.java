package eu.bcvsolutions.idm.core.model.repository.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.core.api.repository.projection.AbstractDtoProjection;
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
}
