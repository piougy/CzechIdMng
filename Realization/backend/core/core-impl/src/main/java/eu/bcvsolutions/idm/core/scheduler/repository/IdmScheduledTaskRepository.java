package eu.bcvsolutions.idm.core.scheduler.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.scheduler.dto.filter.IdmScheduledTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmScheduledTask;

public interface IdmScheduledTaskRepository
	extends AbstractEntityRepository<IdmScheduledTask, IdmScheduledTaskFilter> {

	@Override
	@Deprecated
	default Page<IdmScheduledTask> find(IdmScheduledTaskFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Use IdmScheduledTaskService (uses criteria api)");
	}

}
