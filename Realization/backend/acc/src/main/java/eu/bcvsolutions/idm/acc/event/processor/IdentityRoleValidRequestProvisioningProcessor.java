package eu.bcvsolutions.idm.acc.event.processor;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleValidRequestDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleValidRequestEvent.IdentityRoleValidRequestEventType;

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
	private final IdmIdentityRoleService identityRoleService;
	private final IdmIdentityContractService identityContractService;
	
	@Autowired
	public IdentityRoleValidRequestProvisioningProcessor(
			ApplicationContext applicationContext,
			IdmIdentityRoleService identityRoleService,
			IdmIdentityContractService identityContractService) {
		super(IdentityRoleValidRequestEventType.IDENTITY_ROLE_VALID);
		//
		Assert.notNull(applicationContext);
		Assert.notNull(identityRoleService);
		Assert.notNull(identityContractService);
		//
		this.applicationContext = applicationContext;
		this.identityRoleService = identityRoleService;
		this.identityContractService = identityContractService;
	}
	
	@Override
	public EventResult<IdmIdentityRoleValidRequestDto> process(EntityEvent<IdmIdentityRoleValidRequestDto> event) {
		// IdentityRole and IdentityContract must exist - referential integrity.
		//
		// object identityRole is never null
		UUID identityRoleId = event.getContent().getIdentityRole();
		IdmIdentityRoleDto identityRole = identityRoleService.get(identityRoleId);
		//
		if (identityRole == null) {
			LOG.warn("[IdentityRoleValidRequestProvisioningProcessor] Identity role isn't exists for identity role valid request id: [{}]", event.getContent().getId());
			return new DefaultEventResult<>(event, this);
		}
		//
		IdmIdentityContractDto identityContract = identityContractService.get(identityRole.getIdentityContract());
		if (identityContract != null) {
			LOG.info("[IdentityRoleValidRequestProvisioningProcessor] Start with provisioning for identity role valid request id : [{}]", event.getContent().getId());
			//
			IdmIdentityDto identity = DtoUtils.getEmbedded(identityContract, IdmIdentityContract_.identity, IdmIdentityDto.class);
			boolean requiredProvisioning = getProvisioningService().accountManagement(identity);
			if (requiredProvisioning) {
				// do provisioning, for newly valid role
				getProvisioningService().doProvisioning(identity);
			}
			//
		} else {
			LOG.warn("[IdentityRoleValidRequestProvisioningProcessor] Identity contract isn't exists for identity role valid request id: [{}]", event.getContent().getId());
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
