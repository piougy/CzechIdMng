package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.rest.projection.IdmRoleExcerpt;

/**
 * Roles repository
 * 
 * @author Radek Tomiška 
 *
 */
@RepositoryRestResource( //
		collectionResourceRel = "roles", // 
		path = "roles", //
		itemResourceRel = "role", //
		excerptProjection = IdmRoleExcerpt.class,
		exported = false)
public interface IdmRoleRepository extends AbstractEntityRepository<IdmRole, RoleFilter> {
	
	public static final String ADMIN_ROLE = "superAdminRole"; // TODO: move to configurationService
	
	/**
	 * @deprecated use IdmRoleService (uses criteria api)
	 */
	@Override
	@Deprecated
	@Query(value = "select e from #{#entityName} e")
	default Page<IdmRole> find(RoleFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Use IdmRoleService (uses criteria api)");
	}
	
	IdmRole findOneByName(@Param("name") String name);
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
	@Query(value = "select e from #{#entityName} e where e = :role")
	IdmRole getPersistedRole(@Param("role") IdmRole role);

	@Query("select s.sub from #{#entityName} e join e.subRoles s where e.id = :roleId")
	List<IdmRole> getSubroles(@Param("roleId") UUID roleId);
	
}
