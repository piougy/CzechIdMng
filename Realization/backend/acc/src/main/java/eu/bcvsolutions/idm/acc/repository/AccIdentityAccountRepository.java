package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.dto.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Target system configuration
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(//
		path = "identity-accounts", //
		collectionResourceRel = "identityAccounts", //
		itemResourceRel = "identityAccount", //
		exported = false // we are using repository metadata, but we want expose rest endpoint manually
	)
public interface AccIdentityAccountRepository extends AbstractEntityRepository<AccIdentityAccount, IdentityAccountFilter> {
	
	@Override
	@Query(value = "select e from AccIdentityAccount e left join e.identityRole ir" +
	        " where" +
	        " (?#{[0].accountId} is null or e.account.id = ?#{[0].accountId})" +
	        " and" +
	        " (?#{[0].identityId} is null or e.identity.id = ?#{[0].identityId})" +
	        " and" +
	        " (?#{[0].roleId} is null or ir.role.id = ?#{[0].roleId})" + 
	        " and" +
	        " (?#{[0].identityRoleId} is null or ir.id = ?#{[0].identityRoleId})" + 
	        " and" +
	        " (?#{[0].systemId} is null or e.account.system.id = ?#{[0].systemId})")
	Page<AccIdentityAccount> find(IdentityAccountFilter filter, Pageable pageable);
}
