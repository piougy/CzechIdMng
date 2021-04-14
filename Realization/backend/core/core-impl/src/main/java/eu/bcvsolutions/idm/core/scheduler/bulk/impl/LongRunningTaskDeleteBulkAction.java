package eu.bcvsolutions.idm.core.scheduler.bulk.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;

/**
 * Delete long running task.
 * 
 * @author Radek Tomiška
 * @since 9.7.12
 */
@Component(LongRunningTaskDeleteBulkAction.NAME)
@Description("Delete long running task.")
public class LongRunningTaskDeleteBulkAction extends AbstractRemoveBulkAction<IdmLongRunningTaskDto, IdmLongRunningTaskFilter> {

	public static final String NAME = "core-long-running-task-delete-bulk-action";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LongRunningTaskCancelBulkAction.class);
	//
	@Autowired private IdmLongRunningTaskService service;
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	protected List<UUID> getEntities(IdmBulkActionDto action, StringBuilder description) {
		List<UUID> entities = super.getEntities(action, description);
		if (entities.remove(getLongRunningTaskId())) {
			LOG.debug("Delete long running task cannot delete itself.");
		}
		//
		return entities;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.SCHEDULER_DELETE);
	}

	@Override
	public ReadWriteDtoService<IdmLongRunningTaskDto, IdmLongRunningTaskFilter> getService() {
		return service;
	}
}
