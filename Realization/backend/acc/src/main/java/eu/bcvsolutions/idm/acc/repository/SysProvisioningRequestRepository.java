package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.domain.ProvisioningOperation;
import eu.bcvsolutions.idm.acc.dto.filter.ProvisioningRequestFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningRequest;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Provisioning request log
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(
		collectionResourceRel = "provisioningRequests",
		path = "provisioning-requests",
		itemResourceRel = "provisioningRequest",
		exported = false
)
public interface SysProvisioningRequestRepository extends AbstractEntityRepository<SysProvisioningRequest, ProvisioningRequestFilter> {

	/*
	 * (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository#find(eu.bcvsolutions.idm.core.api.dto.BaseFilter, Pageable)
	 */
	@Override
	@Query(value = "select e from #{#entityName} e where "
			+ " ("
        		+ " ?#{[0].operationId} is null or e.operation.id = ?#{[0].operationId}"
	    	+ " ) "
	    	+ " and "
    		+ " ("
    			+ " ?#{[0].batchId} is null or e.batch.id = ?#{[0].batchId}"
    		+ ") ")
	Page<SysProvisioningRequest> find(ProvisioningRequestFilter filter, Pageable pageable);
	
	/**
	 * Deletes given operation request (one to one).
	 * 
	 * @param operation
	 * @return
	 */
	int deleteByOperation(@Param("operation") ProvisioningOperation operation);
}
