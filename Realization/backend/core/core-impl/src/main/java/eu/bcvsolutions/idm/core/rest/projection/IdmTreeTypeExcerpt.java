package eu.bcvsolutions.idm.core.rest.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;

/**
 * Trimmed view for IdmTreeType
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Projection(name = "excerpt", types = IdmTreeType.class)
public interface IdmTreeTypeExcerpt extends AbstractDtoProjection {
	
	String getCode();
	
	String getName();
}
