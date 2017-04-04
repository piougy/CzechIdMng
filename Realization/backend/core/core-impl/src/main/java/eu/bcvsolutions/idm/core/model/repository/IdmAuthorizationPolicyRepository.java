package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;

import org.joda.time.LocalDate;
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
	 * Returns all valid policies for given identity and entity type. 
	 * 
	 * @param username identity's username
	 * @param authorizableType
	 * @param disabled
	 * @param currentDate
	 * @return
	 */
	@Query(value = "select e from #{#entityName} e" +
	        " where"
	        + " (e.role.disabled = :disabled)"
	        + " and (e.authorizableType is null or e.authorizableType = :authorizableType)"
	        + " and (e.disabled = :disabled)"
	        + " and exists("
	        	+ " from IdmIdentityRole ir join ir.identityContract ic"
	        	+ " where ir.role = e.role and ic.identity.username = :username"
	        	+ " and (ir.validTill is null or ir.validTill >= :currentDate) and (ir.validFrom is null or ir.validFrom <= :currentDate)"
	        	+ " and (ic.disabled = :disabled and ic.validTill is null or ic.validTill >= :currentDate) and (ic.validFrom is null or ic.validFrom <= :currentDate)"
	        	+ ")"
	        + " order by seq asc")
	List<IdmAuthorizationPolicy> getPolicies(
			@Param("username") String username, 
			@Param("authorizableType") String authorizableType, 
			@Param("disabled") boolean disabled,
			@Param("currentDate") LocalDate currentDate);
}
