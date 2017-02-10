package eu.bcvsolutions.idm.acc.event.processor.provisioning;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.notification.service.api.NotificationManager;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.impl.IcUidAttributeImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Provisioning - update operation
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Executes provisioning operation on connector facade. Depends on [" + PrepareConnectorObjectProcessor.PROCESSOR_NAME + "] result operation type [UPDATE].")
public class ProvisioningUpdateProcessor extends AbstractProvisioningProcessor {
	
	public static final String PROCESSOR_NAME = "provisioning-update-processor";
	
	@Autowired
	public ProvisioningUpdateProcessor(
			IcConnectorFacade connectorFacade,
			SysSystemService systemService,
			NotificationManager notificationManager,
			SysProvisioningOperationService provisioningOperationService) {
		super(connectorFacade, systemService, provisioningOperationService, 
				ProvisioningEventType.CREATE, ProvisioningEventType.UPDATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public void processInternal(SysProvisioningOperation provisioningOperation, IcConnectorConfiguration connectorConfig) {
		IcUidAttribute uidAttribute = new IcUidAttributeImpl(null, provisioningOperation.getSystemEntityUid(), null);
		IcConnectorObject connectorObject = provisioningOperation.getProvisioningContext().getConnectorObject();
		if (!connectorObject.getAttributes().isEmpty()) { // TODO: appropriate message - provisioning is not executed - attributes don't change
			connectorFacade.updateObject(provisioningOperation.getSystem().getConnectorInstance(), connectorConfig,
					connectorObject.getObjectClass(), uidAttribute, connectorObject.getAttributes());
		}
	}
	
	@Override
	public boolean supports(EntityEvent<?> entityEvent) {
		if(!super.supports(entityEvent)) {
			return false;
		}
		return ProvisioningEventType.UPDATE == ((ProvisioningOperation)entityEvent.getContent()).getOperationType();
	}
}