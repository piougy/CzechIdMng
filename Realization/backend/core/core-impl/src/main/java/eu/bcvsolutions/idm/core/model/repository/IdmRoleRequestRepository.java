package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.filter.RoleRequestFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleRequest;

/**
 * Repository for role request
 * 
 * @author svandav
 * @author Radek Tomiška
 *
 */
public interface IdmRoleRequestRepository extends AbstractEntityRepository<IdmRoleRequest, RoleRequestFilter> {
	
	/**
	 * @deprecated use IdmRoleRequestService (uses criteria api)
	 */
	@Override
	@Deprecated
	@Query(value = "select e from #{#entityName} e")
	default Page<IdmRoleRequest> find(RoleRequestFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Use IdmRoleRequestService (uses criteria api)");
	}
	
	/**
	 * Finds request for given applicatnt in given state
	 * 
	 * @param applicantId
	 * @param state
	 * @return
	 */
	List<IdmRoleRequest> findAllByApplicant_IdAndState(@Param("applicantId") UUID applicantId, @Param("state") RoleRequestState state);

}
