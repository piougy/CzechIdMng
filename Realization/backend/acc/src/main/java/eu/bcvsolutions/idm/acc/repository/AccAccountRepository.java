package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.dto.AccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
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
		exported = false // we are using repository metadata, but we want expose rest endpoint manually
	)
public interface AccAccountRepository extends BaseRepository<AccAccount> {
	
	@Query(value = "select e from AccAccount e left join e.systemEntity se" +
	        " where" +
	        " (?#{[0].systemId} is null or e.system.id = ?#{[0].systemId})" +
	        " and" +
	        " (?#{[0].systemEntityId} is null or se.id = ?#{[0].systemEntityId})" +
	        " and" +
	        " (?#{[0].identityId} is null or exists (from AccIdentityAccount ia where ia.account = e and ia.identity.id = ?#{[0].identityId}))" + 
	        " and" +
	        " (lower(se.uid) like ?#{[0].uid == null ? '%' : '%'.concat([0].uid.toLowerCase()).concat('%')})" +
	        " and" +
	        " (?#{[0].accountType} is null or e.type = ?#{[0].accountType})")
	Page<AccAccount> findQuick(AccountFilter filter, Pageable pageable);
	
}
