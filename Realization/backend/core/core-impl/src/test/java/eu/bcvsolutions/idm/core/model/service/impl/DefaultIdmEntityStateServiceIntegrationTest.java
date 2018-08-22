package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Entity states integration tests
 * - referential integrity
 * - 
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class DefaultIdmEntityStateServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired private ApplicationContext context;
	@Autowired private IdmEntityEventService entityEventService;
	@Autowired private LookupService lookupService;
	//
	private DefaultIdmEntityStateService entityStateService;

	@Before
	public void init() {
		entityStateService = context.getAutowireCapableBeanFactory().createBean(DefaultIdmEntityStateService.class);
	}
	
	@Test
	public void testReferentialIntegrity() {
		IdmEntityEventDto entityEvent = new IdmEntityEventDto();
		entityEvent.setOwnerType("empty");
		entityEvent.setEventType("empty");
		entityEvent.setOwnerId(UUID.randomUUID());
		entityEvent.setInstanceId("empty");
		entityEvent.setResult(new OperationResultDto(OperationState.BLOCKED));
		entityEvent.setPriority(PriorityType.NORMAL);
		entityEvent = entityEventService.save(entityEvent);
		//
		Assert.assertNotNull(entityEvent.getId());
		//
		IdmEntityStateDto entityState = new IdmEntityStateDto(entityEvent);
		entityState.setResult(new OperationResultDto(OperationState.BLOCKED));
		entityState = entityStateService.save(entityState);
		//
		Assert.assertNotNull(entityState.getId());
		//
		entityEventService.delete(entityEvent);
		//
		Assert.assertNull(entityEventService.get(entityEvent));
		Assert.assertNull(entityStateService.get(entityState));
	}
	
	@Test
	@Ignore
	public void testReferentialIntegrityOwnerIsDeleted() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);
		//
		IdmEntityStateDto entityState = new IdmEntityStateDto();
		entityState.setOwnerId(lookupService.getOwnerId(identity));
		entityState.setOwnerType(lookupService.getOwnerType(identity));
		entityState.setResult(new OperationResultDto(OperationState.BLOCKED));
		entityState = entityStateService.save(entityState);
		//
		IdmEntityStateDto entityStateTwo = new IdmEntityStateDto();
		entityStateTwo.setOwnerId(lookupService.getOwnerId(identityTwo));
		entityStateTwo.setOwnerType(lookupService.getOwnerType(identityTwo));
		entityStateTwo.setResult(new OperationResultDto(OperationState.BLOCKED));
		entityStateTwo = entityStateService.save(entityStateTwo);
		//
		Assert.assertNotNull(entityState.getId());
		Assert.assertNotNull(entityStateTwo.getId());
		//
		getHelper().getService(IdmIdentityService.class).delete(identityTwo);
		//
		Assert.assertNull(entityStateService.get(entityStateTwo));
		Assert.assertNotNull(entityStateService.get(entityState));
	}
}
