package eu.bcvsolutions.idm.test.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.bulk.action.BulkActionManager;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.scheduler.api.config.SchedulerConfiguration;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmProcessedTaskItemService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Abstract class for testing bulk actions
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class AbstractBulkActionTest extends AbstractIntegrationTest {

	@Autowired
	protected BulkActionManager bulkActionManager;
	@Autowired
	protected IdmLongRunningTaskService longRunningTaskService;
	@Autowired
	protected IdmConfigurationService configurationService;
	@Autowired
	protected IdmProcessedTaskItemService processedTaskItemService;
	@Autowired
	protected ObjectMapper objectMapper;
	
	/**
	 * Return processed items for long running task
	 *
	 * @param longRunningTask
	 * @return
	 */
	protected List<IdmProcessedTaskItemDto> getItemsForLrt(IdmLongRunningTaskDto longRunningTask) {
		return processedTaskItemService.findLogItems(longRunningTask, null).getContent();
	}
	
	/**
	 * Find bulk action
	 *
	 * @param entity
	 * @param name
	 * @return
	 */
	protected IdmBulkActionDto findBulkAction(Class<? extends AbstractEntity> entity, String name) {
		List<IdmBulkActionDto> actions = bulkActionManager.getAvailableActions(entity);
		assertFalse(actions.isEmpty());
		
		for (IdmBulkActionDto action : actions) {
			if (action.getName().equals(name)) {
				return action;
			}
		}
		fail("For entity class: " + entity.getSimpleName() + " was not found bulk action: " + name);
		return null;
	}

	/**
	 * Create user with given base permission
	 *
	 * @param permissions
	 * @return
	 */
	protected IdmIdentityDto createUserWithAuthorities(BasePermission ...permissions) {
		IdmIdentityDto createIdentity = getHelper().createIdentity();
		
		IdmRoleDto createRole = getHelper().createRole();
		
		getHelper().createBasePolicy(createRole.getId(), permissions);
		getHelper().createIdentityRole(createIdentity, createRole);
		
		return createIdentity;
	}
	
	/**
	 * Enable only synchronously long runnign task
	 */
	protected void enableSynchronousLrt() {
		configurationService.setBooleanValue(
				SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, false);
	}
	
	/**
	 * Set default value for asznc/sznc behavior on long running task 
	 */
	protected void setDefaultSynchronousLrt() {
		configurationService.setBooleanValue(
				SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, SchedulerConfiguration.DEFAULT_TASK_ASYNCHRONOUS_ENABLED);
	}
	
	/**
	 * Create list of identities
	 *
	 * @param count
	 * @return
	 */
	protected List<IdmIdentityDto> createIdentities(int count) {
		List<IdmIdentityDto> identites = new ArrayList<>();
		//
		for (int index = 0; index < count; index++) {
			identites.add(getHelper().createIdentity());
		}
		//
		return identites;
	}

	/**
	 * Return list of ids from list of identities
	 *
	 * @param identites
	 * @return
	 */
	protected Set<UUID> getIdFromList(List<IdmIdentityDto> identites) {
		return identites.stream().map(IdmIdentityDto::getId).collect(Collectors.toSet());
	}
	
	/**
	 * Check result of bulk action
	 *
	 * @param processAction
	 * @param successCount
	 * @param failedCount
	 * @param warningCount
	 * @return
	 */
	protected IdmLongRunningTaskDto checkResultLrt(IdmBulkActionDto processAction, Long successCount, Long failedCount, Long warningCount) {
		assertNotNull(processAction.getLongRunningTaskId());
		IdmLongRunningTaskDto taskDto = longRunningTaskService.get(processAction.getLongRunningTaskId());
		assertNotNull(taskDto);
		
		if (successCount != null) {
			assertEquals(successCount, taskDto.getSuccessItemCount());
		}
		
		if (failedCount != null) {
			assertEquals(failedCount, taskDto.getFailedItemCount());
		}

		if (warningCount != null) {
			assertEquals(warningCount, taskDto.getWarningItemCount());
		}

		return taskDto;
	}
	
	/**
	 * Transform object to map
	 *
	 * @param object
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, Object> toMap(Object object) {
		Map<String, Object> convertValue = objectMapper.convertValue(object, Map.class);
		//
		Map<String, Object> result = new HashMap<>();
		for (Entry<String, Object> entry : convertValue.entrySet()) {
			if (entry.getValue() != null) {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}
}
