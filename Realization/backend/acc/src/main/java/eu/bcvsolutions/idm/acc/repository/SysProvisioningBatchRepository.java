package eu.bcvsolutions.idm.acc.repository;

import java.util.List;
import java.util.UUID;

import java.time.ZonedDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningBatch;
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
	 * Returns all batches for given system entity
	 * 
	 * One system entity should have just one batch (batches are merged by provisioning batch service)
	 * 
	 * @param systemEntityId
	 * @return
	 */
	List<SysProvisioningBatch> findAllBySystemEntity_IdOrderByCreatedAsc(UUID systemEntityId);
	
	/**
	 * Returns batches by their request's state
	 * 
	 * @param state
	 * @param pageable
	 * @return
	 */
	@Query(value = "select e from #{#entityName} e where exists (select o.id from SysProvisioningOperation o"
			+ " where"
			+ " o.batch = e"
			+ " and"
			+ " o.result.state = :state"
			+ " and o.system.queue = TRUE)")
	Page<SysProvisioningBatch> findByOperationState(@Param("state") OperationState state, Pageable pageable);
	
	/**
	 * Returns batches by their system is virtual and request's state
	 * 
	 * @param state
	 * @param pageable
	 * @return
	 */
	@Query(value = "select e from #{#entityName} e where exists (select o.id from SysProvisioningOperation o"
			+ " where"
			+ " o.batch = e"
			+ " and"
			+ " o.system.virtual = :virtualSystem"
			+ " and"
			+ " o.result.state = :state"
			+ " and o.system.queue = TRUE)")
	Page<SysProvisioningBatch>  findByVirtualSystemAndOperationState(@Param("virtualSystem") Boolean virtualSystem, @Param("state") OperationState state, Pageable pageable);
	
	/**
	 * Returns unprocessed planned batches
	 * 
	 * @param date
	 * @param pageable
	 * @return
	 */
	Page<SysProvisioningBatch> findByNextAttemptLessThanEqual(@Param("nextAttempt") ZonedDateTime date, Pageable pageable);
	
	/**
	 * Merge batches for provisioning operations
	 * 
	 * @param newBatch
	 * @param oldBatch
	 * @return
	 */
	@Modifying(clearAutomatically = true)
	@Query("update SysProvisioningOperation o set o.batch = :newBatch where o.batch = :oldBatch")
	int mergeBatch(@Param("oldBatch") SysProvisioningBatch oldBatch, @Param("newBatch") SysProvisioningBatch newBatch);
}
