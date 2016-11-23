package eu.bcvsolutions.idm.eav.service.api;

import java.util.List;
import java.util.Map;

import eu.bcvsolutions.idm.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.eav.entity.FormableEntity;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;

/**
 * Work with form definitions, attributes and their values
 * 
 * @author Radek Tomiška
 *
 */
public interface FormService {
	
	/**
	 * Finds default definition by given type
	 * 
	 * @param type
	 * @return
	 */
	IdmFormDefinition getDefinition(String type);

	/**
	 * Finds definition by given type and name (optional) 
	 * 
	 * @param type
	 * @param name optional - if no name given, then returns default definition
	 * @return
	 */
	IdmFormDefinition getDefinition(String type, String name);
	
	/**
	 * Creates form definition
	 * 
	 * @param type
	 * @param name
	 * @param formAttributes
	 * @return
	 */
	IdmFormDefinition createDefinition(String type, String name, List<IdmFormAttribute> formAttributes);
		
	/**
	 * Save form values to given owner
	 * 
	 * @param owner
	 * @param values
	 */
	<O extends FormableEntity, E extends AbstractFormValue<O>> void saveValues(O owner, IdmFormDefinition formDefinition, List<E> values);
	
	/**
	 * Reads form values by given owner. Return all values from all form definitions
	 * 
	 * @param owner
	 * @return
	 */
	<O extends FormableEntity> List<AbstractFormValue<O>> getValues(O owner);

	/**
	 * Reads form values by given owner and form definition
	 * 
	 * @param owner
	 * @param formDefinition
	 * @return
	 */
	<O extends FormableEntity> List<AbstractFormValue<O>> getValues(O owner, IdmFormDefinition formDefinition);
	
	/**
	 * Returns form values as map, where key is attribute name
	 * 
	 * @param values
	 * @return
	 */
	<O extends FormableEntity, E extends AbstractFormValue<O>> Map<String, List<E>> toAttributeMap(final List<E> values);
	
	/**
	 * Deletes form values by given owner
	 * 
	 * @param owner
	 * @return
	 */
	<O extends FormableEntity> void deleteValues(O owner);
	
	/**
	 * Deletes form values by given owner and form definition
	 * 
	 * @param owner
	 * @param formDefinition
	 * @return
	 */
	<O extends FormableEntity> void deleteValues(O owner, IdmFormDefinition formDefinition);
}
