package eu.bcvsolutions.idm.core.security.evaluator.eav;

import java.util.List;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.security.evaluator.AbstractUuidEvaluator;

/**
 * Share form definition by uuid.
 * 
 * @author Radek Tomiška
 *
 */
@Component(FormDefinitionUuidEvaluator.EVALUATOR_NAME)
@Description("Share form definition by uuid")
public class FormDefinitionUuidEvaluator extends AbstractUuidEvaluator<IdmFormDefinition> {
	
	public static final String EVALUATOR_NAME = "form-definition-uuid-evaluator";
	
	@Override
	public String getName() {
		return EVALUATOR_NAME;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto uuidAttribute = new IdmFormAttributeDto(PARAMETER_UUID, PARAMETER_UUID, PersistentType.UUID, BaseFaceType.FORM_DEFINITION_SELECT);
		uuidAttribute.setRequired(true);
		return Lists.newArrayList(uuidAttribute);
	}
}
