package eu.bcvsolutions.idm.acc.event.processor.provisioning;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.impl.IcUidAttributeImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Provisioning - delete operation
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Executes provisioning operation on connector facade.")
public class ProvisioningDeleteProcessor extends AbstractProvisioningProcessor {
	
	public static final String PROCESSOR_NAME = "provisioning-delete-processor";
	
	@Autowired
	public ProvisioningDeleteProcessor(
			IcConnectorFacade connectorFacade,
			SysSystemService systemService,
			SysProvisioningOperationService provisioningOperationService) {
		super(connectorFacade, systemService, provisioningOperationService, ProvisioningEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public void processInternal(SysProvisioningOperation provisioningOperation, IcConnectorConfiguration connectorConfig) {;
		IcConnectorInstance connectorInstance = provisioningOperation.getSystem().getConnectorInstance();
		IcUidAttribute uidAttribute = new IcUidAttributeImpl(null, provisioningOperation.getSystemEntityUid(), null);
		IcObjectClass objectClass = provisioningOperation.getProvisioningContext().getConnectorObject().getObjectClass();
		//
		IcConnectorObject connectorObject = connectorFacade.readObject(connectorInstance, connectorConfig, objectClass, uidAttribute);
		if (connectorObject != null) {
			connectorFacade.deleteObject(connectorInstance, connectorConfig, objectClass, uidAttribute);
		}
	}
}
