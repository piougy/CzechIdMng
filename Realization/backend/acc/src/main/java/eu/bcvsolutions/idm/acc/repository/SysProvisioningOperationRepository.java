package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.dto.filter.ProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Provisioning log
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(
		collectionResourceRel = "provisioningOperations",
		path = "provisioning-operations",
		itemResourceRel = "provisioningOperation",
		exported = false
)
public interface SysProvisioningOperationRepository extends AbstractEntityRepository<SysProvisioningOperation, ProvisioningOperationFilter> {

	/*
	 * (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository#find(eu.bcvsolutions.idm.core.api.dto.BaseFilter, Pageable)
	 */
	@Override
	@Query(value = "select e from #{#entityName} e"
			+ " where"
			+ " ("
	        	+ " ?#{[0].systemId} is null or e.system.id = ?#{[0].systemId}"
	    	+ " ) "
	    	+ " and "
        	+ " (?#{[0].from == null ? 'null' : ''} = 'null' or e.created >= ?#{[0].from}) "
        	+ " and "
        	+ " (?#{[0].till == null ? 'null' : ''} = 'null' or e.created <= ?#{[0].till})"
        	+ " and "
        	+ " (?#{[0].operationType} is null or e.operationType = ?#{[0].operationType})"
        	+ " and "
        	+ " (?#{[0].entityType} is null or e.entityType = ?#{[0].entityType})"
        	+ " and "
        	+ " (?#{[0].entityIdentifier} is null or e.entityIdentifier = ?#{[0].entityIdentifier})"
        	+ " and "
        	+ " (?#{[0].systemEntityUid} is null or e.systemEntityUid = ?#{[0].systemEntityUid})"
        	+ " and "
        	+ " (?#{[0].resultState} is null or e.request.result.state = ?#{[0].resultState})")
	Page<SysProvisioningOperation> find(ProvisioningOperationFilter filter, Pageable pageable);
}
