package eu.bcvsolutions.idm.core.model.repository.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.core.model.domain.IdmRoleType;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * Trimmed role - projection is used in collections (search etc.)
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@Projection(name = "excerpt", types = IdmRole.class)
public interface IdmRoleExcerpt extends AbstractDtoProjection {
	
	String getName();
	
	boolean isDisabled();
	
	boolean isApprovable();
	
	IdmRoleType getRoleType();
}
