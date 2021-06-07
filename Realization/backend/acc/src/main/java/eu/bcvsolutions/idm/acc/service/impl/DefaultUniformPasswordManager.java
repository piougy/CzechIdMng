package eu.bcvsolutions.idm.acc.service.impl;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccUniformPasswordFilter;
import eu.bcvsolutions.idm.acc.service.api.AccUniformPasswordService;
import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.TransactionContextHolder;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.acc.service.api.UniformPasswordManager;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Manager for a uniform password of identity.
 *
 * @author Vít Švanda
 * @since 11.0.0
 */
@Service
public class DefaultUniformPasswordManager implements UniformPasswordManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultUniformPasswordManager.class);

	@Autowired
	private IdmPasswordPolicyService passwordPolicyService;
	@Autowired
	private ConfidentialStorage confidentialStorage;
	@Autowired
	private EntityStateManager entityStateManager;
	@Autowired
	private NotificationManager notificationManager;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private AccUniformPasswordService uniformPasswordService;


	@Override
	@Transactional
	public IdmEntityStateDto createEntityState(AbstractDto entityDto) {
		Assert.notNull(entityDto, "Entity cannot be null!");
		Assert.notNull(entityDto.getId(), "Entity ID cannot be null!");
		UUID transactionId = TransactionContextHolder.getContext().getTransactionId();
		
		IdmEntityStateDto entityState = this.getEntityState(entityDto.getId(), entityDto.getClass(), transactionId);
		if (entityState == null) {
			String entityString = entityDto.toString();
			Map<String, Serializable> properties = new HashMap<>();
			properties.put("entity", entityString);
			entityState = entityStateManager.createState(
					entityDto,
					OperationState.BLOCKED,
					CoreResultCode.IDENTITY_UNIFORM_PASSWORD,
					properties
			);
			LOG.debug("Uniform password entity state for entity [{}] and transaction [{}] created.", entityString, transactionId);
		}
		return entityState;
	}

	@Override
	@Transactional
	public void endUniformPasswordProcess(UUID transactionId) {
		Assert.notNull(transactionId, "Transaction cannot be null!");

		IdmEntityStateFilter entityStateFilter = new IdmEntityStateFilter();
		entityStateFilter.setTransactionId(transactionId);
		entityStateFilter.setResultCode(CoreResultCode.IDENTITY_UNIFORM_PASSWORD.getCode());
		entityStateFilter.setStates(Lists.newArrayList(OperationState.BLOCKED));

		List<IdmEntityStateDto> entityStateDtos = entityStateManager.findStates(entityStateFilter, null).getContent();

		// Send notification with password to identities where was uniform password used.
		entityStateDtos.stream()
				.filter(entityState -> IdmIdentity.class.getCanonicalName().equals(entityState.getOwnerType()))
				.filter(this::uniformPasswordUsed)
				.forEach(entityState -> {
					UUID identityId = entityState.getOwnerId();
					if (identityId != null) {
						IdmIdentityDto identityDto = identityService.get(identityId);
						if (identityDto != null) {
							// Send notification with uniform password.
							this.sendUniformPasswordNotification(identityDto, entityState);
						}
					}
				});

		// Remove all uniform password entity states for this transaction.
		entityStateDtos
				.forEach(entityState -> entityStateManager.deleteState(entityState));
	}
	
	@Override
	public boolean isSystemInUniformPasswordAgenda(UUID systemId) {
		Assert.notNull(systemId, "System ID cannot be null!");

		AccUniformPasswordFilter uniformPasswordFilter = new AccUniformPasswordFilter();
		uniformPasswordFilter.setSystemId(systemId);
		uniformPasswordFilter.setDisabled(Boolean.FALSE);
		long count = uniformPasswordService.count(uniformPasswordFilter);

		return count > 0;
	}

	@Override
	public AccUniformPasswordDto getUniformPasswordBySystem(UUID systemId) {
		Assert.notNull(systemId, "System ID cannot be null!");

		AccUniformPasswordFilter uniformPasswordFilter = new AccUniformPasswordFilter();
		uniformPasswordFilter.setSystemId(systemId);
		uniformPasswordFilter.setDisabled(Boolean.FALSE);
		return uniformPasswordService.find(uniformPasswordFilter, null)
				.getContent()
				.stream()
				.findFirst()
				.orElse(null);
	}

	@Override
	public boolean shouldBePasswordSetToIdM() {
		AccUniformPasswordFilter uniformPasswordFilter = new AccUniformPasswordFilter();
		uniformPasswordFilter.setChangeInIdM(Boolean.TRUE);
		uniformPasswordFilter.setDisabled(Boolean.FALSE);
		long count = uniformPasswordService.count(uniformPasswordFilter);

		return count > 0;
	}

	@Override
	@Transactional
	public GuardedString generateUniformPassword(UUID entityIdentifier, Class<? extends AbstractDto> entityType, UUID transactionId) {
		Assert.notNull(transactionId, "Transaction cannot be null!");
		Assert.notNull(entityIdentifier, "Entity identifier cannot be null!");
		Assert.notNull(entityType, "Entity type cannot be null!");

		IdmEntityStateDto entityStateDto = this.getEntityState(entityIdentifier, entityType, transactionId);

		if (entityStateDto != null) {
			GuardedString password = getPassword(entityStateDto);
			if (password == null || password.getValue().length == 0) {
				// TODO: how to generate password for all system policies.
				password = new GuardedString(passwordPolicyService.generatePasswordByDefault());
				confidentialStorage.saveGuardedString(entityStateDto, UNIFORM_PASSWORD_KEY, password);
			}
			// The uniform password was used, we need to mark it. 
			Map<String, Object> parameters = entityStateDto.getResult().getModel().getParameters();
			HashMap<String, Object> newParameters = Maps.newHashMap(parameters);
			newParameters.put(PASSWORD_USED, Boolean.TRUE);
			entityStateDto.getResult().setModel(new DefaultResultModel(CoreResultCode.IDENTITY_UNIFORM_PASSWORD, newParameters));
			entityStateManager.saveState(null, entityStateDto);
			
			return password;
		}

		return null;
	}

	@Override
	public IdmEntityStateDto getEntityState(UUID entityIdentifier, Class<? extends AbstractDto> entityType, UUID transactionId) {
		Assert.notNull(transactionId, "Transaction cannot be null!");
		Assert.notNull(entityIdentifier, "Entity identifier cannot be null!");
		Assert.notNull(entityType, "Entity type cannot be null!");

		IdmEntityStateFilter entityStateFilter = new IdmEntityStateFilter();
		entityStateFilter.setTransactionId(transactionId);
		entityStateFilter.setResultCode(CoreResultCode.IDENTITY_UNIFORM_PASSWORD.getCode());
		entityStateFilter.setOwnerType(entityStateManager.getOwnerType(entityType));
		entityStateFilter.setOwnerId(entityIdentifier);
		entityStateFilter.setStates(Lists.newArrayList(OperationState.BLOCKED));

		return entityStateManager.findStates(entityStateFilter, null)
				.getContent()
				.stream()
				.findFirst()
				.orElse(null);
	}

	private boolean uniformPasswordUsed(IdmEntityStateDto entityState) {
		ResultModel model = entityState.getResult().getModel();
		if (model != null) {
			Object passwordUsed = model.getParameters().get(PASSWORD_USED);
			return passwordUsed instanceof Boolean && (Boolean) passwordUsed;
		}
		return false;
	}

	/**
	 * Get uniform password from the confidential storage.
	 * 
	 * A potentially good place to override for use a static password.
	 */
	protected GuardedString getPassword(IdmEntityStateDto entityStateDto) {
		return confidentialStorage.getGuardedString(entityStateDto, UNIFORM_PASSWORD_KEY);
	}

	/**
	 * Send notification with uniform password.
	 */
	@SuppressWarnings("unchecked")
	protected void sendUniformPasswordNotification(IdmIdentityDto identityDto, IdmEntityStateDto uniformPasswordState) {
		Assert.notNull(identityDto, "Identity cannot be null!");
		Assert.notNull(uniformPasswordState, "Entity state cannot be null!");

		GuardedString password = this.getPassword(uniformPasswordState);
		ResultModel model = uniformPasswordState.getResult().getModel();
		Object successSystemsObj = model.getParameters().get(UniformPasswordManager.SUCCESS_SYSTEM_NAMES);
		Set<UUID> successSystems;
		if (successSystemsObj instanceof Set) {
			successSystems = (Set<UUID>) successSystemsObj;
		} else {
			successSystems = Sets.newHashSet();
		}

		// Send notification if at least one system success.
		if (!successSystems.isEmpty()) {
			notificationManager.send(
					CoreModuleDescriptor.TOPIC_UNIFORM_PASSWORD_SET,
					new IdmMessageDto.Builder()
							.setLevel(NotificationLevel.SUCCESS)
							.addParameter("successSystemNames", StringUtils.join(successSystems, ", "))
							.addParameter("name", identityService.getNiceLabel(identityDto))
							.addParameter("identity", identityDto)
							.addParameter("username", identityDto.getUsername())
							.addParameter("password", password)
							.build(),
					identityDto);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addSystemNameToEntityState(IdmEntityStateDto uniformPasswordState, String systemName) {
		ResultModel model = uniformPasswordState.getResult().getModel();
		// Add system name to entity state for uniform password (will be used in bulk notification).
		Object successSystemNamesObj = model.getParameters().get(UniformPasswordManager.SUCCESS_SYSTEM_NAMES);
		Set<String> successSystems;
		if (successSystemNamesObj instanceof Set) {
			successSystems = (Set<String>) successSystemNamesObj;
		} else {
			successSystems = Sets.newHashSet();
		}
		successSystems.add(systemName);

		// Create new parameters for entity state.
		HashMap<String, Object> newParameters = Maps.newHashMap(model.getParameters());
		newParameters.put(UniformPasswordManager.SUCCESS_SYSTEM_NAMES, successSystems);
		uniformPasswordState.getResult().setModel(new DefaultResultModel(CoreResultCode.IDENTITY_UNIFORM_PASSWORD, newParameters));
	}
}
