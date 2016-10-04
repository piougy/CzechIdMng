package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.core.model.repository.BaseRepository;

/**
 * Target system configuration
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "identityAccounts", //
		path = "identityAccounts", //
		itemResourceRel = "identityAccount", //
		exported = false // we are using repository metadata, but we want expose rest endpoint manually
	)
public interface AccIdentityAccountRepository extends BaseRepository<AccIdentityAccount> {
	
	@Query(value = "select e from AccIdentityAccount e left join e.role r" +
	        " where" +
	        " (?#{[0]} is null or e.account.id = ?#{[0]})" +
	        " and" +
	        " (?#{[1]} is null or e.identity.id = ?#{[1]})" +
	        " and" +
	        " (?#{[2]} is null or r.id = ?#{[2]})")
	Page<AccIdentityAccount> findQuick(
			@Param(value = "accountId") Long accountId, 
			@Param(value = "identityId") Long identityId,
			@Param(value = "roleId") Long roleId,
			Pageable pageable);
}
