package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;

/**
 * Password policy cointain set of rule, for create, update user password.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * 
 * TODO: weak password dictonary is now only string
 *
 */
public interface IdmPasswordPolicyRepository extends AbstractEntityRepository<IdmPasswordPolicy> {
	
	@Query(value = "SELECT e FROM IdmPasswordPolicy e "
			+ "WHERE "
	        + "e.defaultPolicy = true "
	        + "AND "
	        + "e.type = :type "
	        + "AND "
	        + "e.disabled = false")
	IdmPasswordPolicy findOneDefaultType(@Param("type") IdmPasswordPolicyType type);
	
	IdmPasswordPolicy findOneByName(@Param("name") String name);
}
