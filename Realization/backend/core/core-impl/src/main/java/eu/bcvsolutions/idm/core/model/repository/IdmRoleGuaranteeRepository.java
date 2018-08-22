package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;

/**
 * Role guarantee repository
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmRoleGuaranteeRepository extends AbstractEntityRepository<IdmRoleGuarantee> {
	
	List<IdmRoleGuarantee> findAllByRole(IdmRole role);
	
	/**
	 * Find role guarantees by role id
	 * 
	 * @param roleId
	 * @return
	 */
	List<IdmRoleGuarantee> findAllByRole_Id(UUID roleId);
}
