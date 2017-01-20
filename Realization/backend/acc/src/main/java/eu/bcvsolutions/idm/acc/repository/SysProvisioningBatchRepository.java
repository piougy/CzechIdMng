package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningBatch;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Provisioning operation batches. Any operation has request and batch.
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(
		collectionResourceRel = "provisioningBatches",
		path = "provisioning-batches",
		itemResourceRel = "provisioningBatch",
		exported = false
)
public interface SysProvisioningBatchRepository extends AbstractEntityRepository<SysProvisioningBatch, EmptyFilter> {

	/*
	 * (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository#find(eu.bcvsolutions.idm.core.api.dto.BaseFilter, Pageable)
	 */
	@Override
	@Query(value = "select e from #{#entityName} e")
	Page<SysProvisioningBatch> find(EmptyFilter filter, Pageable pageable);
	
	/**
	 * Finds batch for given operation.
	 * 
	 * @param operation
	 * @return
	 */
	@Query(value = "select distinct(r.batch) from SysProvisioningRequest r join r.operation o"
			+ " where o.system = ?#{[0].system} and o.entityIdentifier = ?#{[0].entityIdentifier} and o.systemEntityUid = ?#{[0].systemEntityUid}")
	SysProvisioningBatch findBatch(SysProvisioningOperation operation);
}
