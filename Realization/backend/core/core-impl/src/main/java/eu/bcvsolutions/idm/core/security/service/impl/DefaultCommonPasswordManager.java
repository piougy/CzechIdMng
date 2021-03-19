package eu.bcvsolutions.idm.core.security.service.impl;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import eu.bcvsolutions.idm.core.security.api.service.CommonPasswordManager;
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
 * Manager for a common password of identity.
 *
 * @author Vít Švanda
 * @since 11.0.0
 */
@Service
public class DefaultCommonPasswordManager implements CommonPasswordManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultCommonPasswordManager.class);

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


	@Override
	public IdmEntityStateDto createEntityState(AbstractDto entityDto) {
		Assert.notNull(entityDto, "Entity cannot be null!");
		Assert.notNull(entityDto.getId(), "Entity ID cannot be null!");
		UUID transactionId = TransactionContextHolder.getContext().getTransactionId();
		
		IdmEntityStateDto entityState = this.getEntityState(entityDto.getId(), entityDto.getClass(), transactionId);
		if (entityState == null) {
			Map<String, Serializable> properties = new HashMap<>();
			properties.put("entity", entityDto.toString());
			entityState = entityStateManager.createState(
					entityDto,
					OperationState.BLOCKED,
					CoreResultCode.IDENTITY_COMMON_PASSWORD,
					properties
			);
			LOG.debug("Common password entity state for entity [{}] and transaction [{}] created.", entityDto.toString(), transactionId);
		}
		return entityState;
	}

	@Override
	public void endCommonPasswordProcess(UUID transactionId) {
		Assert.notNull(transactionId, "Transaction cannot be null!");

		IdmEntityStateFilter entityStateFilter = new IdmEntityStateFilter();
		entityStateFilter.setTransactionId(transactionId);
		entityStateFilter.setResultCode(CoreResultCode.IDENTITY_COMMON_PASSWORD.getCode());
		entityStateFilter.setStates(Lists.newArrayList(OperationState.BLOCKED));

		List<IdmEntityStateDto> entityStateDtos = entityStateManager.findStates(entityStateFilter, null).getContent();

		// Send notification with password to identities where was common password used.
		entityStateDtos.stream()
				.filter(entityState -> IdmIdentity.class.getCanonicalName().equals(entityState.getOwnerType()))
				.filter(this::commonPasswordUsed)
				.forEach(entityState -> {
					UUID identityId = entityState.getOwnerId();
					if (identityId != null) {
						IdmIdentityDto identityDto = identityService.get(identityId);
						if (identityDto != null) {
							// Send notification with common password.
							this.sendCommonPasswordNotification(identityDto, entityState);
						}
					}
				});

		// Remove all common password entity states for this transaction.
		entityStateDtos
				.forEach(entityState -> entityStateManager.deleteState(entityState));
	}

	@Override
	@Transactional
	public GuardedString generateCommonPassword(UUID entityIdentifier, Class<? extends AbstractDto> entityType, UUID transactionId) {
		Assert.notNull(transactionId, "Transaction cannot be null!");
		Assert.notNull(entityIdentifier, "Entity identifier cannot be null!");
		Assert.notNull(entityType, "Entity type cannot be null!");

		IdmEntityStateDto entityStateDto = this.getEntityState(entityIdentifier, entityType, transactionId);

		if (entityStateDto != null) {
			GuardedString password = getPassword(entityStateDto);
			if (password == null || password.getValue().length == 0) {
				password = new GuardedString(passwordPolicyService.generatePasswordByDefault());
				confidentialStorage.saveGuardedString(entityStateDto, COMMON_PASSWORD_KEY, password);
			}
			// The common password was used, we need to mark it. 
			Map<String, Object> parameters = entityStateDto.getResult().getModel().getParameters();
			HashMap<String, Object> newParameters = Maps.newHashMap(parameters);
			newParameters.put(PASSWORD_USED, true);
			entityStateDto.getResult().setModel(new DefaultResultModel(CoreResultCode.IDENTITY_COMMON_PASSWORD, newParameters));
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
		entityStateFilter.setResultCode(CoreResultCode.IDENTITY_COMMON_PASSWORD.getCode());
		entityStateFilter.setOwnerType(entityStateManager.getOwnerType(entityType));
		entityStateFilter.setOwnerId(entityIdentifier);
		entityStateFilter.setStates(Lists.newArrayList(OperationState.BLOCKED));

		return entityStateManager.findStates(entityStateFilter, null)
				.getContent()
				.stream()
				.findFirst()
				.orElse(null);
	}

	private boolean commonPasswordUsed(IdmEntityStateDto entityState) {
		ResultModel model = entityState.getResult().getModel();
		if (model != null) {
			Object passwordUsed = model.getParameters().get(PASSWORD_USED);
			return passwordUsed instanceof Boolean && (Boolean) passwordUsed;
		}
		return false;
	}

	/**
	 * Get common password from the confidential storage.
	 * 
	 * A potentially good place to override for use a static password.
	 */
	protected GuardedString getPassword(IdmEntityStateDto entityStateDto) {
		return confidentialStorage.getGuardedString(entityStateDto, COMMON_PASSWORD_KEY);
	}

	/**
	 * Send notification with common password.
	 */
	protected void sendCommonPasswordNotification(IdmIdentityDto identityDto, IdmEntityStateDto commonPasswordState) {
		Assert.notNull(identityDto, "Identity cannot be null!");
		Assert.notNull(commonPasswordState, "Entity state cannot be null!");

		GuardedString password = this.getPassword(commonPasswordState);
		ResultModel model = commonPasswordState.getResult().getModel();
		Object successSystemsObj = model.getParameters().get(CommonPasswordManager.SUCCESS_SYSTEM_NAMES);
		Set<UUID> successSystems = null;
		if (successSystemsObj instanceof Set) {
			successSystems = (Set<UUID>) successSystemsObj;
		} else {
			successSystems = Sets.newHashSet();
		}

		// Send notification if at least one system success.
		if (!successSystems.isEmpty()) {
			notificationManager.send(
					CoreModuleDescriptor.TOPIC_COMMON_PASSWORD_SET,
					new IdmMessageDto.Builder()
							.setLevel(NotificationLevel.SUCCESS)
							.addParameter("successSystemNames", StringUtils.join(successSystems, ", "))
							.addParameter("name", identityService.getNiceLabel(identityDto))
							.addParameter("username", identityDto.getUsername())
							.addParameter("password", password)
							.build(),
					identityDto);
		}
	}
}
