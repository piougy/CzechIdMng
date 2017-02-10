package eu.bcvsolutions.idm.acc.event.processor.provisioning;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.domain.ProvisioningOperation;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.acc.service.impl.DefaultProvisioningService;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordPolicyService;
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
	private final SysSystemEntityService systemEntityService;
	private final IdmPasswordPolicyService passwordPolicyService;
	private final SysProvisioningOperationService provisioningOperationService;
	
	@Autowired
	public ProvisioningCreateProcessor(
			IcConnectorFacade connectorFacade,
			SysSystemService systemService,
			SysSystemEntityService systemEntityService,
			SysProvisioningOperationService provisioningOperationService,
			IdmPasswordPolicyService passwordPolicyService) {
		super(connectorFacade, systemService, provisioningOperationService, 
				ProvisioningEventType.CREATE, ProvisioningEventType.UPDATE);
		//
		Assert.notNull(systemEntityService);
		Assert.notNull(provisioningOperationService);
		//
		this.passwordPolicyService = passwordPolicyService;
		this.systemEntityService = systemEntityService;
		this.provisioningOperationService = provisioningOperationService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public void processInternal(SysProvisioningOperation provisioningOperation, IcConnectorConfiguration connectorConfig) {
		// execute provisioning
		IcConnectorObject connectorObject = provisioningOperation.getProvisioningContext().getConnectorObject();		
		// 	
		for (IcAttribute attribute : connectorObject.getAttributes()) {
			// if attribute is password and his value is empty, generate new password
			if(attribute instanceof IcPasswordAttribute 
					&& ((IcPasswordAttribute) attribute).getPasswordValue() == null) {
				IdmPasswordPolicy passwordPolicy = provisioningOperation.getSystem().getPasswordPolicyGenerate();
				//
				String password = null;
				if (passwordPolicy == null) {
					password = passwordPolicyService.generatePasswordByDefault();
				} else {
					password = passwordPolicyService.generatePassword(passwordPolicy);
				}
				//
				connectorObject.getAttributes().remove(attribute);
				connectorObject.getAttributes().add(new IcPasswordAttributeImpl(DefaultProvisioningService.PASSWORD_SCHEMA_PROPERTY_NAME, new GuardedString(password)));
				break;
			}
		}
		//
		IcUidAttribute icUid = connectorFacade.createObject(provisioningOperation.getSystem().getConnectorInstance(), connectorConfig,
				connectorObject.getObjectClass(), connectorObject.getAttributes());
		//
		provisioningOperationService.save(provisioningOperation); // has to be fist - we need to replace guarded strings before systemEntityService.save(systemEntity)
		//
		// update system entity, when identifier on target system differs
		if (icUid != null && icUid.getUidValue() != null) {
			SysSystemEntity systemEntity = provisioningOperation.getSystemEntity();
			if(!systemEntity.getUid().equals(icUid.getUidValue()) || systemEntity.isWish()) {
				systemEntity.setUid(icUid.getUidValue());
				systemEntity.setWish(false);
				systemEntity = systemEntityService.save(systemEntity);
				LOG.debug("New system entity with uid [{}] was updated", systemEntity.getUid());
			}
		}
	}
	
	@Override
	public boolean supports(EntityEvent<?> entityEvent) {
		if(!super.supports(entityEvent)) {
			return false;
		}
		return ProvisioningEventType.CREATE == ((ProvisioningOperation)entityEvent.getContent()).getOperationType();
	}
}