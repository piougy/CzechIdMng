package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.entity.AccRoleSystem;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
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
public interface AccRoleSystemRepository extends BaseRepository<AccRoleSystem> {

	
	@Query(value = "select e from AccRoleSystem e" +
	        " where" +
	        " (?#{[0]} is null or e.role.id = ?#{[0]})" +
	        " and" +
	        " (?#{[1]} is null or e.system.id = ?#{[1]})")
	Page<AccRoleSystem> findQuick(@Param(value = "roleId") Long roleId, @Param(value = "systemId") Long systemId, Pageable pageable);
}
