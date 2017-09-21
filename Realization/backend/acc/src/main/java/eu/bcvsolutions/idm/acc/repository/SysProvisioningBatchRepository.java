package eu.bcvsolutions.idm.acc.repository;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningBatch;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Provisioning operation batches. Any operation has request and batch.
 * 
 * @author Radek Tomiška
 *
 */
public interface SysProvisioningBatchRepository extends AbstractEntityRepository<SysProvisioningBatch> {
	
	/**
	 * Finds batch for given operation.
	 * 
	 * @param operation
	 * @return
	 */
	@Query(value = "select distinct(r.batch) from SysProvisioningRequest r join r.operation o"
			+ " where"
			+ " o.systemEntity.system = ?#{[0].systemEntity.system}"
			+ " and"
			+ " (o.entityIdentifier = ?#{[0].entityIdentifier} or ?#{[0].entityIdentifier} is null)"
			+ " and"
			+ " o.systemEntity.uid = ?#{[0].systemEntity.uid}")
	SysProvisioningBatch findBatch(SysProvisioningOperation operation);
	
	/**
	 * Returns batches by their request's state
	 * 
	 * @param state
	 * @param pageable
	 * @return
	 */
	@Query(value = "select e from #{#entityName} e where exists (select r.id from SysProvisioningRequest r"
			+ " where"
			+ " r.batch = e"
			+ " and"
			+ " r.result.state = :state)")
	Page<SysProvisioningBatch> findByOperationState(@Param("state") OperationState state, Pageable pageable);
	
	/**
	 * Returns batches by their system is virtual and request's state
	 * 
	 * @param state
	 * @param pageable
	 * @return
	 */
	@Query(value = "select e from #{#entityName} e where exists (select r.id from SysProvisioningRequest r"
			+ " where"
			+ " r.batch = e"
			+ " and"
			+ " r.operation.systemEntity.system.virtual = :virtualSystem"
			+ " and"
			+ " r.result.state = :state)")
	Page<SysProvisioningBatch>  findByVirtualSystemAndOperationState(@Param("virtualSystem") Boolean virtualSystem, @Param("state") OperationState state, Pageable pageable);
	
	/**
	 * Returns unprocessed planned batches
	 * 
	 * @param date
	 * @param pageable
	 * @return
	 */
	Page<SysProvisioningBatch> findByNextAttemptLessThanEqual(@Param("nextAttempt") DateTime date, Pageable pageable);
}
