package eu.bcvsolutions.idm.acc.rest.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntityHandling;
import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * Role system excerpt
 * 
 * 
 * @author Svanda
 *
 */
@Projection(name = "excerpt", types = SysRoleSystem.class)
public interface SysRoleSystemExcerpt extends AbstractDtoProjection {
	
	IdmRole getRole();

	SysSystem getSystem();

	SysSystemEntityHandling getSystemEntityHandling();
}