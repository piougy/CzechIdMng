package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleAttributeRule;

/**
 * Repository of rules for automatic role attribute
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmAutomaticRoleAttributeRuleRepository extends AbstractEntityRepository<IdmAutomaticRoleAttributeRule> {

	@Query(value = ""
			+ "SELECT e FROM #{#entityName} e WHERE"
				+ "(?#{:automaticRoles == null ? 0 : :automaticRoles.size()} = 0 OR e.automaticRoleAttribute IN (?#{:automaticRoles}))"
			+ "AND"
				+ ":type IS NULL OR :type = e.type")
	List<IdmAutomaticRoleAttributeRule> findAllByAutomaticRolesAndType(@Param(value = "automaticRoles") List<UUID> automaticRoles, @Param(value = "type") AutomaticRoleAttributeRuleType type);
}
