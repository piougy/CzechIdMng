package eu.bcvsolutions.idm.core.model.event.processor;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.filter.ConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;

/**
 * Deletes role - ensures referential integrity.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Deletes role from repository.")
public class RoleDeleteProcessor extends CoreEventProcessor<IdmRole> {
	
	public static final String PROCESSOR_NAME = "role-delete-processor";
	private final IdmRoleRepository repository;
	private final IdmIdentityRoleRepository identityRoleRepository;
	private final IdmConceptRoleRequestService conceptRoleRequestService;
	private final IdmRoleRequestService roleRequestService;
	private final IdmRoleTreeNodeService roleTreeNodeService;
	
	@Autowired
	public RoleDeleteProcessor(
			IdmRoleRepository repository,
			IdmIdentityRoleRepository identityRoleRepository,
			IdmConceptRoleRequestService conceptRoleRequestService,
			IdmRoleRequestService roleRequestService,
			IdmRoleTreeNodeService roleTreeNodeService) {
		super(RoleEventType.DELETE);
		//
		Assert.notNull(repository);
		Assert.notNull(identityRoleRepository);
		Assert.notNull(conceptRoleRequestService);
		Assert.notNull(roleRequestService);
		Assert.notNull(roleTreeNodeService);
		//
		this.repository = repository;
		this.identityRoleRepository = identityRoleRepository;
		this.conceptRoleRequestService = conceptRoleRequestService;
		this.roleRequestService = roleRequestService;
		this.roleTreeNodeService = roleTreeNodeService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRole> process(EntityEvent<IdmRole> event) {
		IdmRole role = event.getContent();
		
		// role assigned to identity could not be deleted
		if(identityRoleRepository.countByRole(role) > 0) {
			throw new ResultCodeException(CoreResultCode.ROLE_DELETE_FAILED_IDENTITY_ASSIGNED, ImmutableMap.of("role", role.getName()));
		}
		// remove related automatic roles
		RoleTreeNodeFilter filter = new RoleTreeNodeFilter();
		filter.setRoleId(role.getId());
		roleTreeNodeService.findDto(filter, null).forEach(roleTreeNode -> {
			roleTreeNodeService.delete(roleTreeNode);
		});
		// Find all concepts and remove relation on role
		ConceptRoleRequestFilter conceptRequestFilter = new ConceptRoleRequestFilter();
		conceptRequestFilter.setRoleId(role.getId());
		conceptRoleRequestService.findDto(conceptRequestFilter, null).getContent().forEach(concept -> {
			IdmRoleRequestDto request = roleRequestService.getDto(concept.getRoleRequest());
			String message = null;
			if (concept.getState().isTerminatedState()) {
				message = MessageFormat.format(
						"Role [{0}] (requested in concept [{1}]) was deleted (not from this role request)!",
						role.getName(), concept.getId());
			} else {
				message = MessageFormat.format(
						"Request change in concept [{0}], was not executed, because requested role [{1}] was deleted (not from this role request)!",
						concept.getId(), role.getName());
				concept.setState(RoleRequestState.CANCELED);
			}
			roleRequestService.addToLog(request, message);
			conceptRoleRequestService.addToLog(concept, message);
			concept.setRole(null);

			roleRequestService.save(request);
			conceptRoleRequestService.save(concept);
		});
		
		// guarantees and compositions are deleted by hibernate mapping
		repository.delete(role);
		//
		return new DefaultEventResult<>(event, this);
	}
}