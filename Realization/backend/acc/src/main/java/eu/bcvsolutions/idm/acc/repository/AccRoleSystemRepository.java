package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

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
public interface AccRoleSystemRepository extends BaseRepository<AccRoleSystem> {

}
