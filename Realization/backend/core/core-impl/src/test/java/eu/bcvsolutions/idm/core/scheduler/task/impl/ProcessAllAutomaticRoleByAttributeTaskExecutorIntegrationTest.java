package eu.bcvsolutions.idm.core.scheduler.task.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.service.AutomaticRoleManager;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Integration test for {@link ProcessAllAutomaticRoleByAttributeTaskExecutor}.
 *
 * @author Ondrej Kopr
 *
 */
public class ProcessAllAutomaticRoleByAttributeTaskExecutorIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private EntityEventManager entityEventManager;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private LongRunningTaskManager longRunningTaskManager;

	@Test
	public void testAssignRoles() {
		ProcessAllAutomaticRoleByAttributeTaskExecutor autoRolecalculation = new ProcessAllAutomaticRoleByAttributeTaskExecutor();
		longRunningTaskManager.executeSync(autoRolecalculation);
		//
		String automaticRoleValue = this.getHelper().createName();
		IdmAutomaticRoleAttributeDto automaticRole = this.getHelper().createAutomaticRole(this.getHelper().createRole().getId());
		this.getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS, AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.description.getName(), null, automaticRoleValue);

		List<IdmIdentityDto> identities = new ArrayList<IdmIdentityDto>();
		for (int index = 0; index < 154; index++) {
			IdmIdentityDto identity = this.getHelper().createIdentity((GuardedString)null);
			identities.add(identity);

			identity.setDescription(automaticRoleValue);
			identity = saveIdentityWithouRecalculation(identity);

			checkIdentityRoles(identity, 0, null);
		}

		autoRolecalculation = new ProcessAllAutomaticRoleByAttributeTaskExecutor();
		longRunningTaskManager.executeSync(autoRolecalculation);

		for (IdmIdentityDto identity : identities) {
			checkIdentityRoles(identity, 1, automaticRole.getId());
		}
	}

	@Test
	public void testRemoveRoles() {
		ProcessAllAutomaticRoleByAttributeTaskExecutor autoRolecalculation = new ProcessAllAutomaticRoleByAttributeTaskExecutor();
		longRunningTaskManager.executeSync(autoRolecalculation);
		//
		String automaticRoleValue = this.getHelper().createName();
		IdmAutomaticRoleAttributeDto automaticRole = this.getHelper().createAutomaticRole(this.getHelper().createRole().getId());
		this.getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS, AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.description.getName(), null, automaticRoleValue);

		List<IdmIdentityDto> identities = new ArrayList<IdmIdentityDto>();
		for (int index = 0; index < 134; index++) {
			IdmIdentityDto identity = this.getHelper().createIdentity((GuardedString)null);
			identities.add(identity);

			identity.setDescription(automaticRoleValue);
			identityService.save(identity);
			identity = identityService.save(identity);

			checkIdentityRoles(identity, 1, automaticRole.getId());
		}

		for (IdmIdentityDto identity : identities) {
			identity.setDescription(null);
			identity = saveIdentityWithouRecalculation(identity);
			checkIdentityRoles(identity, 1, automaticRole.getId());
		}

		autoRolecalculation = new ProcessAllAutomaticRoleByAttributeTaskExecutor();
		longRunningTaskManager.executeSync(autoRolecalculation);

		for (IdmIdentityDto identity : identities) {
			checkIdentityRoles(identity, 0, null);
		}
	}

	/**
	 * Check count of identity roles and if roles are equal 1 also check automatic role id
	 *
	 * @param identity
	 * @param count
	 */
	private void checkIdentityRoles(IdmIdentityDto identity, int count, UUID automaticRoleId) {
		List<IdmIdentityRoleDto> findAllByIdentity = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(count, findAllByIdentity.size());
		if (count == 1 && automaticRoleId != null) {
			IdmIdentityRoleDto identityRoleDto = findAllByIdentity.get(0);
			assertEquals(automaticRoleId, identityRoleDto.getAutomaticRole());
		}
	}
	/**
	 * Save identity without automatic role recalculation
	 *
	 * @param identity
	 * @return
	 */
	private IdmIdentityDto saveIdentityWithouRecalculation(IdmIdentityDto identity) {
		IdentityEvent event = new IdentityEvent(IdentityEventType.UPDATE, identity);
		event.getProperties().put(AutomaticRoleManager.SKIP_RECALCULATION, Boolean.TRUE);
		return entityEventManager.process(event).getContent();
	}
			
}
