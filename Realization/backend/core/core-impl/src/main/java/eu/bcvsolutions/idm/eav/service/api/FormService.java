package eu.bcvsolutions.idm.eav.service.api;

import java.util.List;
import java.util.Map;

import eu.bcvsolutions.idm.eav.domain.PersistentType;
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
	 * Finds default definition by given owner
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * 
	 * @param ownerClass owner type
	 * @return
	 */
	IdmFormDefinition getDefinition(Class<? extends FormableEntity> ownerClass);
	
	/**
	 * Return default definition name for given owner type.
	 * 
	 * @param ownerClass owner type
	 * @return
	 */
	String getDefaultDefinitionType(Class<? extends FormableEntity> ownerClass);
	
	/**
	 * Creates form definition
	 * 
	 * @param type definition type
	 * @param name definition name (default will be created, if no definition name is given)
	 * @param formAttributes
	 * @return
	 */
	IdmFormDefinition createDefinition(String type, String name, List<IdmFormAttribute> formAttributes);
	
	/**
	 * Creates default form definition for given owner type
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * 
	 * @param ownerClass owner type
	 * @param formAttributes
	 * @return
	 */
	IdmFormDefinition createDefinition(Class<? extends FormableEntity> ownerClass, List<IdmFormAttribute> formAttributes);
		
	/**
	 * Save form values to given owner and form definition.
	 * 
	 * @param owner
	 * @param formDefinition
	 * @param values
	 */
	<O extends FormableEntity, E extends AbstractFormValue<O>> void saveValues(O owner, IdmFormDefinition formDefinition, List<E> values);
	
	/**
	 * Reads form values by given owner. Return values from default form definition.
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * 
	 * @param owner
	 * @return
	 * @throws IllegalArgumentException if default definition does not exist
	 */
	<O extends FormableEntity> List<AbstractFormValue<O>> getValues(O owner);

	/**
	 * Reads form values by given owner and form definition
	 * 
	 * @param owner
	 * @param formDefinition [optional] if form definition is not given, then return attribute values from default definition
	 * @return
	 * @throws IllegalArgumentException if form definition is not given and default definition does not exist
	 */
	<O extends FormableEntity> List<AbstractFormValue<O>> getValues(O owner, IdmFormDefinition formDefinition);
	
	/**
	 * Returns attribute values by attributeName from given definition, or empty collection
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * 
	 * @param owner
	 * @param formDefinition [optional] if form definition is not given, then return attribute values from default definition
	 * @param attributeName
	 * @return
	 * @throws IllegalArgumentException if form definition is not given and default definition does not exist
	 */
	<O extends FormableEntity> List<AbstractFormValue<O>> getValues(O owner, IdmFormDefinition formDefinition, String attributeName);
	
	/**
	 * Returns form values as map, where key is attribute name
	 * 
	 * @param values
	 * @return
	 */
	<O extends FormableEntity, E extends AbstractFormValue<O>> Map<String, List<E>> toValueMap(final List<E> values);
	
	/**
	 * Returns FormValue values by persistent type as map, where key is attribute name
	 * 
	 * @see {@link PersistentType}
	 * @param values
	 * @return
	 */
	Map<String, List<Object>> toPersistentValueMap(final List<AbstractFormValue<FormableEntity>> values);
	
	/**
	 * Returns FormValue values by persistent type - usable for single multi attribute values
	 * 
	 * @see {@link PersistentType}
	 * @param values
	 * @return
	 */
	List<Object> toPersistentValues(final List<AbstractFormValue<FormableEntity>> values);
	
	/**
	 * Returns single FormValue by persistent type - usable for single attribute value
	 * 
	 * @see {@link PersistentType}
	 * @param values
	 * @return
	 * @throws IllegalArgumentException if attributte has multi values
	 */
	Object toSinglePersistentValue(final List<AbstractFormValue<FormableEntity>> values);
	
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
