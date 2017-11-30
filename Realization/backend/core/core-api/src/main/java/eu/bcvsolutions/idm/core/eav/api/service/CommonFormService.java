package eu.bcvsolutions.idm.core.eav.api.service;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDto;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Common eav forms.
 * - can be assigned to {@link FormableEntity}, which has to have {@link UUID} identifier.
 * - {@link AbstractEntity} type or {@link AbstractDto} can be used as owner type.
 * - used mainly for internal purpose - storing filters, configurations, etc.
 * - don't use for storing extended attributes! Use {@link FormService} instead.
 * - when owner is deleted, then all forms have to be deleted to - override owner's service delete method properly.
 *
 * Underlying {@link AbstractEntity} has to extend {@link FormableEntity}.
 * If {@link AbstractDto} is given as owner type, then {@link FormableEntity} owner will be found by
 * {@link LookupService} => transformation to {@link FormableEntity}.
 *
 * @see FormService
 * @see FormableEntity
 *
 * @author Radek Tomiška
 * @since 7.6.0
 */
public interface CommonFormService {

	/**
	 * Finds all forms by given owner. One owner can have more forms (e.g. with different name, definition, etc ...)
	 *
	 * @param owner
	 * @param permission permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	List<IdmFormDto> getForms(Identifiable owner, BasePermission... permission);

	/**
	 * Saves form
	 *
	 * @param owner form's owner
	 * @param form
	 * @param permission permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	IdmFormDto saveForm(Identifiable owner, IdmFormDto form, BasePermission... permission);

	/**
	 * Deletes all forms by given owner
	 *
	 * @param owner
	 * @param permission permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	void deleteForms(Identifiable owner, BasePermission... permission);
}
