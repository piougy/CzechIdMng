package eu.bcvsolutions.idm.acc.event.processor.provisioning;

import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperation;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.ResultState;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningResult;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.notification.service.api.NotificationManager;

/**
 * Execute provisioning
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractProvisioningProcessor extends AbstractEntityEventProcessor<SysProvisioningOperation> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PrepareAccountProcessor.class);
	protected final IcConnectorFacade connectorFacade;
	protected final SysSystemService systemService;
	private final NotificationManager notificationManager;
	private final SysProvisioningOperationRepository provisioningOperationRepository;
	
	public AbstractProvisioningProcessor(
			IcConnectorFacade connectorFacade,
			SysSystemService systemService,
			NotificationManager notificationManager,
			SysProvisioningOperationRepository provisioningOperationRepository,
			ProvisioningOperationType... provisioningOperationType) {
		super(provisioningOperationType);
		//
		Assert.notNull(connectorFacade);
		Assert.notNull(systemService);
		Assert.notNull(notificationManager);
		Assert.notNull(provisioningOperationRepository);
		//
		this.connectorFacade = connectorFacade;
		this.systemService = systemService;
		this.notificationManager = notificationManager;
		this.provisioningOperationRepository = provisioningOperationRepository;
	}
	
	/**
	 * Execute provisioning operation
	 * 
	 * @param provisioningOperation
	 * @param connectorConfig
	 */
	protected abstract void processInternal(ProvisioningOperation provisioningOperation, IcConnectorConfiguration connectorConfig);
	
	/**
	 * Prepare provisioning operation execution
	 */
	@Override
	public EventResult<SysProvisioningOperation> process(EntityEvent<SysProvisioningOperation> event) {		
		SysProvisioningOperation provisioningOperation = event.getContent();
		SysSystem system = provisioningOperation.getSystem();
		IcObjectClass objectClass = provisioningOperation.getProvisioningContext().getConnectorObject().getObjectClass();
		LOG.debug("Start provisioning operation [{}] for object with uid [{}] and connector object [{}]", 
				provisioningOperation.getOperationType(),
				provisioningOperation.getSystemEntityUid(),
				objectClass.getType());
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
		
		try {
			processInternal(provisioningOperation, connectorConfig);
			//
			ResultModel resultModel = new DefaultResultModel(
					AccResultCode.PROVISIONING_SUCCEED, 
					ImmutableMap.of(
							"name", provisioningOperation.getSystemEntityUid(), 
							"system", system.getName(),
							"operationType", provisioningOperation.getOperationType(),
							"objectClass", objectClass.getType()));
			provisioningOperation.getRequest().setResult(new SysProvisioningResult.Builder(ResultState.EXECUTED).setModel(resultModel).build()); // TODO: code
			provisioningOperationRepository.save(provisioningOperation);
			//
			LOG.debug(resultModel.toString());
			//
			notificationManager.send(AccModuleDescriptor.TOPIC_PROVISIONING, new IdmMessage.Builder().setModel(resultModel).build());
		} catch (Exception ex) {
			ResultModel resultModel = new DefaultResultModel(AccResultCode.PROVISIONING_FAILED, 
					ImmutableMap.of(
							"name", provisioningOperation.getSystemEntityUid(), 
							"system", system.getName(),
							"operationType", provisioningOperation.getOperationType(),
							"objectClass", objectClass.getType()));			
			LOG.error(resultModel.toString(), ex);
			provisioningOperation.getRequest().setResult(
					new SysProvisioningResult.Builder(ResultState.EXCEPTION).setModel(resultModel).setCause(ex).build());
			//
			provisioningOperationRepository.save(provisioningOperation);
			//
			notificationManager.send(
					AccModuleDescriptor.TOPIC_PROVISIONING, 
					new IdmMessage.Builder().setModel(resultModel).build());
		}
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// default order - 0 - its default implementation
		return CoreEvent.DEFAULT_ORDER;
	}
}
