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
			+ "SELECT e FROM #{#entityName} e"
	        + " WHERE"
	        + " (?#{[0].password} is null or e.password = ?#{[0].password})"
	        + " AND"
	        + " (?#{[0].validTill == null ? 'null' : ''} = 'null' or e.validTill <= ?#{[0].validTill})"
	        + " AND"
	        + " (?#{[0].validFrom == null ? 'null' : ''} = 'null' or e.validFrom >= ?#{[0].validFrom})"
	        + " AND"
	        + " (?#{[0].identityId} is null or e.identity.id = ?#{[0].identityId})"
	        + " AND"
	        + " (?#{[0].mustChange} is null or e.mustChange = ?#{[0].mustChange})")
	Page<IdmPassword> find(IdmPasswordFilter filter, Pageable pageable);
	
	IdmPassword findOneByIdentity_Id(@Param("identityId") UUID identityId);
}
