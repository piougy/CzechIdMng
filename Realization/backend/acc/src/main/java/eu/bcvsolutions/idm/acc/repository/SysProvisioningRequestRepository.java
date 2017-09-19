package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.acc.domain.ProvisioningOperation;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningRequestFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningRequest;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Provisioning request log
 * 
 * @author Radek Tomiška
 *
 */
public interface SysProvisioningRequestRepository extends AbstractEntityRepository<SysProvisioningRequest> {

	@Query(value = "select e from #{#entityName} e where "
			+ " ("
        		+ " ?#{[0].operationId} is null or e.operation.id = ?#{[0].operationId}"
	    	+ " ) "
	    	+ " and "
    		+ " ("
    			+ " ?#{[0].batchId} is null or e.batch.id = ?#{[0].batchId}"
    		+ ") ")
	Page<SysProvisioningRequest> find(SysProvisioningRequestFilter filter, Pageable pageable);
	
	/**
	 * Deletes given operation request (one to one).
	 * 
	 * @param operation
	 * @return
	 */
	int deleteByOperation(@Param("operation") ProvisioningOperation operation);
}
