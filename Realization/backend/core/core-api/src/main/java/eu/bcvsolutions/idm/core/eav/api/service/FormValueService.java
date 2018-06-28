package eu.bcvsolutions.idm.core.eav.api.service;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Custom form value service (and repository) can be registered by spring plugin.
 * Use {@link FormService} in your code. {@link FormService} uses form value services internally.
 * 
 * TODO: Support identifiable owner? Or owner just by id?
 * 
 * @see FormService
 * @see CommonFormService
 * @author Radek Tomiška
 *
 * @param <O> values owner
 */
public interface FormValueService<O extends FormableEntity> extends 
		ReadWriteDtoService<IdmFormValueDto, IdmFormValueFilter<O>>,
		Plugin<Class<?>>,
		AuthorizableService<IdmFormValueDto> {

	String CONFIDENTIAL_STORAGE_VALUE_PREFIX = "eav";
	String PROPERTY_OWNER = "owner";
	
	/**
	 * Returns values owner type.
	 * 
	 * @return
	 */
	Class<O> getOwnerClass();
	
	/**
	 * Returns values by given owner and definition (optional). If no definition is given, then all values from given owner are returned.
	 *
	 * @param owner
	 * @param definiton [optional] If no definition is given, then all values from given owner are returned.
	 * @param permission base permissions to evaluate (AND)
	 */
	List<IdmFormValueDto> getValues(O owner, IdmFormDefinitionDto formDefiniton, BasePermission... permission);
	
	/**
	 * Returns values by given owner and attribute (required). If no attribute is given, then {@link IllegalArgumentException} is thrown.
	 *
	 * @param owner
	 * @param attribute
	 * @param permission base permissions to evaluate (AND)
	 */
	List<IdmFormValueDto> getValues(O owner, IdmFormAttributeDto attribute, BasePermission... permission);
	
	/**
	 * Deletes values by given owner and definition. If no definition is given, then all values from given owner are deleted.
	 *
	 * @param owner
	 * @param definiton
	 * @param permission base permissions to evaluate (AND)
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	void deleteValues(O owner, IdmFormDefinitionDto formDefiniton, BasePermission... permission);
	
	/**
	 * Deletes values by given owner and attribute. If no attribute is given, then {@link IllegalArgumentException} is thrown.
	 * 
	 * @param owner
	 * @param formAttribute
	 * @param permission base permissions to evaluate (AND)
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	void deleteValues(O owner, IdmFormAttributeDto attribute, BasePermission... permission);
	
	/**
	 * Returns key in confidential storage for given extended attribute
	 * 
	 * @param formAttributeId
	 * @return
	 */
	String getConfidentialStorageKey(UUID formAttributeId);
	
	/**
	 * Returns value in FormValue's persistent type from confidential storage
	 * 
	 * @see {@link #getConfidentialStorageKey(IdmFormAttribute)}
	 * 
	 * @param guardedValue
	 * @return
	 */
	Serializable getConfidentialPersistentValue(IdmFormValueDto guardedValue);
	
	/**
	 * Finds owners by string attribute value
	 * 
	 * @param attribute
	 * @param persistentValue
	 * @param pageable
	 * @return
	 */
	Page<O> findOwners(IdmFormAttributeDto attribute, Serializable persistentValue, Pageable pageable);
}
