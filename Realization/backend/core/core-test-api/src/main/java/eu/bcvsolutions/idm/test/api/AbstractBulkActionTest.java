package eu.bcvsolutions.idm.test.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.bulk.action.BulkActionManager;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.scheduler.api.config.SchedulerConfiguration;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmProcessedTaskItemService;

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
	
	protected void setSynchronousLrt(boolean value) {
		configurationService.setBooleanValue(
				SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, value);
	}
	
	protected List<IdmProcessedTaskItemDto> getItemsForLrt(IdmLongRunningTaskDto longRunningTask) {
		return processedTaskItemService.findLogItems(longRunningTask, null).getContent();
	}
	
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
}
