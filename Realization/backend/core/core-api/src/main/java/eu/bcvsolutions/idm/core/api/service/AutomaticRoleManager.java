package eu.bcvsolutions.idm.core.api.service;

import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.exception.AcceptedException;
import eu.bcvsolutions.idm.core.api.exception.CoreException;

/**
 * Manager for automatic role by attributes
 * 
 * Beware, the methods doesn't have the annotation {@link Transactional}. In some
 * cases we don't want to create transaction (for example in tests).
 * 
 * @author svandav
 *
 */
public interface AutomaticRoleManager {

	
	/**
	 * Create automatic role by attributes. Uses automatic role request.
	 * 
	 * Beware, the method doesn't have the annotation {@link Transactional}. In some
	 * cases we don't want to create transaction (for example in tests).
	 * 
	 * @throws AcceptedException when request is in progress. 
	 * @throws CoreException when some exceptions occurs;
	 * 
	 * @param automaticRole
	 * @param executeImmediately
	 * @param rules
	 * @return Created automatic role, when request was executed.
	 */
	IdmAutomaticRoleAttributeDto createAutomaticRoleByAttribute(IdmAutomaticRoleAttributeDto automaticRole,
			boolean executeImmediately, IdmAutomaticRoleAttributeRuleDto... rules);

	/**
	 * 
	 * Change rules of the automatic role by attributes. Uses automatic role
	 * request. Add new rules (given rule does not have filled ID). Update exists
	 * rules (given rule has filled ID). Remove rules (remove all rules for this
	 * automatic role, that are not in given rules).
	 * 
	 * Beware, the method doesn't have the annotation {@link Transactional}. In some
	 * cases we don't want to create transaction (for example in tests).
	 * 
	 * @throws AcceptedException
	 *             when request is in progress.
	 * @throws CoreException
	 *             when some exceptions occurs;
	 * 
	 * @param automaticRole
	 * @param executeImmediately
	 * @param rules
	 * @return Created automatic role, when request was executed.
	 */
	IdmAutomaticRoleAttributeDto changeAutomaticRoleRules(IdmAutomaticRoleAttributeDto automaticRole,
			boolean executeImmediately, IdmAutomaticRoleAttributeRuleDto... rules);

	
	/**
	 * Create automatic role by tree node. Uses automatic role request
	 * 
	 * Beware, the method doesn't have the annotation {@link Transactional}. In some
	 * cases we don't want to create transaction (for example in tests).
	 * 
	 * @throws AcceptedException when request is in progress. 
	 * @throws CoreException when some exceptions occurs;
	 * 
	 * @param automaticRole
	 * @param executeImmediately
	 * @return
	 */
	IdmRoleTreeNodeDto createAutomaticRoleByTree(IdmRoleTreeNodeDto automaticRole, boolean executeImmediately);

	/**
	 * Delete exists automatic role by tree node or by attribute. Uses automatic role request.
	 * 
	 * Beware, the method doesn't have the annotation {@link Transactional}. In some
	 * cases we don't want to create transaction (for example in tests).
	 * 
	 * @param automaticRole
	 * @param executeImmediately
	 */
	void deleteAutomaticRole(AbstractIdmAutomaticRoleDto automaticRole, boolean executeImmediately);
}
