package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.repository.ExternalIdentifiableRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * Roles repository
 * 
 * @author Radek Tomiška 
 *
 */
public interface IdmRoleRepository extends AbstractEntityRepository<IdmRole>, ExternalIdentifiableRepository<IdmRole, UUID> {
	
	/**
	 * @deprecated use {@link #findOneByCode(String)}
	 */
	@Deprecated
	IdmRole findOneByName(@Param("name") String name);
	
	@Query(value = "select e from #{#entityName} e where e.name = :code")
	IdmRole findOneByCode(@Param("code") String code);
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
	@Query(value = "select e from #{#entityName} e where e = :role")
	IdmRole getPersistedRole(@Param("role") IdmRole role);

	@Query("select s.sub from #{#entityName} e join e.subRoles s where e.id = :roleId")
	List<IdmRole> getSubroles(@Param("roleId") UUID roleId);
	
}
