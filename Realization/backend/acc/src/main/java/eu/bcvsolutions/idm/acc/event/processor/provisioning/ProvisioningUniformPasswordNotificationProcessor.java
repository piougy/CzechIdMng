package eu.bcvsolutions.idm.acc.event.processor.provisioning;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.Maps;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.acc.service.api.UniformPasswordManager;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

/**
 * Processor adds system entity ID to system entity for uniform password. This processor doesn't send notification. Notification will be send after sync ends.
 *
 * @author Vít Švanda
 * @since 11.0.0
 */
@Component(ProvisioningUniformPasswordNotificationProcessor.PROCESSOR_NAME)
@Description("Processor adds system entity ID to system entity for uniform password. This processor doesn't send notification. Notification will be send after sync ends.")
public class ProvisioningUniformPasswordNotificationProcessor extends AbstractEntityEventProcessor<SysProvisioningOperationDto> {

	public static final String PROCESSOR_NAME = "provisioning-common-password-notification-processor";

	@Autowired
	private UniformPasswordManager uniformPasswordManager;
	@Autowired
	private EntityStateManager entityStateManager;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private AccAccountService accountService;


	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public boolean conditional(EntityEvent<SysProvisioningOperationDto> event) {
		if (!super.conditional(event)) {
			return false;
		}
		SysProvisioningOperationDto provisioningOperation = event.getContent();

		// Uniform password notification can be send, only when account is created => update can be switched to create, if target account does not exist.
		// @see PrepareConnectorObjectProcessor
		if (provisioningOperation.getOperationType() != ProvisioningEventType.CREATE) {
			return false;
		}
		
		if (provisioningOperation.getSystem() == null || provisioningOperation.getSystemEntity() == null) {
			return false;
		}

		// Check if this system supports change of a password.
		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setSystemEntityId(provisioningOperation.getSystemEntity());
		accountFilter.setSystemId(provisioningOperation.getSystem());
		accountFilter.setSupportChangePassword(Boolean.TRUE);

		long count = accountService.count(accountFilter);
		if (count == 0) {
			// System doesn't support a password change!
			return false;
		}

		// Uniform password notification can be send only if provisioning operation ended successfully!
		return OperationState.EXECUTED == provisioningOperation.getResultState();
	}

	@Override
	@SuppressWarnings("unchecked")
	public EventResult<SysProvisioningOperationDto> process(EntityEvent<SysProvisioningOperationDto> event) {
		SysProvisioningOperationDto provisioningOperation = event.getContent();
		IdmIdentityDto identityDto = null;
		if (provisioningOperation.getEntityIdentifier() != null && SystemEntityType.IDENTITY == provisioningOperation.getEntityType()) {
			identityDto = identityService.get(provisioningOperation.getEntityIdentifier());
		}

		if (identityDto != null && identityDto.getState() != IdentityState.CREATED) {
			// Notification will be send after end of sync.
			if (identityDto.getId() != null) {
				IdmEntityStateDto uniformPasswordState = uniformPasswordManager
						.getEntityState(identityDto.getId(), identityDto.getClass(), provisioningOperation.getTransactionId());
				UUID systemId = provisioningOperation.getSystem();
				if (systemId != null) {
					SysSystemDto systemDto = systemService.get(systemId);
					if (systemDto != null) {
						if (uniformPasswordState != null) {
							// Add name of system to the entity state.
							uniformPasswordManager.addSystemNameToEntityState(uniformPasswordState, systemDto.getCode());

							ResultModel model = uniformPasswordState.getResult().getModel();
							// Create new parameters for entity state.
							HashMap<String, Object> newParameters = Maps.newHashMap(model.getParameters());

							// Add system entity ID to entity state for uniform password (could be used in bulk notification).
							UUID systemEntityId = provisioningOperation.getSystemEntity();
							if (systemEntityId != null) {
								Object successSystemEntitiesObj = model.getParameters().get(UniformPasswordManager.SUCCESS_SYSTEM_ENTITIES);
								Set<UUID> successSystemEntities = null;
								if (successSystemEntitiesObj instanceof Set) {
									successSystemEntities = (Set<UUID>) successSystemEntitiesObj;
								} else {
									successSystemEntities = Sets.newHashSet();
								}
								successSystemEntities.add(systemEntityId);
								newParameters.put(UniformPasswordManager.SUCCESS_SYSTEM_ENTITIES, successSystemEntities);
							}

							// Save entity state with new parameters.
							uniformPasswordState.getResult().setModel(new DefaultResultModel(CoreResultCode.IDENTITY_UNIFORM_PASSWORD, newParameters));
							entityStateManager.saveState(null, uniformPasswordState);
						}
					}
				}
			}
		}
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return 900;
	}
}
