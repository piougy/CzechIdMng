package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.notification.service.api.NotificationManager;

/**
 * Provisioning - create operation
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class ProvisioningCreateProcessor extends AbstractProvisioningProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ProvisioningCreateProcessor.class);
	private final SysSystemEntityService systemEntityService;
	
	@Autowired
	public ProvisioningCreateProcessor(
			IcConnectorFacade connectorFacade,
			SysSystemService systemService,
			SysSystemEntityService systemEntityService,
			NotificationManager notificationManager,
			SysProvisioningOperationRepository provisioningOperationRepository) {
		super(connectorFacade, systemService, notificationManager, provisioningOperationRepository, 
				ProvisioningOperationType.CREATE, ProvisioningOperationType.UPDATE);
		//
		Assert.notNull(systemEntityService);
		//
		this.systemEntityService = systemEntityService;
	}

	@Override
	public void processInternal(SysProvisioningOperation provisioningOperation, IcConnectorConfiguration connectorConfig) {		
		// execute provisioning
		IcConnectorObject connectorObject = provisioningOperation.getProvisioningContext().getConnectorObject();
		IcUidAttribute icUid = connectorFacade.createObject(provisioningOperation.getSystem().getConnectorKey(), connectorConfig,
				connectorObject.getObjectClass(), connectorObject.getAttributes());
		//
		// update system entity, when identifier on target system differs
		if (icUid != null && icUid.getUidValue() != null) {
			SysSystemEntity systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(
					provisioningOperation.getSystem(), 
					provisioningOperation.getEntityType(), 
					provisioningOperation.getSystemEntityUid());
			if (systemEntity == null) {
				systemEntity = new SysSystemEntity();
				systemEntity.setEntityType(provisioningOperation.getEntityType());
				systemEntity.setSystem(provisioningOperation.getSystem());
				systemEntity.setUid(icUid.getUidValue());
				systemEntity = systemEntityService.save(systemEntity);
				LOG.debug("New system entity with uid [{}] was created", systemEntity.getUid());
			} else if(!systemEntity.getUid().equals(icUid.getUidValue())){
				systemEntity.setUid(icUid.getUidValue());
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
		return ProvisioningOperationType.CREATE.equals(((SysProvisioningOperation)entityEvent.getContent()).getOperationType());
	}
}