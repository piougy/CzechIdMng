package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityPasswordFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityPassword;

@RepositoryRestResource(
		collectionResourceRel = "identityPasswords",
		itemResourceRel = "identityPassword",
		exported = false
	)
public interface IdmIdentityPasswordRepository extends AbstractEntityRepository<IdmIdentityPassword, IdentityPasswordFilter>{
	
	@Override
	@Query(value = ""
			+ "SELECT e FROM IdmIdentityPassword e"
	        + " WHERE "
	        + "(?#{[0].password} is null or e.password = ?#{[0].password}) "
	        + "AND "
	        + "(?#{[0].validTill} is null or e.validTill <= ?#{[0].validTill}) "
	        + "AND "
	        + "(?#{[0].validFrom} is null or e.validFrom >= ?#{[0].validFrom}) "
	        + "AND "
	        + "(?#{[0].identityId} is null or e.identity.id = ?#{[0].identityId}) "
	        + "AND "
	        + "(?#{[0].mustChange} is null or e.mustChange = ?#{[0].mustChange}) ")
	Page<IdmIdentityPassword> find(IdentityPasswordFilter filter, Pageable pageable);
	
	IdmIdentityPassword findOneByIdentity(@Param("identity") IdmIdentity identity);
}
