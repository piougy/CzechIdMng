package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.dto.filter.AuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorizationPolicy;

/**
 * Assign authorization evaluator to role.
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmAuthorizationPolicyRepository extends AbstractEntityRepository<IdmAuthorizationPolicy, AuthorizationPolicyFilter> {
	
	@Override
	@Query(value = "select e from #{#entityName} e" +
	        " where"
	        + " ("
	        	+ " ?#{[0].roleId} is null"
	        	+ " or"
	        	+ " e.role.id = ?#{[0].roleId}"
	        + " )")
	Page<IdmAuthorizationPolicy> find(AuthorizationPolicyFilter filter, Pageable pageable);
	
	/**
	 * Returns all policies for given identity and entity type
	 * 
	 * @param identityId
	 * @param authorizableType
	 * @param disabled
	 * @return
	 */
	@Query(value = "select e from #{#entityName} e" +
	        " where"
	        + " (e.authorizableType is null or e.authorizableType = :authorizableType)"
	        + " and (e.disabled = :disabled)"
	        + " and exists(from IdmIdentityRole ir where ir.role = e.role and ir.identityContract.identity.id = :identityId)"
	        + " order by seq asc")
	List<IdmAuthorizationPolicy> getPolicies(
			@Param("identityId") UUID identityId, 
			@Param("authorizableType") String authorizableType, 
			@Param("disabled") boolean disabled);
}
