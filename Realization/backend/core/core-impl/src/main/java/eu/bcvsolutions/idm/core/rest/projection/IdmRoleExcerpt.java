package eu.bcvsolutions.idm.core.rest.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.core.api.domain.RoleType;
import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * Trimmed role - projection is used in collections (search etc.)
 * 
 * @author Radek Tomiška 
 *
 */
@Projection(name = "excerpt", types = IdmRole.class)
public interface IdmRoleExcerpt extends AbstractDtoProjection {
	
	String getName();
	
	boolean isDisabled();
	
	RoleType getRoleType();
	
	String getDescription();
}
