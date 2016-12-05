package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.dto.IdentityRoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * Identity roles
 * 
 * @author Radek Tomiška
 */
@RepositoryRestResource(//
		collectionResourceRel = "identityRoles", //
		path = "identity-roles", //
		itemResourceRel = "identityRole",
		exported = false//
	)
public interface IdmIdentityRoleRepository extends AbstractEntityRepository<IdmIdentityRole, IdentityRoleFilter> {
	
	/*
	 * (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository#find(eu.bcvsolutions.idm.core.api.dto.BaseFilter, Pageable)
	 */
	@Override
	@Query(value = "select e from IdmIdentityRole e" +
	        " where " +
	        " (?#{[0].identityId} is null or e.identity.id = ?#{[0].identityId})" +
	        " and" +
	        "(?#{[0].text} is null or lower(e.identity.username) like ?#{[0].text == null ? '%' : '%'.concat([0].toLowerCase()).concat('%')})")
	Page<IdmIdentityRole> find(IdentityRoleFilter filter, Pageable pageable);
	
	Page<IdmIdentityRole> findByIdentity(@Param("identity") IdmIdentity identity, Pageable pageable);
	
	List<IdmIdentityRole> findAllByIdentity(@Param("identity") IdmIdentity identity, Sort sort);

	Page<IdmIdentityRole> findByIdentityUsername(@Param("username") String username, Pageable pageable);
	
	Long countByRole(@Param("role") IdmRole role);
	
	Page<IdmIdentityRole> findByRole(@Param("role") IdmRole role, Pageable pageable);
	
	List<IdmIdentityRole> findAllByIdentityAndRole(@Param("identity") IdmIdentity identity, @Param("role") IdmRole role);
	
	/**
	 * Removes all roles of given identity
	 * 
	 * @param identity
	 * @return
	 */
	int deleteByIdentity(@Param("identity") IdmIdentity identity);
}
