package eu.bcvsolutions.idm.acc.event.processor.provisioning;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.domain.ProvisioningOperation;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.impl.IcUidAttributeImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.notification.service.api.NotificationManager;

/**
 * Provisioning - update operation
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class ProvisioningUpdateProcessor extends AbstractProvisioningProcessor {
	
	@Autowired
	public ProvisioningUpdateProcessor(
			IcConnectorFacade connectorFacade,
			SysSystemService systemService,
			NotificationManager notificationManager,
			SysProvisioningOperationService provisioningOperationService) {
		super(connectorFacade, systemService, provisioningOperationService, 
				ProvisioningOperationType.CREATE, ProvisioningOperationType.UPDATE);
	}

	@Override
	public void processInternal(ProvisioningOperation provisioningOperation, IcConnectorConfiguration connectorConfig) {	
		IcUidAttribute uidAttribute = new IcUidAttributeImpl(null, provisioningOperation.getSystemEntityUid(), null);
		IcConnectorObject connectorObject = provisioningOperation.getProvisioningContext().getConnectorObject();
		if (!connectorObject.getAttributes().isEmpty()) { // TODO: appropriate message - provisioning is not executed - attributes don't change
			connectorFacade.updateObject(provisioningOperation.getSystem().getConnectorKey(), connectorConfig,
					connectorObject.getObjectClass(), uidAttribute, connectorObject.getAttributes());	
		}
	}
	
	@Override
	public boolean supports(EntityEvent<?> entityEvent) {
		if(!super.supports(entityEvent)) {
			return false;
		}
		return ProvisioningOperationType.UPDATE.equals(((ProvisioningOperation)entityEvent.getContent()).getOperationType());
	}
}