package eu.bcvsolutions.idm.core.model.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmPasswordPolicyFilter;
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
	
	@Query(value = "SELECT e FROM IdmPasswordPolicy e" +
	        " WHERE"
	        + " ("
		        + " ?#{[0].text} is null"
		        + " or lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}"
	        + " ) "
	        + " AND "
	        	+ "("
	        		+ "?#{[0].passwordLengthRequired} is null "
	        		+ "or e.passwordLengthRequired = ?#{[0].passwordLengthRequired}"
	        	+ ")"
	        + " AND "
		        + "("
			        + "?#{[0].minPasswordLength} is null "
			        + "or e.minPasswordLength = ?#{[0].minPasswordLength}"
		        + ")"
	        + " AND "
		        + "("
			        + "?#{[0].maxPasswordLength} is null "
			        + "or e.maxPasswordLength = ?#{[0].maxPasswordLength}"
		        + ")"
	        + " AND "
		        + "("
			        + "?#{[0].upperCharRequired} is null "
			        + "or e.upperCharRequired = ?#{[0].upperCharRequired}"
		        + ")"
	        + " AND "
		        + "("
			        + "?#{[0].minUpperChar} is null "
			        + "or e.minUpperChar = ?#{[0].minUpperChar}"
		        + ")"
	        + " AND "
		        + "("
			        + "?#{[0].numberRequired} is null "
			        + "or e.numberRequired = ?#{[0].numberRequired}"
		        + ")"
	        + " AND "
		        + "("
			        + "?#{[0].minNumber} is null "
			        + "or e.minNumber = ?#{[0].minNumber}"
		        + ")"
	        + " AND "
		        + "("
			        + "?#{[0].specialCharRequired} is null "
			        + "or e.specialCharRequired = ?#{[0].specialCharRequired}"
		        + ")"
	        + " AND "
		        + "("
			        + "?#{[0].minSpecialChar} is null "
			        + "or e.minSpecialChar = ?#{[0].minSpecialChar}"
		        + ")"
	        + " AND "
		        + "("
			        + "?#{[0].weakPassRequired} is null "
			        + "or e.weakPassRequired = ?#{[0].weakPassRequired}"
		        + ")"
	        + " AND "
		        + "("
			        + "?#{[0].weakPass} is null "
			        + "or e.weakPass = ?#{[0].weakPass}"
		        + ")"
	        + " AND "
		        + "("
			        + "?#{[0].maxPasswordAge} is null "
			        + "or e.maxPasswordAge = ?#{[0].maxPasswordAge}"
		        + ")"
	        + " AND "
		        + "("
			        + "?#{[0].minPasswordAge} is null "
			        + "or e.minPasswordAge = ?#{[0].minPasswordAge}"
		        + ")"
	        + " AND "
		        + "("
			        + "?#{[0].enchancedControl} is null "
			        + "or e.enchancedControl = ?#{[0].enchancedControl}"
		        + ")"
	        + " AND "
		        + "("
			        + "?#{[0].minRulesToFulfill} is null "
			        + "or e.minRulesToFulfill = ?#{[0].minRulesToFulfill}"
		        + ")"
	        + " AND "
		        + "("
			        + "?#{[0].type} is null "
			        + "or e.type = ?#{[0].type}"
		        + ")"
        	+ " AND "
	        	+ "("
		        	+ "?#{[0].defaultPolicy} is null "
		        	+ "or e.defaultPolicy = ?#{[0].defaultPolicy}"
	        	+ ")")
	Page<IdmPasswordPolicy> find(IdmPasswordPolicyFilter filter, Pageable pageable);
	

	@Query(value = "SELECT e FROM IdmPasswordPolicy e "
			+ "WHERE "
	        + "e.defaultPolicy = true "
	        + "AND "
	        + "e.type = :type "
	        + "AND "
	        + "e.disabled = false")
	IdmPasswordPolicy findOneDefaultType(@Param("type") IdmPasswordPolicyType type);
	
	IdmPasswordPolicy findOneByName(@Param("name") String name);
	
	@Modifying
	@Query("UPDATE IdmPasswordPolicy e SET e.defaultPolicy = false WHERE e.type = :type "
			+ "AND (:updatedEntityId is null or e.id != :updatedEntityId) ")
	void updateDefaultPolicyByType(@Param("type") IdmPasswordPolicyType type, @Param("updatedEntityId") UUID updatedEntityId);
}
