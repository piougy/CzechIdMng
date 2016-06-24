package eu.bcvsolutions.idm.core.model.repository.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Trimmed identity - projection is used in collections (search etc.)
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@Projection(name = "excerpt", types = IdmIdentity.class)
public interface IdmIdentityExcerpt extends AbstractDtoProjection {
	
	String getUsername();
	
	boolean isDisabled();
	
	String getFirstName();
	
	String getLastName();
	
	String getEmail();
	
	String getPhone();
	
	String getTitleBefore();
	
	String getTitleAfter();
	
	String getDescription();
}
