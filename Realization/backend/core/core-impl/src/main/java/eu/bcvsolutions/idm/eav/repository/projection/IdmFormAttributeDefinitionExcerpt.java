package eu.bcvsolutions.idm.eav.repository.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.core.api.repository.projection.AbstractDtoProjection;
import eu.bcvsolutions.idm.eav.domain.PersistentType;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;

/**
 * Form attribute definition trimmed projection
 * 
 * @author Radek Tomiška
 *
 */
@Projection(name = "excerpt", types = IdmFormAttribute.class)
public interface IdmFormAttributeDefinitionExcerpt extends AbstractDtoProjection {

	String getName();

	IdmFormDefinition getFormDefinition();

	PersistentType getPersistentType();

	boolean isMultiple();

	boolean isRequired();

	short getSeq();

	boolean isReadonly();

	String getDisplayName();
	
	String getDescription();
	
	boolean isConfidental();
	
	String getDefaultValue();
}
