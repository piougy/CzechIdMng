package eu.bcvsolutions.idm.eav.repository.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.core.api.repository.projection.AbstractDtoProjection;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;

/**
 * Form definition trimmed projection
 * 
 * @author Radek Tomiška
 *
 */
@Projection(name = "excerpt", types = IdmFormDefinition.class)
public interface IdmFormDefinitionExcerpt extends AbstractDtoProjection {

	String getName();
}
