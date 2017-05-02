package eu.bcvsolutions.idm.acc.event.processor;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.dto.IdmIdentityRoleValidRequestDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleValidRequestEvent.IdentityRoleValidRequestEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;

/**
 * Processor for catch {@link IdentityRoleValidRequestEventType.IDENTITY_ROLE_VALID} - start account management for newly valid identityRoles
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Component
@Description("Start provisioning for role valid request result operation type [IDENTITY_ROLE_VALID].")
public class IdentityRoleValidRequestProvisioningProcessor extends AbstractEntityEventProcessor<IdmIdentityRoleValidRequestDto> {
	
	public static final String PROCESSOR_NAME = "identity-role-valid-request-provisioning-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityRoleValidRequestProvisioningProcessor.class);
	private ProvisioningService provisioningService;
	private final ApplicationContext applicationContext;
	private AccAccountManagementService accountManagementService;
	private final IdmIdentityRoleRepository identityRoleRepository;
	
	@Autowired
	public IdentityRoleValidRequestProvisioningProcessor(ApplicationContext applicationContext,
			IdmIdentityRoleRepository identityRoleRepository) {
		super(IdentityRoleValidRequestEventType.IDENTITY_ROLE_VALID);
		//
		Assert.notNull(applicationContext);
		Assert.notNull(identityRoleRepository);
		//
		this.applicationContext = applicationContext;
		this.identityRoleRepository =identityRoleRepository;
	}
	
	@Override
	public EventResult<IdmIdentityRoleValidRequestDto> process(EntityEvent<IdmIdentityRoleValidRequestDto> event) {
		// IdentityRole and IdentityContract must exist - referential integrity.
		//
		// object identityRole is never null
		UUID identityRoleId = event.getContent().getIdentityRole();
		IdmIdentityRole identityRole = identityRoleRepository.findOne(identityRoleId);
		//
		if (identityRole == null) {
			LOG.warn("[IdentityRoleValidRequestProvisioningProcessor] Identity role isn't exists for identity role valid request id: [{0}]", event.getContent().getId());
			return new DefaultEventResult<>(event, this);
		}
		//
		IdmIdentityContract identityContract = identityRole.getIdentityContract();
		if (identityContract != null) {
			LOG.info("[IdentityRoleValidRequestProvisioningProcessor] Start with provisioning for identity role valid request id : [{0}]", event.getContent().getId());
			//
			boolean requiredProvisioning = getAccountManagementService().resolveIdentityAccounts(identityContract.getIdentity());
			if (requiredProvisioning) {
				// do provisioning, for newly valid role
				getProvisioningService().doProvisioning(identityContract.getIdentity());
			}
			//
		} else {
			LOG.warn("[IdentityRoleValidRequestProvisioningProcessor] Identity contract isn't exists for identity role valid request id: [{0}]", event.getContent().getId());
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER - 10;
	}
	
	/**
	 * provisioningService has dependency everywhere - so we need lazy init ...
	 * 
	 * @return
	 */
	private ProvisioningService getProvisioningService() {
		if (provisioningService == null) {
			provisioningService = applicationContext.getBean(ProvisioningService.class);
		}
		return provisioningService;
	}
	
	private AccAccountManagementService getAccountManagementService() {
		if (accountManagementService == null) {
			accountManagementService = applicationContext.getBean(AccAccountManagementService.class);
		}
		return accountManagementService;
	}
}
