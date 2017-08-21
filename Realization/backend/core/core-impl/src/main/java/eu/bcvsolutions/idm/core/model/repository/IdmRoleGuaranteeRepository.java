package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.dto.filter.RoleGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;

/**
 * Role guarantee repository
 * - role guarantee is controlled (CRUD) by role, but some operations are needed to call directly (remove etc.)
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "roleGuarantees", //
		path = "role-guarantees", //
		itemResourceRel = "roleGuarantee", //
		exported = false
)
public interface IdmRoleGuaranteeRepository extends AbstractEntityRepository<IdmRoleGuarantee, RoleGuaranteeFilter> {
	
	/**
	 * @deprecated use IdmRoleGuaranteeService (uses criteria api)
	 */
	@Override
	@Deprecated
	@Query(value = "select e from #{#entityName} e")
	default Page<IdmRoleGuarantee> find(RoleGuaranteeFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Use IdmRoleService (uses criteria api)");
	};
	
	List<IdmRoleGuarantee> findAllByRole(@Param("role") IdmRole role);
	
	/**
	 * Removes guarantee by given identity
	 * 
	 * @param guarantee
	 * @return
	 */
	int deleteByGuarantee_Id(@Param("guarantee") UUID guarantee);
	
	/**
	 * Removes guarantee by given role
	 * 
	 * @param guarantee
	 * @return
	 */
	int deleteByRole(@Param("role") IdmRole role);
}
