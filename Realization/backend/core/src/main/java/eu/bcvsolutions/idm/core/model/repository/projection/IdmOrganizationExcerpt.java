package eu.bcvsolutions.idm.core.model.repository.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.core.model.entity.IdmOrganization;

/**
 * Trimmed organization - projection is used in collections (search etc.)
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@Projection(name = "excerpt", types = IdmOrganization.class)
public interface IdmOrganizationExcerpt extends AbstractDtoProjection {
	
	String getName();
	
	boolean isDisabled();
}
