package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.dto.filter.ConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest;

/**
 * Repository for role request
 * 
 * @author svandav
 * @author Radek Tomiška
 */
public interface IdmConceptRoleRequestRepository extends AbstractEntityRepository<IdmConceptRoleRequest, ConceptRoleRequestFilter> {
	
	/**
	 * @deprecated use IdmConceptRoleRequestService (uses criteria api)
	 */
	@Override
	@Deprecated
	@Query(value = "select e from #{#entityName} e")
	default Page<IdmConceptRoleRequest> find(ConceptRoleRequestFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Use IdmConceptRoleRequestService (uses criteria api)");
	}
	
	/**
	 * Finds all concepts for this request
	 * 
	 * @param roleRequestId
	 * @return
	 */
	List<IdmConceptRoleRequest> findAllByRoleRequest_Id(@Param("roleRequestId") UUID roleRequestId);

}
