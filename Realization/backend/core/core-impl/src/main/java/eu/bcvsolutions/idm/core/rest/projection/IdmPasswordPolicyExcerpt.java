package eu.bcvsolutions.idm.core.rest.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;

@Projection(name = "excerpt", types = IdmPasswordPolicy.class)
public interface IdmPasswordPolicyExcerpt extends AbstractDtoProjection {

}
