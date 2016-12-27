package eu.bcvsolutions.idm.eav.service.api;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.eav.dto.filter.FormValueFilter;
import eu.bcvsolutions.idm.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;

/**
 * Custom form value repository can be registered by spring plugin
 * 
 * @author Radek Tomiška
 *
 * @param <O> values owner
 * @param <E> values entity
 */
public interface FormValueService<O extends FormableEntity, E extends AbstractFormValue<O>> extends Plugin<Class<?>> {

	public static final String CONFIDENTIAL_STORAGE_VALUE_PREFIX = "eav";
	
	/**
	 * Saves a given form value. Use the returned instance for further operations as the save operation might have changed the
	 * entity instance completely.
	 * 
	 * @param entity
	 * @return the saved entity
	 */
	E save(E entity);
	
	/**
	 * Returns entity by given id. Returns null, if entity is not exists.
	 * 
	 * @param id
	 * @return
	 */
	E get(UUID id);
	
	/**
	 * Returns values by given owner and definition (optional). If no definition is given, then all values from given owner are returned.
	 *
	 * @param owner
	 * @param definiton
	 */
	List<E> getValues(O owner, IdmFormDefinition formDefiniton);
	
	/**
	 * Returns page of entities by given filter
	 * 
	 * @param filter
	 * @param pageable
	 * @return
	 */
	Page<E> find(FormValueFilter<O> filter, Pageable pageable);
	
	/**
	 * Deletes form value
	 * 
	 * @param value
	 */
	void deleteValue(E value);
	
	/**
	 * Deletes values by given owner and definition. If no definition is given, then all values from given owner are deleted.
	 *
	 * @param owner
	 * @param definiton
	 */
	void deleteValues(O owner, IdmFormDefinition formDefiniton);
	
	/**
	 * Returns key in confidential storage for given extended attribute
	 * 
	 * TODO: owner is not needed now ... but?
	 * 
	 * @param attribute
	 * @return
	 */
	String getConfidentialStorageKey(IdmFormAttribute attribute);
	
	/**
	 * Returns value in FormValue's persistent type from confidential storage
	 * 
	 * @see {@link #getConfidentialStorageKey(IdmFormAttribute)}
	 * 
	 * @param guardedValue
	 * @return
	 */
	Serializable getConfidentialPersistentValue(E guardedValue);
}
