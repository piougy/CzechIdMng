package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.repository.projection.AccAccountExcerpt;
import eu.bcvsolutions.idm.core.model.repository.BaseRepository;

/**
 * Accounts on target system
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "accounts", //
		path = "accounts", //
		itemResourceRel = "account", //
		excerptProjection = AccAccountExcerpt.class,
		exported = false // we are using repository metadata, but we want expose rest endpoint manually
	)
public interface AccAccountRepository extends BaseRepository<AccAccount> {
	
	@Query(value = "select e from AccAccount e" +
	        " where" +
	        " (?#{[0]} is null or e.system.id = ?#{[0]})")
	Page<AccAccount> findQuick(@Param(value = "systemId") Long systemId, @Param(value = "systemEntityId") Long systemEntityId, Pageable pageable);
	
}
