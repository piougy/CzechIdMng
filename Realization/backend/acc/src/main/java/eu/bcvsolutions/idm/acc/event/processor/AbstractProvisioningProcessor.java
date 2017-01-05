package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Execute provisioning
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractProvisioningProcessor extends AbstractEntityEventProcessor<SysProvisioningOperation> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ProvisioningCreateProcessor.class);
	protected final IcConnectorFacade connectorFacade;
	protected final SysSystemService systemService;
	
	public AbstractProvisioningProcessor(
			ProvisioningOperationType provisioningOperationType,
			IcConnectorFacade connectorFacade,
			SysSystemService systemService) {
		super(provisioningOperationType);
		//
		Assert.notNull(connectorFacade);
		Assert.notNull(systemService);
		//
		this.connectorFacade = connectorFacade;
		this.systemService = systemService;
	}
	
	/**
	 * Execute provisioning operation
	 * 
	 * @param provisioningOperation
	 * @param connectorConfig
	 */
	protected abstract void processInternal(SysProvisioningOperation provisioningOperation, IcConnectorConfiguration connectorConfig);
	
	/**
	 * Prepare provisioning operation execution
	 */
	@Override
	public EventResult<SysProvisioningOperation> process(EntityEvent<SysProvisioningOperation> event) {		
		SysProvisioningOperation provisioningOperation = event.getContent();
		SysSystem system = provisioningOperation.getSystem();
		LOG.debug("Start provisioning operation [{}] for object with uid [{}] and connector object [{}]", 
				provisioningOperation.getOperationType(),
				provisioningOperation.getSystemEntityUid(),
				provisioningOperation.getConnectorObject().getObjectClass().getType());
		//
		// Find connector identification persisted in system
		if (system.getConnectorKey() == null) {
			throw new ProvisioningException(AccResultCode.CONNECTOR_KEY_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", provisioningOperation.getSystem().getName()));
		}
		// load connector configuration
		IcConnectorConfiguration connectorConfig = systemService.getConnectorConfiguration(provisioningOperation.getSystem());
		if (connectorConfig == null) {
			throw new ProvisioningException(AccResultCode.CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}
		//
		processInternal(provisioningOperation, connectorConfig);
		//
		LOG.debug("Provisioning operation [{}] for object with uid [{}] and connector object [{}] is completed", 
				provisioningOperation.getOperationType(), 
				provisioningOperation.getSystemEntityUid(),
				provisioningOperation.getConnectorObject().getObjectClass().getType());
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// default order - 0 - its default implementation
		return CoreEvent.DEFAULT_ORDER;
	}
}
