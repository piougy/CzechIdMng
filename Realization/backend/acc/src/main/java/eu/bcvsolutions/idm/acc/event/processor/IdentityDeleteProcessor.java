package eu.bcvsolutions.idm.acc.event.processor;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakRecipientDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningBreakRecipientFilter;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBreakRecipientService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;

/**
 * Before identity delete - deletes all identity accounts
 * 
 * @author Radek Tomiška
 *
 */
@Component("accIdentityDeleteProcessor")
@Description("Ensures referential integrity. Cannot be disabled.")
public class IdentityDeleteProcessor extends AbstractEntityEventProcessor<IdmIdentityDto> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityDeleteProcessor.class);
	
	public static final String PROCESSOR_NAME = "identity-delete-processor";
	private final AccIdentityAccountService identityAccountService;
	private final SysProvisioningBreakRecipientService provisioningBreakRecipientService;
	
	@Autowired
	public IdentityDeleteProcessor(
			AccIdentityAccountService identityAccountService,
			SysProvisioningBreakRecipientService provisioningBreakRecipientService) {
		super(IdentityEventType.DELETE);
		//
		Assert.notNull(identityAccountService);
		Assert.notNull(provisioningBreakRecipientService);
		//
		this.identityAccountService = identityAccountService;
		this.provisioningBreakRecipientService = provisioningBreakRecipientService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setIdentityId(event.getContent().getId());
		identityAccountService.find(filter, null).forEach(identityAccount -> {
			identityAccountService.forceDelete(identityAccount);
		});
		//
		// remove all recipients from provisioning break
		deleteProvisioningRecipient(event.getContent().getId());
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
	private void deleteProvisioningRecipient(UUID identityId) {
		SysProvisioningBreakRecipientFilter filter = new SysProvisioningBreakRecipientFilter();
		filter.setIdentityId(identityId);
		for (SysProvisioningBreakRecipientDto recipient : provisioningBreakRecipientService.find(filter, null).getContent()) {
			LOG.debug("Remove recipient from provisioning break [{}]", recipient.getId());
			provisioningBreakRecipientService.delete(recipient);
		}
	}
}