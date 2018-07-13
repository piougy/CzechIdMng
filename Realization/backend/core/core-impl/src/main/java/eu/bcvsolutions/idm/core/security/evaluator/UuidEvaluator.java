package eu.bcvsolutions.idm.core.security.evaluator;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Share entity with uuid
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Share entity by uuid")
public class UuidEvaluator extends AbstractUuidEvaluator<Identifiable> {
	
	@Override
	public boolean supports(Class<?> authorizableType) {
		Assert.notNull(authorizableType);
		// uuid superclasses only
		return super.supports(authorizableType)
				&& (AbstractEntity.class.isAssignableFrom(authorizableType) || AbstractDto.class.isAssignableFrom(authorizableType));
	}
	
}
