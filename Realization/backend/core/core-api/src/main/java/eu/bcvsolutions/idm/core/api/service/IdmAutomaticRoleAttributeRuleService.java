package eu.bcvsolutions.idm.core.api.service;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleAttributeRuleFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service of rules for autoamtic role attributes
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmAutomaticRoleAttributeRuleService
		extends ReadWriteDtoService<IdmAutomaticRoleAttributeRuleDto, IdmAutomaticRoleAttributeRuleFilter>,
		AuthorizableService<IdmAutomaticRoleAttributeRuleDto> {

	/**
	 * Delete all rules by given attribute id
	 * 
	 * @param attributeId
	 */
	void deleteAllByAttribute(UUID attributeId);
	
	/**
	 * Return all {@link IdmAutomaticRoleAttributeRuleDto} for given id of  {@link IdmAutomaticRoleAttributeDto}
	 * and is equals with given {@link AutomaticRoleAttributeRuleType}.
	 * 
	 * @param automaticRole
	 * @param type
	 * @return
	 */
	List<IdmAutomaticRoleAttributeRuleDto> findAllRulesForAutomaticRoleAndType(UUID automaticRole,
			AutomaticRoleAttributeRuleType type);
	
}
