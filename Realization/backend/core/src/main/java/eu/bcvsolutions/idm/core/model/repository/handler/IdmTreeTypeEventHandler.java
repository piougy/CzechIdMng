package eu.bcvsolutions.idm.core.model.repository.handler;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;

/**
 * Securing types
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
@RepositoryEventHandler(IdmTreeType.class)
public class IdmTreeTypeEventHandler {
	
	@HandleBeforeSave
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.TREETYPE_WRITE + "')")
	public void handleBeforeSave(IdmTreeType type) {
		// nothing just security
	}
	
	@HandleBeforeCreate
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.TREETYPE_WRITE + "')")
	public void handleBeforeCreate(IdmTreeType type) {
		// nothing just security
	}
	
	@HandleBeforeDelete
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.TREETYPE_DELETE + "')")
	public void handleBeforeDelete(IdmTreeType type) {	
		// nothing just security
	}
}
