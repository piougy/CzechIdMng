package eu.bcvsolutions.idm.notification.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationConfiguration;
import eu.bcvsolutions.idm.notification.repository.IdmNotificationConfigurationRepository;
import eu.bcvsolutions.idm.notification.service.api.IdmNotificationConfigurationService;

/**
 * Configuration for notification routing
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmNotificationConfigurationService extends AbstractReadWriteEntityService<IdmNotificationConfiguration, EmptyFilter> implements IdmNotificationConfigurationService {
	
	@Autowired
	public DefaultIdmNotificationConfigurationService(IdmNotificationConfigurationRepository repository) {
		super(repository);		
	}

}
