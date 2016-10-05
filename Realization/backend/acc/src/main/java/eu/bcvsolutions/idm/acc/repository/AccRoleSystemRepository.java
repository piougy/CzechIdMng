package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.dto.RoleSystemFilter;
import eu.bcvsolutions.idm.acc.entity.AccRoleSystem;
import eu.bcvsolutions.idm.core.model.repository.BaseRepository;

/**
 * Role could assign identity account on target system.
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "roleSystems", //
		path = "roleSystems", //
		itemResourceRel = "roleSystem", //
		exported = false // we are using repository metadata, but we want expose rest endpoint manually
	)
public interface AccRoleSystemRepository extends BaseRepository<AccRoleSystem, RoleSystemFilter> {
	
	@Override
	@Query(value = "select e from AccRoleSystem e" +
	        " where" +
	        " (?#{[0].roleId} is null or e.role.id = ?#{[0].roleId})" +
	        " and" +
	        " (?#{[0].systemId} is null or e.system.id = ?#{[0].systemId})")
	Page<AccRoleSystem> find(RoleSystemFilter filter, Pageable pageable);
}
