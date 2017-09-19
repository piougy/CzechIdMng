package eu.bcvsolutions.idm.acc.event.processor.provisioning;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperation;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcPasswordAttribute;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.impl.IcPasswordAttributeImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Provisioning - create operation
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Executes provisioning operation on connector facade. Depends on [" + PrepareConnectorObjectProcessor.PROCESSOR_NAME + "] result operation type [CREATE].")
public class ProvisioningCreateProcessor extends AbstractProvisioningProcessor {

	public static final String PROCESSOR_NAME = "provisioning-create-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ProvisioningCreateProcessor.class);
	private final IdmPasswordPolicyService passwordPolicyService;
	private final SysProvisioningOperationService provisioningOperationService;
	private final SysSystemService systemService;
	
	@Autowired
	public ProvisioningCreateProcessor(
			IcConnectorFacade connectorFacade,
			SysSystemService systemService,
			SysProvisioningOperationService provisioningOperationService,
			IdmPasswordPolicyService passwordPolicyService,
			SysSystemEntityService systemEntityService) {
		super(connectorFacade, systemService, provisioningOperationService, systemEntityService, 
				ProvisioningEventType.CREATE, ProvisioningEventType.UPDATE);
		//
		Assert.notNull(provisioningOperationService);
		Assert.notNull(systemService);
		//
		this.passwordPolicyService = passwordPolicyService;
		this.provisioningOperationService = provisioningOperationService;
		this.systemService = systemService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public IcUidAttribute processInternal(SysProvisioningOperationDto provisioningOperation, IcConnectorConfiguration connectorConfig) {
		// get system for password policy
		SysSystemDto system = systemService.get(provisioningOperation.getSystem());
		// execute provisioning
		IcConnectorObject connectorObject = provisioningOperation.getProvisioningContext().getConnectorObject();
		for (IcAttribute attribute : connectorObject.getAttributes()) {
			// if attribute is password and his value is empty, generate new password
			if(attribute instanceof IcPasswordAttribute 
					&& ((IcPasswordAttribute) attribute).getPasswordValue() == null) {
				UUID passwordPolicyId = system.getPasswordPolicyGenerate();
				//
				String password = null;
				if (passwordPolicyId == null) {
					LOG.debug("Generate password policy for system [{}], not found. Password will be generate by default password policy", system.getCode());
					password = passwordPolicyService.generatePasswordByDefault();
				} else {
					LOG.debug("Generate password policy for system  [{}] found", system.getCode());
					password = passwordPolicyService.generatePassword(passwordPolicyService.get(passwordPolicyId));
				}
				//
				connectorObject.getAttributes().remove(attribute);
				connectorObject.getAttributes().add(new IcPasswordAttributeImpl(ProvisioningService.PASSWORD_SCHEMA_PROPERTY_NAME, new GuardedString(password)));
				break;
			}
		}
		//
		IcUidAttribute icUid = connectorFacade.createObject(system.getConnectorInstance(), connectorConfig,
				connectorObject.getObjectClass(), connectorObject.getAttributes());
		//
		// set connector object back to provisioning context
		provisioningOperation.getProvisioningContext().setConnectorObject(connectorObject);
		provisioningOperation = provisioningOperationService.save(provisioningOperation); // has to be first - we need to replace guarded strings before systemEntityService.save(systemEntity)
		return icUid;
	}
	
	@Override
	public boolean supports(EntityEvent<?> entityEvent) {
		if(!super.supports(entityEvent)) {
			return false;
		}
		return ProvisioningEventType.CREATE == ((ProvisioningOperation)entityEvent.getContent()).getOperationType();
	}
}