package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.filter.AuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorizationPolicy;

/**
 * Assign authorization evaluator to role.
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmAuthorizationPolicyRepository extends AbstractEntityRepository<IdmAuthorizationPolicy, AuthorizationPolicyFilter> {
	
	/**
	 * @deprecated Use IdmAuthorizationPolicyService (uses criteria api)
	 */
	@Override
	@Deprecated
	@Query(value = "select e from #{#entityName} e")
	default Page<IdmAuthorizationPolicy> find(AuthorizationPolicyFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Use IdmAuthorizationPolicyService (uses criteria api)");
	}
	
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
	        	+ " where ir.role = e.role and ic.identity.id = :identityId"
	        	+ " and (ir.validTill is null or ir.validTill >= :currentDate) and (ir.validFrom is null or ir.validFrom <= :currentDate)"
	        	+ " and (ic.disabled = :disabled and ic.validTill is null or ic.validTill >= :currentDate) and (ic.validFrom is null or ic.validFrom <= :currentDate)"
	        	+ ")"
	        + " order by seq asc")
	List<IdmAuthorizationPolicy> getPolicies(
			@Param("identityId") UUID identityId, 
			@Param("authorizableType") String authorizableType, 
			@Param("disabled") boolean disabled,
			@Param("currentDate") LocalDate currentDate);
	
	/**
	 * Return enabled role's policies (role and policy has to be enabled).
	 * 
	 * @param roleId
	 * @param disabled
	 * @return
	 */
	@Query(value = "select e from #{#entityName} e join e.role r"
			+ " where"
			+ " r.id = :roleId"
			+ " and"
			+ " r.disabled = :disabled"
			+ " and"
			+ " e.disabled = :disabled")
	List<IdmAuthorizationPolicy> getPolicies(
			@Param("roleId") UUID roleId,
			@Param("disabled") boolean disabled);
	
	/**
	 * Return enabled and persisted (outside current transaction)
	 * role's policies (role and policy has to be enabled).
	 * 
	 * @param roleId
	 * @param disabled
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
	@Query(value = "select e from #{#entityName} e join e.role r"
			+ " where"
			+ " r.id = :roleId"
			+ " and"
			+ " r.disabled = :disabled"
			+ " and"
			+ " e.disabled = :disabled")
	List<IdmAuthorizationPolicy> getPersistedPolicies(
			@Param("roleId") UUID roleId,
			@Param("disabled") boolean disabled);
	
	/**
	 * Return persisted policy by ID.
	 * 
	 * @param policyId
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
	@Query(value = "select e from #{#entityName} e where e.id = :policyId")
	IdmAuthorizationPolicy getPersistedPolicy(@Param("policyId") UUID policyId);

}
