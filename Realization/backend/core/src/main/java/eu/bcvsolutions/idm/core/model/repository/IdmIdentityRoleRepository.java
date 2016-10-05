package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.model.dto.QuickFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

@RepositoryRestResource(//
		collectionResourceRel = "identityRoles", //
		path = "identityRoles", //
		itemResourceRel = "identityRole",
		exported = false//
	)
public interface IdmIdentityRoleRepository extends BaseRepository<IdmIdentityRole, QuickFilter> {
	
	Page<IdmIdentityRole> findByIdentity(@Param("identity") IdmIdentity identity, Pageable pageable);
	
	List<IdmIdentityRole> findAllByIdentity(@Param("identity") IdmIdentity identity, Sort sort);

	Page<IdmIdentityRole> findByIdentityUsername(@Param("username") String username, Pageable pageable);
	
	@Override
	@Query(value = "select e from IdmIdentityRole e" +
	        " where " +
	        "(?#{[0].text} is null or lower(e.identity.username) like ?#{[0].text == null ? '%' : '%'.concat([0].toLowerCase()).concat('%')})")
	Page<IdmIdentityRole> find(QuickFilter filter, Pageable pageable);
	
	List<IdmIdentityRole> findAllByIdentityAndRole(@Param("identity") IdmIdentity identity, @Param("role") IdmRole role);
}
