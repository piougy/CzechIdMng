package eu.bcvsolutions.idm.core.eav.api.service;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;

/**
 * Custom form value repository can be registered by spring plugin.
 * 
 * @author Radek Tomiška
 *
 * @param <O> values owner
 */
public interface FormValueService<O extends FormableEntity> extends Plugin<Class<?>> {

	public static final String CONFIDENTIAL_STORAGE_VALUE_PREFIX = "eav";
	
	/**
	 * Returns values owner type.
	 * 
	 * @return
	 */
	Class<O> getOwnerClass();
	
	/**
	 * Saves a given form value. Use the returned instance for further operations as the save operation might have changed the
	 * entity instance completely.
	 * 
	 * @param entity
	 * @return the saved entity
	 */
	IdmFormValueDto save(IdmFormValueDto entity);
	
	/**
	 * Returns values by given owner and definition (optional). If no definition is given, then all values from given owner are returned.
	 *
	 * @param owner
	 * @param definiton
	 */
	List<IdmFormValueDto> getValues(O owner, IdmFormDefinitionDto formDefiniton);
	
	/**
	 * Returns values by given owner and attribute (required). If no attribute is given, then {@link IllegalArgumentException} is thrown.
	 *
	 * @param owner
	 * @param attribute
	 */
	List<IdmFormValueDto> getValues(O owner, IdmFormAttributeDto attribute);
	
	/**
	 * Returns page of entities by given filter
	 * 
	 * @param filter
	 * @param pageable
	 * @return
	 */
	Page<IdmFormValueDto> find(IdmFormValueFilter<O> filter, Pageable pageable);
	
	/**
	 * Deletes form value
	 * 
	 * @param value
	 */
	void delete(IdmFormValueDto value);
	
	/**
	 * Deletes values by given owner and definition. If no definition is given, then all values from given owner are deleted.
	 *
	 * @param owner
	 * @param definiton
	 */
	void deleteValues(O owner, IdmFormDefinitionDto formDefiniton);
	
	/**
	 * Deletes values by given owner and attribute. If no attribute is given, then {@link IllegalArgumentException} is thrown.
	 * 
	 * @param owner
	 * @param formAttribute
	 */
	void deleteValues(O owner, IdmFormAttributeDto attribute);
	
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
