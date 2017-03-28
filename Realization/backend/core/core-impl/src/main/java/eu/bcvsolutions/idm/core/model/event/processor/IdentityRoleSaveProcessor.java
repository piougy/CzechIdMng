package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleValidRequestService;

/**
 * Save identity role
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Persists identity role.")
public class IdentityRoleSaveProcessor extends CoreEventProcessor<IdmIdentityRole> {

	public static final String PROCESSOR_NAME = "identity-role-save-processor";
	private final IdmIdentityRoleRepository repository;
	private final IdmIdentityRoleService identityRoleService;
	private final IdmIdentityRoleValidRequestService validRequestService;
	
	@Autowired
	public IdentityRoleSaveProcessor(
			IdmIdentityRoleRepository repository,
			IdmIdentityRoleService identityRoleService,
			IdmIdentityRoleValidRequestService validRequestService) {
		super(IdentityRoleEventType.CREATE, IdentityRoleEventType.UPDATE);
		//
		Assert.notNull(repository);
		Assert.notNull(identityRoleService);
		Assert.notNull(validRequestService);
		//
		this.repository = repository;
		this.identityRoleService = identityRoleService;
		this.validRequestService = validRequestService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityRole> process(EntityEvent<IdmIdentityRole> event) {
		IdmIdentityRole identityRole = event.getContent();
		repository.save(identityRole);
		//
		// TODO: move in another processor?
		// if identityRole isn't valid save request into validRequests
		if (!identityRoleService.isIdentityRoleValidFromNow(identityRole)) {
			// create new IdmIdentityRoleValidRequest
			validRequestService.createByIdentityRole(identityRole);
		}
		//
		return new DefaultEventResult<>(event, this);
	}
}