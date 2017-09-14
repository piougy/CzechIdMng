package eu.bcvsolutions.idm.acc.event.processor.provisioning;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
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
	
	private final SysSystemService systemService;
	
	@Autowired
	public ProvisioningDeleteProcessor(
			IcConnectorFacade connectorFacade,
			SysSystemService systemService,
			SysProvisioningOperationService provisioningOperationService,
			SysSystemEntityService systemEntityService) {
		super(connectorFacade, systemService, provisioningOperationService, systemEntityService, ProvisioningEventType.DELETE);
		//
		Assert.notNull(systemService);
		//
		this.systemService = systemService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public IcUidAttribute processInternal(SysProvisioningOperationDto provisioningOperation, IcConnectorConfiguration connectorConfig) {
		SysSystemDto system = systemService.get(provisioningOperation.getSystem());
		IcConnectorInstance connectorInstance = system.getConnectorInstance();
		IcUidAttribute uidAttribute = new IcUidAttributeImpl(null, provisioningOperation.getSystemEntityUid(), null);
		IcObjectClass objectClass = provisioningOperation.getProvisioningContext().getConnectorObject().getObjectClass();
		//
		IcConnectorObject connectorObject = connectorFacade.readObject(connectorInstance, connectorConfig, objectClass, uidAttribute);
		if (connectorObject != null) {
			connectorFacade.deleteObject(connectorInstance, connectorConfig, objectClass, uidAttribute);
		}
		return null;
	}
}
