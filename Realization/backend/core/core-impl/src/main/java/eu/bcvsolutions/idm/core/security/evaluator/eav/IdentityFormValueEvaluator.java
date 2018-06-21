package eu.bcvsolutions.idm.core.security.evaluator.eav;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityFormValue;

/**
 * Permissions to form attribute values
 * 
 * @author Radek Tomiška
 *
 */
@Component(IdentityFormValueEvaluator.EVALUATOR_NAME)
@Description("Permissions to identity form attribute values. By definition (main if not specified)"
		+ " and attrinute code (all if not specified).")
public class IdentityFormValueEvaluator extends AbstractFormValueEvaluator<IdmIdentityFormValue> {
	
	public static final String EVALUATOR_NAME = "identity-form-value-evaluator";
	
	@Override
	public String getName() {
		return EVALUATOR_NAME;
	}
}
