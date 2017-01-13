package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningRequest;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
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
public interface SysProvisioningRequestRepository extends AbstractEntityRepository<SysProvisioningRequest, EmptyFilter> {

	/*
	 * (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository#find(eu.bcvsolutions.idm.core.api.dto.BaseFilter, Pageable)
	 */
	@Override
	@Query(value = "select e from #{#entityName} e")
	Page<SysProvisioningRequest> find(EmptyFilter filter, Pageable pageable);
	
	/**
	 * Deletes given operation request (ono to one).
	 * 
	 * @param operation
	 * @return
	 */
	int deleteByOperation(@Param("operation") SysProvisioningOperation operation);
}
