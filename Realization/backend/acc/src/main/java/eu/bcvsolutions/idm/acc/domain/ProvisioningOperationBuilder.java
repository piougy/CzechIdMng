package eu.bcvsolutions.idm.acc.domain;

import java.util.UUID;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;

/**
 * {@link SysProvisioningOperation} builder
 * 
 * @author Radek Tomiška
 *
 */
public class ProvisioningOperationBuilder {

	private ProvisioningOperationType operationType;
	private SysSystem system;
	private IcConnectorObject connectorObject;
	private SystemEntityType entityType;
	private UUID entityIdentifier;
	private String systemEntityUid;
	
	public ProvisioningOperationBuilder setOperationType(ProvisioningOperationType operationType) {
		this.operationType = operationType;
		return this;
	}
	
	public ProvisioningOperationBuilder setSystem(SysSystem system) {
		this.system = system;
		return this;
	}
	
	public ProvisioningOperationBuilder setConnectorObject(IcConnectorObject connectorObject) {
		this.connectorObject = connectorObject;
		return this;
	}
	
	public ProvisioningOperationBuilder setEntityType(SystemEntityType entityType) {
		this.entityType = entityType;
		return this;
	}
	
	public ProvisioningOperationBuilder setEntityIdentifier(UUID entityIdentifier) {
		this.entityIdentifier = entityIdentifier;
		return this;
	}
	
	public ProvisioningOperationBuilder setSystemEntityUid(String systemEntityUid) {
		this.systemEntityUid = systemEntityUid;
		return this;
	}
	
	/**
	 * Returns newly constructed SysProvisioningOperation object.
	 * 
	 * @return
	 */
	public SysProvisioningOperation build() {
		SysProvisioningOperation provisioningOperation = new SysProvisioningOperation();
		provisioningOperation.setOperationType(operationType);
		provisioningOperation.setConnectorObject(connectorObject);
		provisioningOperation.setSystem(system);
		provisioningOperation.setSystemEntityUid(systemEntityUid);
		provisioningOperation.setEntityType(entityType);
		provisioningOperation.setEntityIdentifier(entityIdentifier);
		return provisioningOperation;
	}
}
