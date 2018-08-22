package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Entity state integration tests
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class DefaultEntityStateManagerIntergationTest extends AbstractIntegrationTest {

	@Autowired private ApplicationContext context;
	//
	private DefaultEntityStateManager manager; 
	
	@Before
	public void init() {
		manager = context.getAutowireCapableBeanFactory().createBean(DefaultEntityStateManager.class);
	}
	
	@Test
	public void getOwnerType() {
		IdmIdentityDto owner = getHelper().createIdentity((GuardedString) null);
		//
		Assert.assertEquals(IdmIdentity.class.getCanonicalName(), manager.getOwnerType(IdmIdentity.class));
		Assert.assertEquals(IdmIdentity.class.getCanonicalName(), manager.getOwnerType(IdmIdentityDto.class));
		//
		Assert.assertEquals(IdmIdentity.class.getCanonicalName(), manager.getOwnerType(owner));
	}
	
	@Test
	public void testSaveState() {
		IdmIdentityDto owner = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto ownerTwo = getHelper().createIdentity((GuardedString) null);
		//
		IdmEntityStateDto state = new IdmEntityStateDto();
		state.setResult(
				new OperationResultDto
					.Builder(OperationState.RUNNING)
					.setModel(new DefaultResultModel(CoreResultCode.DELETED))
					.build());
		state = manager.saveState(owner, state);
		
		IdmEntityStateDto stateOther = new IdmEntityStateDto();
		stateOther.setResult(
				new OperationResultDto
					.Builder(OperationState.RUNNING)
					.setModel(new DefaultResultModel(CoreResultCode.DELETED))
					.build());
		manager.saveState(ownerTwo, stateOther);
		
		List<IdmEntityStateDto> states = manager.findStates(owner, null).getContent();
		Assert.assertEquals(1, states.size());
		IdmEntityStateDto persistedState = states.get(0);
		//
		Assert.assertEquals(owner.getId(), persistedState.getOwnerId());
		Assert.assertEquals(manager.getOwnerType(owner), persistedState.getOwnerType());
		Assert.assertEquals(OperationState.RUNNING, persistedState.getResult().getState());
		//
		manager.deleteState(state);
		//
		Assert.assertTrue(manager.findStates(owner, new PageRequest(0, 1)).getTotalElements() == 0);
	}
}
