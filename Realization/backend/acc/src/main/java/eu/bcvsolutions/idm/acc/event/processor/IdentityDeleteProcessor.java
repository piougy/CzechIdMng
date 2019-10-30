package eu.bcvsolutions.idm.acc.event.processor;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakRecipientDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncContractConfigDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningBreakRecipientFilter;
import eu.bcvsolutions.idm.acc.repository.SysSyncConfigRepository;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBreakRecipientService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;

/**
 * Before identity delete - deletes all identity accounts.
 * 
 * @author Radek Tomiška
 *
 */
@Component(IdentityDeleteProcessor.PROCESSOR_NAME)
@Description("Ensures referential integrity. Cannot be disabled. Removes identity accounts.")
public class IdentityDeleteProcessor
		extends CoreEventProcessor<IdmIdentityDto> 
		implements IdentityProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityDeleteProcessor.class);
	
	public static final String PROCESSOR_NAME = "acc-identity-delete-processor";
	//
	@Autowired private AccIdentityAccountService identityAccountService;
	@Autowired private SysProvisioningBreakRecipientService provisioningBreakRecipientService;
	@Autowired private SysSyncConfigRepository syncConfigRepository;
	@Autowired private SysSyncConfigService syncConfigService;
	
	public IdentityDeleteProcessor() {
		super(IdentityEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		IdmIdentityDto identity = event.getContent();
		Assert.notNull(identity, "Identity is required.");
		Assert.notNull(identity.getId(), "Identity identifier is required.");
		//
		// Clears default leader.
		syncConfigRepository
			.findByDefaultLeader(identity.getId())
			.forEach(config -> {
				SysSyncContractConfigDto configDto = (SysSyncContractConfigDto) syncConfigService.get(config.getId());
				configDto.setDefaultLeader(null);
				syncConfigService.save(configDto);
			});
		//
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		identityAccountService.find(filter, null).forEach(identityAccount -> {
			identityAccountService.forceDelete(identityAccount);
		});
		//
		// remove all recipients from provisioning break
		deleteProvisioningRecipients(identity.getId());
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// right now before identity delete
		return CoreEvent.DEFAULT_ORDER - 1;
	}
	
	@Override
	public boolean isDisableable() {
		return false;
	}
	
	/**
	 * Method remove all provisioning recipient for identity id given in parameter
	 * 
	 * @param identityId
	 */
	private void deleteProvisioningRecipients(UUID identityId) {
		SysProvisioningBreakRecipientFilter filter = new SysProvisioningBreakRecipientFilter();
		filter.setIdentityId(identityId);
		for (SysProvisioningBreakRecipientDto recipient : provisioningBreakRecipientService.find(filter, null).getContent()) {
			LOG.debug("Remove recipient from provisioning break [{}]", recipient.getId());
			provisioningBreakRecipientService.delete(recipient);
		}
	}
}