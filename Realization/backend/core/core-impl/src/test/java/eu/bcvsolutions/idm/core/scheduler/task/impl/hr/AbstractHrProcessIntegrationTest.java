package eu.bcvsolutions.idm.core.scheduler.task.impl.hr;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.AbstractCoreWorkflowIntegrationTest;
import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.event.processor.contract.IdentityContractEnableProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.contract.IdentityContractEndProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.contract.IdentityContractExclusionProcessor;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmProcessedTaskItemService;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmScheduledTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulableTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.task.impl.AbstractWorkflowStatefulExecutor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.TestHelper;
import eu.bcvsolutions.idm.test.api.utils.SchedulerTestUtils;

/**
 * Common hr test super class
 * 
 * @author Jan Helbich
 * @author Radek Tomiška
 *
 * @param <E>
 */
public abstract class AbstractHrProcessIntegrationTest<E extends AbstractDto> extends AbstractCoreWorkflowIntegrationTest {

	@Autowired protected TestHelper helper;
	@Autowired protected IdmIdentityContractService identityContractService;
	@Autowired protected IdmIdentityService identityService;
	@Autowired protected IdmScheduledTaskService scheduledTaskService;
	@Autowired protected IdmLongRunningTaskService longRunningService;
	@Autowired protected IdmProcessedTaskItemService itemService;
	@Autowired protected IdmIdentityRoleService identityRoleService;
	
	protected AbstractWorkflowStatefulExecutor<E> executor;
	protected IdmScheduledTaskDto scheduledTask = null;
	protected IdmLongRunningTaskDto lrt = null;
	
	protected IdmIdentityDto createTestIdentity(String username) {
		return createTestIdentity(username, false);
	}
	
	protected void before() {
		super.loginAsAdmin();
		//
		helper.disable(IdentityContractEnableProcessor.class);
		helper.disable(IdentityContractEndProcessor.class);
		helper.disable(IdentityContractExclusionProcessor.class);
	}
	
	protected void after() {
		enableAllProcessors();
		//
		super.logout();
	}
	
	protected void enableAllProcessors() {
		helper.enable(IdentityContractEnableProcessor.class);
		helper.enable(IdentityContractEndProcessor.class);
		helper.enable(IdentityContractExclusionProcessor.class);
	}

	protected IdmIdentityDto createTestIdentity(String username, boolean disabled) {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername(username);
		identity.setPassword(new GuardedString("password"));
		identity.setFirstName("Test");
		identity.setLastName("User");
		identity.setEmail("test.user@example.tl");
		identity.setState(disabled ? IdentityState.DISABLED : null);
		identity = this.identityService.save(identity);
		// make sure default contract is not created
		identityContractService.findAllByIdentity(identity.getId())
			.forEach(c -> identityContractService.deleteInternal(c));
		return identity;
	}

	protected IdmIdentityContractDto getTestContract(IdmIdentityDto identity, boolean excluded) {
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setMain(false);
		contract.setState(excluded ? ContractState.EXCLUDED : null);
		return contract;
	}
	
	protected IdmIdentityContractDto createTestContract(IdmIdentityDto identity, boolean excluded) {
		return identityContractService.save(getTestContract(identity, excluded));
	}
	
	protected IdmScheduledTaskDto createIdmScheduledTask(String taskName) {
		return scheduledTaskService.save(SchedulerTestUtils.createIdmScheduledTask(taskName));
	}

	protected IdmLongRunningTaskDto createIdmLongRunningTask(IdmScheduledTaskDto taskDto,
			Class<? extends SchedulableTaskExecutor<Boolean>> clazz) {
		return longRunningService.save(SchedulerTestUtils.createIdmLongRunningTask(taskDto, clazz));
	}
	
	@SuppressWarnings("unchecked")
	protected void process(IdmLongRunningTaskDto lrt, E dto) {
		when(executor.getItemsToProcess(any(Pageable.class)))
			.thenReturn(new PageImpl<>(Lists.newArrayList(dto)))
			.thenReturn(new PageImpl<>(Lists.newArrayList()));
		//
		executor.process();
	}
	
}
