package eu.bcvsolutions.idm.core.model.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmPasswordFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmPassword;

/**
 * Storing crypted passwords to IdM
 * 
 * @author Ondřej Kopr
 * @author Radek Tomiška
 *
 */
public interface IdmPasswordRepository extends AbstractEntityRepository<IdmPassword> {
	
	@Query(value = ""
			+ "select e from #{#entityName} e"
	        + " where"
	        + " (?#{[0].password} is null or e.password = ?#{[0].password})"
	        + " and"
	        + " (?#{[0].validTill == null ? 'null' : ''} = 'null' or e.validTill <= ?#{[0].validTill})"
	        + " and"
	        + " (?#{[0].validFrom == null ? 'null' : ''} = 'null' or e.validFrom >= ?#{[0].validFrom})"
	        + " and"
	        + " (?#{[0].identityId} is null or e.identity.id = ?#{[0].identityId})"
	        + " and"
	        + " (?#{[0].mustChange} is null or e.mustChange = ?#{[0].mustChange})"
	        + " and"
	        + " (?#{[0].identityDisabled} is null or e.identity.disabled = ?#{[0].identityDisabled})")
	Page<IdmPassword> find(IdmPasswordFilter filter, Pageable pageable);
	
	/**
	 * Identity has one password (or any).
	 * 
	 * @param identityId
	 * @return
	 */
	IdmPassword findOneByIdentity_Id(@Param("identityId") UUID identityId);

	/**
	 * Identity has one password (or any).
	 *
	 * @param username
	 * @return
	 */
	IdmPassword findOneByIdentity_username(@Param("username") String username);
}
