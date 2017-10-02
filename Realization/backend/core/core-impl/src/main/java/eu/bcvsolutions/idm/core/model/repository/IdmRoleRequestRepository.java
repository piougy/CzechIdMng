package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleRequest;

/**
 * Repository for role request
 * 
 * @author svandav
 * @author Radek Tomiška
 */
public interface IdmRoleRequestRepository extends AbstractEntityRepository<IdmRoleRequest> {
	
	/**
	 * Finds request for given applicant in given state
	 * 
	 * @param applicantId
	 * @param state
	 * @return
	 */
	List<IdmRoleRequest> findAllByApplicant_IdAndState(@Param("applicantId") UUID applicantId, @Param("state") RoleRequestState state);

}
