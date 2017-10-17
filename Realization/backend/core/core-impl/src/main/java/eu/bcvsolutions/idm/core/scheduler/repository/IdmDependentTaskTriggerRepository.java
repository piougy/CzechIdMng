package eu.bcvsolutions.idm.core.scheduler.repository;

import java.util.List;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmDependentTaskTrigger;

/**
 * Dependent task trigger repository
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmDependentTaskTriggerRepository extends AbstractEntityRepository<IdmDependentTaskTrigger> {

	/**
	 * Find all dependent tasks by initiator
	 * 
	 * @param initiatorTaskId
	 * @return
	 */
	List<IdmDependentTaskTrigger> findByInitiatorTaskId(String initiatorTaskId);
	
	/**
	 * Find all dependent tasks by dependent task
	 * 
	 * @param dependentTaskId
	 * @return
	 */
	List<IdmDependentTaskTrigger> findByDependentTaskId(String dependentTaskId);
}
