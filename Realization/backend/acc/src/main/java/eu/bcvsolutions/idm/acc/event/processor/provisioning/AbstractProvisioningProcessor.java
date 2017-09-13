package eu.bcvsolutions.idm.acc.event.processor.provisioning;

import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Execute provisioning
 * 
 * @author Radek Tomiška
 *
 */
@Enabled(AccModuleDescriptor.MODULE_ID)
public abstract class AbstractProvisioningProcessor extends AbstractEntityEventProcessor<SysProvisioningOperationDto> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractProvisioningProcessor.class);
	protected final IcConnectorFacade connectorFacade;
	protected final SysSystemService systemService;
	private final SysSystemEntityService systemEntityService;
	private final SysProvisioningOperationService provisioningOperationService;
	
	public AbstractProvisioningProcessor(
			IcConnectorFacade connectorFacade,
			SysSystemService systemService,
			SysProvisioningOperationService provisioningOperationService,
			SysSystemEntityService systemEntityService,
			ProvisioningEventType... provisioningOperationType) {
		super(provisioningOperationType);
		//
		Assert.notNull(connectorFacade);
		Assert.notNull(systemService);
		Assert.notNull(provisioningOperationService);
		Assert.notNull(systemEntityService);
		//
		this.connectorFacade = connectorFacade;
		this.systemService = systemService;
		this.provisioningOperationService = provisioningOperationService;
		this.systemEntityService = systemEntityService;
	}
	
	/**
	 * Execute provisioning operation
	 * 
	 * @param provisioningOperation
	 * @param connectorConfig
	 */
	protected abstract IcUidAttribute processInternal(SysProvisioningOperationDto provisioningOperation, IcConnectorConfiguration connectorConfig);
	
	/**
	 * Prepare provisioning operation execution
	 */
	@Override
	public EventResult<SysProvisioningOperationDto> process(EntityEvent<SysProvisioningOperationDto> event) {				
		SysProvisioningOperationDto provisioningOperation = event.getContent();
		SysSystemDto system = systemService.get(provisioningOperation.getSystem());
		IcConnectorObject connectorObject = provisioningOperation.getProvisioningContext().getConnectorObject();
		IcObjectClass objectClass = connectorObject.getObjectClass();
		LOG.debug("Start provisioning operation [{}] for object with uid [{}] and connector object [{}]", 
				provisioningOperation.getOperationType(),
				provisioningOperation.getSystemEntityUid(),
				objectClass.getType());
		//
		// Find connector identification persisted in system
		if (system.getConnectorKey() == null) {
			throw new ProvisioningException(AccResultCode.CONNECTOR_KEY_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}
		// load connector configuration
		IcConnectorConfiguration connectorConfig = systemService.getConnectorConfiguration(systemService.get(provisioningOperation.getSystem()));
		if (connectorConfig == null) {
			throw new ProvisioningException(AccResultCode.CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}
		//
		try {
			provisioningOperation = provisioningOperationService.save(provisioningOperation);
			// convert confidential string to guarded strings before provisioning realization
			connectorObject = provisioningOperationService.getFullConnectorObject(provisioningOperation);
			provisioningOperation.getProvisioningContext().setConnectorObject(connectorObject);
			//
			IcUidAttribute resultUid = processInternal(provisioningOperation, connectorConfig);
			// update system entity, when identifier on target system differs
			if (resultUid != null && resultUid.getUidValue() != null) {
				SysSystemEntityDto systemEntity = systemEntityService.get(provisioningOperation.getSystemEntity());
				if(!systemEntity.getUid().equals(resultUid.getUidValue()) || systemEntity.isWish()) {
					systemEntity.setUid(resultUid.getUidValue());
					systemEntity.setWish(false);
					systemEntity = systemEntityService.save(systemEntity);
					LOG.info("UID was changed. System entity with uid [{}] was updated", systemEntity.getUid());
				}
			}
			
			provisioningOperationService.handleSuccessful(provisioningOperation);
		} catch (Exception ex) {
			provisioningOperationService.handleFailed(provisioningOperation, ex);
		}
		// set operation back to content
		event.setContent(provisioningOperation);
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// default order - 0 - its default implementation
		return CoreEvent.DEFAULT_ORDER;
	}
}
