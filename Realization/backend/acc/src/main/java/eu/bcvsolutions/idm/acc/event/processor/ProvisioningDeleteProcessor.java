package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.impl.IcUidAttributeImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.notification.service.api.NotificationManager;

/**
 * Provisioning - delete operation
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class ProvisioningDeleteProcessor extends AbstractProvisioningProcessor {
	
	@Autowired
	public ProvisioningDeleteProcessor(
			IcConnectorFacade connectorFacade,
			SysSystemService systemService,
			NotificationManager notificationManager,
			SysProvisioningOperationRepository provisioningOperationRepository) {
		super(ProvisioningOperationType.DELETE, connectorFacade, systemService, notificationManager, provisioningOperationRepository);
	}

	@Override
	public void processInternal(SysProvisioningOperation provisioningOperation, IcConnectorConfiguration connectorConfig) {;
		IcUidAttribute uidAttribute = new IcUidAttributeImpl(null, provisioningOperation.getSystemEntityUid(), null);
		connectorFacade.deleteObject(
				provisioningOperation.getSystem().getConnectorKey(), 
				connectorConfig, 
				provisioningOperation.getConnectorObject().getObjectClass(), 
				uidAttribute);
	}
}
