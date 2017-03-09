package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.dto.filter.ConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest;

/**
 * Repository for role request
 * @author svandav
 *
 */
public interface IdmConceptRoleRequestRepository extends AbstractEntityRepository<IdmConceptRoleRequest, ConceptRoleRequestFilter> {
	
	/*
	 * (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository#find(eu.bcvsolutions.idm.core.api.dto.BaseFilter, Pageable)
	 */
	@Override
	@Query(value = "select e from IdmConceptRoleRequest e" +
	        " where " +
	        " (?#{[0].roleRequestId} is null or e.roleRequest.id = ?#{[0].roleRequestId})" +
	        " and" +
	        " (?#{[0].state} is null or e.state = ?#{[0].state})")
	Page<IdmConceptRoleRequest> find(ConceptRoleRequestFilter filter, Pageable pageable);

}
