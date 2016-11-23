package eu.bcvsolutions.idm.eav.service.api;

import java.util.List;
import java.util.UUID;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.eav.entity.FormableEntity;
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
	 * Deletes values by given owner and definition. If no definition is given, then all values from given owner are deleted.
	 *
	 * @param owner
	 * @param definiton
	 */
	void deleteValues(O owner, IdmFormDefinition formDefiniton);
}
