package eu.bcvsolutions.idm.acc.event.processor;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakRecipientDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccRoleAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningBreakRecipientFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.service.api.AccRoleAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBreakRecipientService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleProcessor;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;

/**
 * Before role delete - deletes all role system mappings
 * 
 * @author Radek Tomiška
 *
 */
@Component("accRoleDeleteProcessor")
@Description("Ensures referential integrity. Cannot be disabled.")
public class RoleDeleteProcessor
		extends CoreEventProcessor<IdmRoleDto> 
		implements RoleProcessor {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RoleDeleteProcessor.class);
	
	public static final String PROCESSOR_NAME = "role-delete-processor";
	private final SysRoleSystemService roleSystemService;
	private final AccRoleAccountService roleAccountService;
	private final SysProvisioningBreakRecipientService provisioningBreakRecipientService;
	
	@Autowired
	public RoleDeleteProcessor(
			SysRoleSystemService roleSystemService,
			AccRoleAccountService roleAccountService,
			SysProvisioningBreakRecipientService provisioningBreakRecipientService) {
		super(RoleEventType.DELETE);
		//
		Assert.notNull(roleSystemService);
		Assert.notNull(roleAccountService);
		Assert.notNull(provisioningBreakRecipientService);
		//
		this.roleSystemService = roleSystemService;
		this.roleAccountService = roleAccountService;
		this.provisioningBreakRecipientService = provisioningBreakRecipientService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleDto> process(EntityEvent<IdmRoleDto> event) {
		// delete mapped roles
		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setRoleId(event.getContent().getId());
		roleSystemService.find(roleSystemFilter, null).forEach(roleSystem -> {
			roleSystemService.delete(roleSystem);
		});
		//
		// delete relations on account (includes delete of account	)
		AccRoleAccountFilter filter = new AccRoleAccountFilter();
		filter.setRoleId(event.getContent().getId());
		roleAccountService.find(filter, null).forEach(roleAccount -> {
			roleAccountService.delete(roleAccount);
		});
		//
		// remove all recipients from provisioning break
		deleteProvisioningRecipient(event.getContent().getId());
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		// right now before role delete
		return CoreEvent.DEFAULT_ORDER - 1;
	}
	
	@Override
	public boolean isDisableable() {
		return false;
	}
	
	/**
	 * Method remove all provisioning recipient for role id given in parameter
	 * 
	 * @param identityId
	 */
	private void deleteProvisioningRecipient(UUID roleId) {
		SysProvisioningBreakRecipientFilter filter = new SysProvisioningBreakRecipientFilter();
		filter.setRoleId(roleId);
		for (SysProvisioningBreakRecipientDto recipient : provisioningBreakRecipientService.find(filter, null).getContent()) {
			LOG.debug("Remove recipient from provisioning break [{}]", recipient.getId());
			provisioningBreakRecipientService.delete(recipient);
		}
	}
}