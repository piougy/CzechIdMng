package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdentityDto;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.impl.IcUidAttributeImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.notification.service.api.NotificationManager;
import eu.bcvsolutions.idm.security.api.service.SecurityService;

/**
 * Provisioning - update operation
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class ProvisioningUpdateProcessor extends AbstractProvisioningProcessor {
	
	@Autowired
	private NotificationManager notificationManager;
	@Autowired
	private SecurityService securityService;
	@Autowired
	private IdmIdentityService identityService;
	
	@Autowired
	public ProvisioningUpdateProcessor(
			IcConnectorFacade connectorFacade,
			SysSystemService systemService) {
		super(ProvisioningOperationType.UPDATE, connectorFacade, systemService);
	}

	@Override
	public void processInternal(SysProvisioningOperation provisioningOperation, IcConnectorConfiguration connectorConfig) {	
		IcUidAttribute uidAttribute = new IcUidAttributeImpl(null, provisioningOperation.getSystemEntityUid(), null);
		connectorFacade.updateObject(provisioningOperation.getSystem().getConnectorKey(), connectorConfig,
				provisioningOperation.getConnectorObject().getObjectClass(), uidAttribute, provisioningOperation.getConnectorObject().getAttributes());	
		IdentityDto currentIdentityDto = securityService.getAuthentication().getCurrentIdentity();
		
		if (currentIdentityDto.getId() != null) {
			notificationManager.send(
					"idm:websocket", 
					new IdmMessage(
							"code-welkej", 
							"Provisioning účtu [" + provisioningOperation.getSystemEntityUid() + "] na systém [" + provisioningOperation.getSystem().getName() + "] úspěšně proběhl."
							), 
					identityService.get(currentIdentityDto.getId()));
		}
	}
}
