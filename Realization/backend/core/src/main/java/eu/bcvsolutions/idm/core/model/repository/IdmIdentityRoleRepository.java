package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

@RepositoryRestResource(//
		collectionResourceRel = "identityRoles", //
		path = "identityRoles", //
		itemResourceRel = "identityRole" //
	)
public interface IdmIdentityRoleRepository extends BaseRepository<IdmIdentityRole> {
	
	Page<IdmIdentityRole> findByIdentity(@Param("identity") IdmIdentity identity, Pageable pageable);

	@RestResource(path = "quick", rel = "quick")
	Page<IdmIdentityRole> findByIdentityUsername(@Param("username") String username, Pageable pageable);
	
	List<IdmIdentityRole> findAllByIdentityAndRole(@Param("identity") IdmIdentity identity, @Param("role") IdmRole role);
	
	@Override
	@RestResource(exported = false)
	IdmIdentityRole save(@Param("entity") IdmIdentityRole entity);
	
	@Override
	@RestResource(exported = false)
	void delete(Long id);

	@Override
	@RestResource(exported = false)
	void delete(IdmIdentityRole entity);
}
