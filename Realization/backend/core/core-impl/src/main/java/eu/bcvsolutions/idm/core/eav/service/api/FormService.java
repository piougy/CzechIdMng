package eu.bcvsolutions.idm.core.eav.service.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.service.impl.AbstractFormValueService;

/**
 * Work with form definitions, attributes and their values
 * 
 * Confidential values are stored in confidential storage.
 * 
 * TODO: EAV entities dto and move to api
 * TODO: FormableEntity -> FormableDto ...
 * 
 * @see ConfidentialStorage
 * @see IdmFormDefinition}
 * @see IdmFormAttribute}
 * @see AbstractFormValue
 * @see FormableEntity
 * 
 * @author Radek Tomiška
 *
 */
public interface FormService extends ScriptEnabled {
	
	/**
	 * Default definition name for type (if no name is given)
	 */
	static final String DEFAULT_DEFINITION_CODE = IdmFormDefinitionService.DEFAULT_DEFINITION_CODE;
	
	/**
	 * Finds default definition by given type
	 * 
	 * @param type
	 * @return
	 */
	IdmFormDefinition getDefinition(String type);
	
	/**
	 * Returns all definitions for given type
	 * 
	 * @param type
	 * @return
	 */
	List<IdmFormDefinition> getDefinitions(String type);
	
	/**
	 * Finds definition by given type and code (optional) 
	 * 
	 * @param type
	 * @param name [optional] - if no name given, then returns main definition
	 * @return
	 */
	IdmFormDefinition getDefinition(String type, String code);
	
	/**
	 * Finds main definition by given owner
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param ownerClass owner type
	 * @return
	 */
	IdmFormDefinition getDefinition(Class<? extends FormableEntity> ownerClass);
	
	/**
	 * Returns all definitions for given type
	 * 
	 * @param ownerClass
	 * @return
	 */
	List<IdmFormDefinition> getDefinitions(Class<? extends FormableEntity> ownerClass);
	
	/**
	 * Finds definition by given type and code (optional) 
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param ownerClass
	 * @param name [optional] - if no name given, then returns main definition
	 * @return
	 */
	IdmFormDefinition getDefinition(Class<? extends FormableEntity> ownerClass, String code);
	
	/**
	 * Return default definition type for given owner type.
	 * 
	 * @param ownerClass owner type
	 * @return
	 */
	String getDefaultDefinitionType(Class<? extends FormableEntity> ownerClass);
	
	/**
	 * Returns attribute by given name from main form definition
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param ownerClass owner type
	 * @param attributeName attribute name
	 * @return
	 */
	IdmFormAttribute getAttribute(Class<? extends FormableEntity> ownerClass, String attributeName);
	
	/**
	 * Saves given form definition
	 * 
	 * @param formDefinition
	 * @return
	 */
	IdmFormDefinition saveDefinition(IdmFormDefinition formDefinition);
	
	/**
	 * Creates form definition
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param type definition type
	 * @param name [optional] definition name (main with default code will be created, if no definition code is given)
	 * @param formAttributes
	 * @return
	 */
	IdmFormDefinition createDefinition(String type, String code, List<IdmFormAttribute> formAttributes);
	
	/**
	 * Creates main form definition for given owner type
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param ownerClass owner type
	 * @param formAttributes
	 * @return
	 */
	IdmFormDefinition createDefinition(Class<? extends FormableEntity> ownerClass, List<IdmFormAttribute> formAttributes);
	
	/**
	 * Creates form definition
	 * 
	 * @param ownerClass owner type
	 * @param name [optional] definition code (main with default code will be created, if no definition code is given)
	 * @param formAttributes
	 * @return
	 */
	IdmFormDefinition createDefinition(Class<? extends FormableEntity> ownerClass, String code, List<IdmFormAttribute> formAttributes);
	
	/**
	 * Persists given form attribute.
	 * 
	 * @param attribute
	 * @return
	 */
	IdmFormAttribute saveAttribute(IdmFormAttribute attribute);
	
	/**
	 * Persists given form attribute. If attribute does not have form definition specified, then main will be used.
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param ownerClass owner type
	 * @param attribute
	 * @return
	 */
	IdmFormAttribute saveAttribute(Class<? extends FormableEntity> ownerClass, IdmFormAttribute attribute);
		
	/**
	 * Saves form values to given owner and form definition.
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param owner
	 * @param formDefinition [optional] if not specified, then main will be used.
	 * @param values
	 * @param <O> values owner
	 * @param <E> values entity
	 * @return persisted values
	 */
	<O extends FormableEntity, E extends AbstractFormValue<O>> List<E> saveValues(O owner, IdmFormDefinition formDefinition, List<E> values);
	<O extends FormableEntity, E extends AbstractFormValue<O>> List<E> saveValues(UUID owner, Class<O> ownerType, IdmFormDefinition formDefinition, List<E> values);
	
	/**
	 * Saves form values to given owner and form attribute - saves attribute values only.
	 * 
	 * @param owner
	 * @param attribute
	 * @param persistentValue raw values
	 * @param <O> values owner
	 * @param <E> value entity
	 * @return persisted values
	 */
	<O extends FormableEntity, E extends AbstractFormValue<O>> List<E> saveValues(O owner, IdmFormAttribute attribute, List<Serializable> persistentValues);
	<O extends FormableEntity, E extends AbstractFormValue<O>> List<E> saveValues(UUID owner, Class<O> ownerType, IdmFormAttribute attribute, List<Serializable> persistentValues);
	
	/**
	 * Saves form values to given owner and form attribute - saves attribute values only.
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param owner
	 * @param formDefinition [optional] - main will be used, if no definition is given
	 * @param attributeName
	 * @param persistentValues
	 * @return persisted values
	 * @param <O> values owner
	 * @param <E> value entity
	 * @throws IllegalArgumentException if main definition does not exist
	 */
	<O extends FormableEntity, E extends AbstractFormValue<O>> List<E> saveValues(O owner, IdmFormDefinition formDefinition, String attributeName, List<Serializable> persistentValues);
	<O extends FormableEntity, E extends AbstractFormValue<O>> List<E> saveValues(UUID ownerId, Class<O> ownerType, IdmFormDefinition formDefinition, String attributeName, List<Serializable> persistentValues);
	
	/**
	 * Saves form values to given owner and form attribute - saves attribute values only. Main form definition will be used.
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param owner
	 * @param attributeName
	 * @param persistentValues
	 * @return persisted values
	 * @param <O> values owner
	 * @param <E> value entity
	 * @throws IllegalArgumentException if main definition does not exist
	 */
	<O extends FormableEntity, E extends AbstractFormValue<O>> List<E> saveValues(O owner, String attributeName, List<Serializable> persistentValues);
	<O extends FormableEntity, E extends AbstractFormValue<O>> List<E> saveValues(UUID ownerId, Class<O> ownerType, String attributeName, List<Serializable> persistentValues);
	
	/**
	 * Reads form values by given owner. Return values from main form definition.
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param owner
	 * @param <O> values owner
	 * @return
	 * @throws IllegalArgumentException if main definition does not exist
	 */
	<O extends FormableEntity> List<AbstractFormValue<O>> getValues(O owner);
	<O extends FormableEntity> List<AbstractFormValue<O>> getValues(UUID ownerId, Class<O> ownerType);

	/**
	 * Reads form values by given owner and form definition
	 * 
	 * @param owner
	 * @param formDefinition [optional] if form definition is not given, then return attribute values from main definition
	 * @param <O> values owner
	 * @return
	 * @throws IllegalArgumentException if form definition is not given and main definition does not exist
	 */
	<O extends FormableEntity> List<AbstractFormValue<O>> getValues(O owner, IdmFormDefinition formDefinition);
	<O extends FormableEntity> List<AbstractFormValue<O>> getValues(UUID ownerId, Class<O> ownerType, IdmFormDefinition formDefinition);
	
	/**
	 * Returns attribute values by attributeName from given definition, or empty collection
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param owner
	 * @param formDefinition [optional] if form definition is not given, then return attribute values from main definition
	 * @param attributeName
	 * @param <O> values owner
	 * @return
	 * @throws IllegalArgumentException if form definition is not given and main definition does not exist
	 */
	<O extends FormableEntity> List<AbstractFormValue<O>> getValues(O owner, IdmFormDefinition formDefinition, String attributeName);
	<O extends FormableEntity> List<AbstractFormValue<O>> getValues(UUID ownerId, Class<O> ownerType, IdmFormDefinition formDefinition, String attributeName);
	
	/**
	 * Returns attribute values by given attribute definition, or empty collection.
	 * 
	 * @param owner
	 * @param attribute (required)
	 * @param <O> values owner
	 * @return
	 * @throws IllegalArgumentException if attribute is not given
	 */
	<O extends FormableEntity> List<AbstractFormValue<O>> getValues(O owner, IdmFormAttribute attribute);
	<O extends FormableEntity> List<AbstractFormValue<O>> getValues(UUID ownerId, Class<O> ownerType, IdmFormAttribute attribute);
	
	/**
	 * Returns attribute values by attributeName from main definition, or empty collection
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param owner
	 * @param attributeName
	 * @param <O> values owner
	 * @return
	 * @throws IllegalArgumentException if main definition does not exist
	 */
	<O extends FormableEntity> List<AbstractFormValue<O>> getValues(O owner, String attributeName);
	<O extends FormableEntity> List<AbstractFormValue<O>> getValues(UUID ownerId, Class<O> ownerType, String attributeName);
	
	/**
	 * Returns form values as map, key is attribute name
	 * 
	 * @param values
	 * @param <E> values owner
	 * @param <E> values entity
	 * @return
	 */
	<O extends FormableEntity, E extends AbstractFormValue<O>> Map<String, List<E>> toValueMap(final List<E> values);
	
	/**
	 * Returns FormValue values by persistent type as map, where key is attribute name
	 * 
	 * @see {@link PersistentType}
	 * @param values form values
	 * @return
	 */
	Map<String, List<Serializable>> toPersistentValueMap(final List<AbstractFormValue<FormableEntity>> values);
	
	/**
	 * Returns FormValue values by persistent type - usable for single multi attribute values
	 * 
	 * @see {@link PersistentType}
	 * @param values form values
	 * @return
	 */
	List<Serializable> toPersistentValues(final List<AbstractFormValue<FormableEntity>> values);
	
	/**
	 * Returns single FormValue by persistent type - usable for single attribute value
	 * 
	 * @see {@link PersistentType}
	 * @param values form values
	 * @return
	 * @throws IllegalArgumentException if attributte has multi values
	 */
	Serializable toSinglePersistentValue(final List<AbstractFormValue<FormableEntity>> values);
	
	/**
	 * Deletes given attribute definition
	 * 
	 * @param attribute
	 */
	void deleteAttribute(IdmFormAttribute attribute);
	
	/**
	 * Deletes form values by given owner
	 * 
	 * @param owner values owner
	 * @param <O> values owner
	 * @return
	 */
	<O extends FormableEntity> void deleteValues(O owner);
	
	/**
	 * Deletes form values by given owner and form definition
	 * 
	 * @param owner values owner
	 * @param formDefinition
	 * @param <O> values owner
	 * @return
	 */
	<O extends FormableEntity> void deleteValues(O owner, IdmFormDefinition formDefinition);
	
	/**
	 * Deletes form values by given owner and form attribute
	 * 
	 * @param owner
	 * @param formAttribute
	 */
	<O extends FormableEntity> void deleteValues(O owner, IdmFormAttribute formAttribute);
	
	/**
	 * Returns key in confidential storage for given extended attribute and owner
	 * 
	 * @see {@link ConfidentialStorage}
	 * @see {@link AbstractFormValueService#getConfidentialStorageKey(IdmFormAttribute)}
	 * 
	 * @param owner attribute owner
	 * @param attribute extended attribute
	 * @return key to confidential storage to given owner and attribute
	 */
	String getConfidentialStorageKey(FormableEntity owner, IdmFormAttribute attribute);
	
	/**
	 * Returns value in FormValue's persistent type from confidential storage
	 * 
	 * @see {@link #getConfidentialStorageKey(FormableEntity, IdmFormAttribute)}
	 * 
	 * @param guardedValue
	 * @param <O> values owner
	 * @param <E> value entity
	 * @return
	 */
	<O extends FormableEntity, E extends AbstractFormValue<O>> Serializable getConfidentialPersistentValue(E guardedValue);
	
	/**
	 * Finds owners by attribute value
	 * 
	 * @param ownerClass owner type
	 * @param attribute attribute
	 * @param persistentValue attribute value
	 * @param pageable
	 * @param <O> values owner
	 * @return
	 */
	<O extends FormableEntity> Page<O> findOwners(Class<O> ownerClass, IdmFormAttribute attribute, Serializable persistentValue, Pageable pageable);
	
	/**
	 * Finds owners by attribute value from default form definition.
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)
	 * @param ownerClass owner type
	 * @param attributeName attribute
	 * @param persistentValue attribute value
	 * @param pageable
	 * @param <O> values owner
	 * @return
	 */
	<O extends FormableEntity> Page<O> findOwners(Class<O> ownerClass, String attributeName, Serializable persistentValue, Pageable pageable);
	
	/**
	 * Method return full class name of all entity that implements {@link FormableEntity}
	 * 
	 * @return
	 */
	List<String> getOwnerTypes();
}
