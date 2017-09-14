package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest;

/**
 * Repository for role request
 * 
 * @author svandav
 * @author Radek Tomiška
 */
public interface IdmConceptRoleRequestRepository extends AbstractEntityRepository<IdmConceptRoleRequest> {
	
	/**
	 * Finds all concepts for this request
	 * 
	 * @param roleRequestId
	 * @return
	 */
	List<IdmConceptRoleRequest> findAllByRoleRequest_Id(@Param("roleRequestId") UUID roleRequestId);

}
