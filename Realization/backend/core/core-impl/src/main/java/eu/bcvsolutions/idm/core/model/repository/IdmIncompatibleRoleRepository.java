package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmIncompatibleRole;

/**
 * Segregation of Duties
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
public interface IdmIncompatibleRoleRepository extends AbstractEntityRepository<IdmIncompatibleRole> {
	
	/**
	 * Returns all defined incompatible roles for the given role. Given role can be defined as superior or sub (both sides).
	 * 
	 * @param roleId
	 * @return
	 */
	@Query(value = "SELECT e FROM #{#entityName} e "
	        + "WHERE "
	        + "e.superior.id = :roleId OR e.sub.id = :roleId")
	List<IdmIncompatibleRole> findAllByRole(@Param("roleId") UUID roleId);
	
	/**
	 * Returns all defined incompatible roles for the given role. Given role can be defined as superior or sub (both sides).
	 * 
	 * @param roleIds
	 * @return
	 */
	@Query(value = "SELECT e FROM #{#entityName} e "
	        + "WHERE "
	        + "e.superior.id IN :roleIds OR e.sub.id IN :roleIds")
	List<IdmIncompatibleRole> findAllByRoles(@Param("roleIds") List<UUID> roleIds);
}
