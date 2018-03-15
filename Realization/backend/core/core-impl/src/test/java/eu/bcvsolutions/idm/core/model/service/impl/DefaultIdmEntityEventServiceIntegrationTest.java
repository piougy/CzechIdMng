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
import eu.bcvsolutions.idm.core.api.exception.EventDeleteFailedHasChildrenException;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmEntityStateService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Entity events integration tests
 * - referential integrity
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmEntityEventServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private ApplicationContext context;
	@Autowired private IdmEntityStateService entityStateService;
	@Autowired private EntityEventManager entityEventManager;
	@Autowired private IdmIdentityService identityService;
	//
	private DefaultIdmEntityEventService entityEventService;

	@Before
	public void init() {
		entityEventService = context.getAutowireCapableBeanFactory().createBean(DefaultIdmEntityEventService.class);
	}
	
	@Test
	@Transactional
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
	@Transactional
	public void testReferentialIntegrityOwnerIsDeleted() {
		IdmIdentityDto identity = helper.createIdentity((GuardedString) null);
		IdmIdentityDto identityTwo = helper.createIdentity((GuardedString) null);
		//
		IdmEntityEventDto entityEvent = new IdmEntityEventDto();
		entityEvent.setOwnerType(entityEventManager.getOwnerType(identity.getClass()));
		entityEvent.setEventType("empty");
		entityEvent.setOwnerId(identity.getId());
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
		identityService.delete(identityTwo);
		//
		Assert.assertNotNull(identityService.get(identity));
		Assert.assertNotNull(entityEventService.get(entityEvent));
		Assert.assertNotNull(entityStateService.get(entityState));
		//
		identityService.delete(identity);
		//
		Assert.assertNull(identityService.get(identity));
		Assert.assertNull(entityEventService.get(entityEvent));
		Assert.assertNull(entityStateService.get(entityState));
	}
	
	@Test(expected = EventDeleteFailedHasChildrenException.class)
	@Transactional
	public void testReferentialIntegritParentIsDeleted() {
		IdmEntityEventDto parentEvent = new IdmEntityEventDto();
		parentEvent.setOwnerType("empty");
		parentEvent.setEventType("empty");
		parentEvent.setOwnerId(UUID.randomUUID());
		parentEvent.setInstanceId("empty");
		parentEvent.setResult(new OperationResultDto(OperationState.BLOCKED));
		parentEvent.setPriority(PriorityType.NORMAL);
		parentEvent = entityEventService.save(parentEvent);
		//
		IdmEntityEventDto entityEvent = new IdmEntityEventDto();
		entityEvent.setOwnerType("empty");
		entityEvent.setEventType("empty");
		entityEvent.setOwnerId(UUID.randomUUID());
		entityEvent.setInstanceId("empty");
		entityEvent.setResult(new OperationResultDto(OperationState.BLOCKED));
		entityEvent.setPriority(PriorityType.NORMAL);
		entityEvent.setParent(parentEvent.getId());
		entityEvent = entityEventService.save(entityEvent);
		//
		Assert.assertNotNull(entityEvent.getId());
		//
		entityEventService.delete(parentEvent);
	}
	
	@Transactional
	public void testReferentialIntegrityLastChildIsDeleted() {
		IdmEntityEventDto parentEvent = new IdmEntityEventDto();
		parentEvent.setOwnerType("empty");
		parentEvent.setEventType("empty");
		parentEvent.setOwnerId(UUID.randomUUID());
		parentEvent.setInstanceId("empty");
		parentEvent.setResult(new OperationResultDto(OperationState.BLOCKED));
		parentEvent.setPriority(PriorityType.NORMAL);
		parentEvent = entityEventService.save(parentEvent);
		//
		IdmEntityEventDto entityEventOne = new IdmEntityEventDto();
		entityEventOne.setOwnerType("empty");
		entityEventOne.setEventType("empty");
		entityEventOne.setOwnerId(UUID.randomUUID());
		entityEventOne.setInstanceId("empty");
		entityEventOne.setResult(new OperationResultDto(OperationState.BLOCKED));
		entityEventOne.setPriority(PriorityType.NORMAL);
		entityEventOne.setParent(parentEvent.getId());
		entityEventOne = entityEventService.save(entityEventOne);
		//
		IdmEntityEventDto entityEventTwo = new IdmEntityEventDto();
		entityEventTwo.setOwnerType("empty");
		entityEventTwo.setEventType("empty");
		entityEventTwo.setOwnerId(UUID.randomUUID());
		entityEventTwo.setInstanceId("empty");
		entityEventTwo.setResult(new OperationResultDto(OperationState.BLOCKED));
		entityEventTwo.setPriority(PriorityType.NORMAL);
		entityEventTwo.setParent(parentEvent.getId());
		entityEventTwo = entityEventService.save(entityEventTwo);
		//
		Assert.assertNotNull(parentEvent.getId());
		Assert.assertNotNull(entityEventOne.getId());
		Assert.assertNotNull(entityEventTwo.getId());
		//
		entityEventService.delete(entityEventTwo);
		//
		Assert.assertNotNull(entityEventService.get(parentEvent.getId()));
		Assert.assertNotNull(entityEventService.get(entityEventOne.getId()));
		Assert.assertNull(entityEventService.get(entityEventTwo.getId()));
		//
		entityEventService.delete(entityEventOne);
		//
		Assert.assertNull(entityEventService.get(parentEvent.getId()));
		Assert.assertNull(entityEventService.get(entityEventOne.getId()));
		Assert.assertNull(entityEventService.get(entityEventTwo.getId()));
	}
}
