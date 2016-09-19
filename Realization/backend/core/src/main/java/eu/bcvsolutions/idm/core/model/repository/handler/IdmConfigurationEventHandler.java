package eu.bcvsolutions.idm.core.model.repository.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration;
import eu.bcvsolutions.idm.core.model.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.rest.impl.IdmConfigurationController;

/**
 * Handles configuration active operations and redirect them to {@link IdmConfigurationService}.
 * 
 * TODO: maybe will be better annd custom endpoint to {@link IdmConfigurationController} instead.
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 */
@Component
@RepositoryEventHandler(IdmConfiguration.class)
public class IdmConfigurationEventHandler {
	
	@Autowired
	private IdmConfigurationService configurationService;
	
	@HandleBeforeSave
	public void handleBeforeSave(IdmConfiguration configuration) {
		// configurationService.setConfiguration(configuration);
		// throw new RestApplicationException(CoreResultCode.OK, "OK.");
	}
	
	@HandleBeforeCreate
	public void handleBeforeCreate(IdmConfiguration configuration) {	
		// configurationService.setConfiguration(configuration);
		// throw new RestApplicationException(CoreResultCode.OK, "OK.");
	}
	
	@HandleBeforeDelete
	public void handleBeforeDelete(IdmConfiguration configuration) {	
		// TODO: configurationService.delete ... should be used for clear cache
	}
}
