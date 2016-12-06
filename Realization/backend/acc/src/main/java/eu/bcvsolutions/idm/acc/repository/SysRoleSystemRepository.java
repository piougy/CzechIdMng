package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.dto.RoleSystemFilter;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.repository.projection.SysRoleSystemExcerpt;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * Role could assign identity account on target system.
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "roleSystems", //
		path = "role-systems", //
		itemResourceRel = "roleSystem", //
		excerptProjection = SysRoleSystemExcerpt.class,
		exported = false // we are using repository metadata, but we want expose rest endpoint manually
	)
public interface SysRoleSystemRepository extends AbstractEntityRepository<SysRoleSystem, RoleSystemFilter> {
	
	/*
	 * (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository#find(eu.bcvsolutions.idm.core.api.dto.BaseFilter, Pageable)
	 */
	@Override
	@Query(value = "select e from SysRoleSystem e" +
	        " where" +
	        " (?#{[0].roleId} is null or e.role.id = ?#{[0].roleId})" +
	        " and" +
	        " (?#{[0].systemId} is null or e.system.id = ?#{[0].systemId})")
	Page<SysRoleSystem> find(RoleSystemFilter filter, Pageable pageable);
	
	/**
	 * Removes mapping by given role
	 * 
	 * @param role
	 * @return
	 */
	int deleteByRole(@Param("role") IdmRole role);
	
	/**
	 * Removes mapping by given system
	 * 
	 * @param system
	 * @return
	 */
	int deleteBySystem(@Param("system") SysSystem system);
}
