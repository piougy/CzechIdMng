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
 * @author svandav
 *
 */

public interface IdmRoleRequestRepository extends AbstractEntityRepository<IdmRoleRequest, RoleRequestFilter> {
	
	/*
	 * (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository#find(eu.bcvsolutions.idm.core.api.dto.BaseFilter, Pageable)
	 */
	@Override
	@Query(value = "select e from IdmRoleRequest e" +
	        " where " +
	        " (?#{[0].applicantId} is null or e.applicant.id = ?#{[0].applicantId})" +
	        " and" +
	        " (?#{[0].duplicatedToRequestId} is null or e.duplicatedToRequest.id = ?#{[0].duplicatedToRequestId})" +
	        " and" +
	        " (?#{[0].applicant} is null or e.applicant.username = ?#{[0].applicant})" +
	        " and" +
	        " (?#{[0].states == null ? 0 : [0].states.size()} = 0 or e.state IN (?#{[0].states}))"+ // List must be tested via size not null (bug in spring data probably)
	        " and" +
	        " (?#{[0].state} is null or e.state = ?#{[0].state})")
	Page<IdmRoleRequest> find(RoleRequestFilter filter, Pageable pageable);
	
	/**
	 * Finds request for given applicatnt in given state
	 * 
	 * @param applicantId
	 * @param state
	 * @return
	 */
	List<IdmRoleRequest> findAllByApplicant_IdAndState(@Param("applicantId") UUID applicantId, @Param("state") RoleRequestState state);

}
