package eu.bcvsolutions.idm.core.eav.rest.projection;

import java.util.List;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;

/**
 * Form definition trimmed projection
 * 
 * @author Radek Tomiška
 *
 */
@Projection(name = "excerpt", types = IdmFormDefinition.class)
public interface IdmFormDefinitionExcerpt extends AbstractDtoProjection {

	String getType();
	
	String getCode();
	
	String getName();
	
	String getDescription();
	
	boolean isMain();
	
	List<IdmFormAttribute> getFormAttributes();
	
	boolean isUnmodifiable();
}
