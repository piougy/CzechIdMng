package eu.bcvsolutions.idm.eav.rest.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;
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
public interface IdmFormAttributeExcerpt extends AbstractDtoProjection {

	String getName();

	IdmFormDefinition getFormDefinition();

	PersistentType getPersistentType();

	boolean isMultiple();

	boolean isRequired();

	short getSeq();

	boolean isReadonly();

	String getDisplayName();
	
	String getDescription();
	
	boolean isConfidential();
	
	String getDefaultValue();
}
