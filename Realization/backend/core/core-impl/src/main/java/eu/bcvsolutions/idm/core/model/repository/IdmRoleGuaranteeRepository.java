package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

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
	
	List<IdmRoleGuarantee> findAllByRole(IdmRole role);
	
	/**
	 * Find role guarantees by role id
	 * 
	 * @param roleId
	 * @return
	 */
	List<IdmRoleGuarantee> findAllByRole_Id(UUID roleId);
}
