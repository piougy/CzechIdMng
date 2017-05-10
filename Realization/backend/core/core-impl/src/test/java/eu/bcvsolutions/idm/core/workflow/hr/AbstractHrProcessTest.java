package eu.bcvsolutions.idm.core.workflow.hr;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.AbstractCoreWorkflowIntegrationTest;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulableTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmLongRunningTaskDtoService;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmProcessedTaskItemDtoService;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmScheduledTaskDtoService;
import eu.bcvsolutions.idm.core.scheduler.task.impl.hr.AbstractWorkflowStatefulExecutor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.utils.SchedulerTestUtils;

public abstract class AbstractHrProcessTest<E extends AbstractDto> extends AbstractCoreWorkflowIntegrationTest {

	@Autowired protected IdmIdentityContractService identityContractService;
	@Autowired protected IdmIdentityService identityService;
	@Autowired protected IdmScheduledTaskDtoService scheduledTaskService;
	@Autowired protected IdmLongRunningTaskDtoService longRunningService;
	@Autowired protected IdmProcessedTaskItemDtoService itemService;
	@Autowired protected IdmIdentityRoleService identityRoleService;
	
	protected AbstractWorkflowStatefulExecutor<E> executor;
	protected IdmScheduledTaskDto scheduledTask = null;
	protected IdmLongRunningTaskDto lrt = null;
	
	protected IdmIdentityDto createTestIdentity(String username) {
		return createTestIdentity(username, false);
	}

	protected IdmIdentityDto createTestIdentity(String username, boolean disabled) {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername(username);
		identity.setPassword(new GuardedString("password"));
		identity.setFirstName("Test");
		identity.setLastName("User");
		identity.setEmail("test.user@example.tl");
		identity.setDisabled(disabled);
		identity = this.identityService.save(identity);
		return identity;
	}

	protected IdmIdentityContractDto getTestContract(IdmIdentityDto identity, boolean disabled) {
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setMain(false);
		contract.setDisabled(disabled);
		return contract;
	}
	
	protected IdmIdentityContractDto createTestContract(IdmIdentityDto identity, boolean disabled) {
		return identityContractService.save(getTestContract(identity, disabled));
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
