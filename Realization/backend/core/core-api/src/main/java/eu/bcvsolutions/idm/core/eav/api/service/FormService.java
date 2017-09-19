package eu.bcvsolutions.idm.core.eav.api.service;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;

/**
 * Work with form definitions, attributes and their values => eav, extended attributes and their values to given owner ({@link FormableEntity}).
 * 
 * Confidential values are stored in confidential storage.
 * 
 * {@link AbstractEntity} type or {@link AbstractDto} can be used as owner type. 
 * Underlying {@link AbstractEntity} has to extend {@link FormableEntity}. 
 * If {@link AbstractDto} is given as owner type, then {@link FormableEntity} owner will be found by 
 * {@link LookupService} => transformation to {@link FormableEntity}. 
 * 
 * @see ConfidentialStorage
 * @see IdmFormDefinition}
 * @see IdmFormAttribute}
 * @see FormableEntity
 * @see LookupService
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
	 * Returns true, when given owner type support eav forms. If {@link AbstractDto} owner type is given, 
	 * then underlying {@link AbstractEntity} is resolved automaticaly => {@link AbstractEntity} has 
	 * to implement  {@link FormableEntity}.
	 * 
	 * @param ownerType
	 * @return
	 */
	boolean isFormable(Class<? extends Identifiable> ownerType);
	
	/**
	 * Finds default definition by given type
	 * 
	 * @param type
	 * @return
	 */
	IdmFormDefinitionDto getDefinition(String type);
	
	/**
	 * Returns all definitions for given type
	 * 
	 * @param type
	 * @return
	 */
	List<IdmFormDefinitionDto> getDefinitions(String type);
	
	/**
	 * Finds definition by given type and code (optional) 
	 * 
	 * @param type
	 * @param name [optional] - if no name given, then returns main definition
	 * @return
	 */
	IdmFormDefinitionDto getDefinition(String type, String code);
	
	/**
	 * Finds main definition by given owner
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param ownerType owner type
	 * @return
	 */
	IdmFormDefinitionDto getDefinition(Class<? extends Identifiable> ownerType);
	
	/**
	 * Returns all definitions for given type
	 * 
	 * @param ownerType
	 * @return
	 */
	List<IdmFormDefinitionDto> getDefinitions(Class<? extends Identifiable> ownerType);
	
	/**
	 * Finds definition by given type and code (optional) 
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param ownerType
	 * @param name [optional] - if no name given, then returns main definition
	 * @return
	 */
	IdmFormDefinitionDto getDefinition(Class<? extends Identifiable> ownerType, String code);
	
	/**
	 * Return default definition type for given owner type.
	 * 
	 * @param ownerType owner type
	 * @return
	 */
	String getDefaultDefinitionType(Class<? extends Identifiable> ownerType);
	
	/**
	 * Returns attribute by given name from main form definition
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param ownerType owner type
	 * @param attributeCode attribute code
	 * @return
	 */
	IdmFormAttributeDto getAttribute(Class<? extends Identifiable> ownerType, String attributeCode);
	
	/**
	 * Saves given form definition
	 * 
	 * @param formDefinition
	 * @return
	 */
	IdmFormDefinitionDto saveDefinition(IdmFormDefinitionDto formDefinition);
	
	/**
	 * Creates form definition
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param type definition type
	 * @param code [optional] definition code (main with default code will be created, if no definition code is given)
	 * @param formAttributes
	 * @return
	 */
	IdmFormDefinitionDto createDefinition(String type, String code, List<IdmFormAttributeDto> formAttributes);
	
	/**
	 * Creates main form definition for given owner type
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param ownerType owner type
	 * @param formAttributes
	 * @return
	 */
	IdmFormDefinitionDto createDefinition(Class<? extends Identifiable> ownerType, List<IdmFormAttributeDto> formAttributes);
	
	/**
	 * Creates form definition
	 * 
	 * @param ownerType owner type
	 * @param code [optional] definition code (main with default code will be created, if no definition code is given)
	 * @param formAttributes
	 * @return
	 */
	IdmFormDefinitionDto createDefinition(Class<? extends Identifiable> ownerType, String code, List<IdmFormAttributeDto> formAttributes);
	
	/**
	 * Persists given form attribute.
	 * 
	 * @param attribute
	 * @return
	 */
	IdmFormAttributeDto saveAttribute(IdmFormAttributeDto attribute);
	
	/**
	 * Persists given form attribute. If attribute does not have form definition specified, then main will be used.
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param ownerType owner type
	 * @param attribute
	 * @return
	 */
	IdmFormAttributeDto saveAttribute(Class<? extends Identifiable> ownerType, IdmFormAttributeDto attribute);
		
	/**
	 * Saves form values to given owner and form definition.
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param owner
	 * @param formDefinition [optional] if not specified, then main will be used.
	 * @param values
	 * @return persisted values
	 */
	List<IdmFormValueDto> saveValues(Identifiable owner, IdmFormDefinitionDto formDefinition, List<IdmFormValueDto> values);
	List<IdmFormValueDto> saveValues(UUID ownerId, Class<? extends Identifiable> ownerType, IdmFormDefinitionDto formDefinition, List<IdmFormValueDto> values);
	
	/**
	 * Saves form values to given owner and form definition.
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param owner
	 * @param formDefinition [optional] if not specified, then main will be used.
	 * @param values
	 * @return persisted values
	 */
	<O extends FormableEntity> IdmFormInstanceDto saveFormInstance(Identifiable owner, IdmFormDefinitionDto formDefinition, List<IdmFormValueDto> values);
	
	/**
	 * Saves form values to given owner and form attribute - saves attribute values only.
	 * 
	 * @param owner
	 * @param attribute
	 * @param persistentValue raw values
	 * @return persisted values
	 */
	<O extends FormableEntity> List<IdmFormValueDto> saveValues(Identifiable owner, IdmFormAttributeDto attribute, List<Serializable> persistentValues);
	List<IdmFormValueDto> saveValues(UUID ownerId, Class<? extends Identifiable> ownerType, IdmFormAttributeDto attribute, List<Serializable> persistentValues);
	
	/**
	 * Saves form values to given owner and form attribute - saves attribute values only.
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param owner
	 * @param formDefinition [optional] - main will be used, if no definition is given
	 * @param attributeName
	 * @param persistentValues
	 * @return persisted values
	 * @throws IllegalArgumentException if main definition does not exist
	 */
	List<IdmFormValueDto> saveValues(Identifiable owner, IdmFormDefinitionDto formDefinition, String attributeName, List<Serializable> persistentValues);
	List<IdmFormValueDto> saveValues(UUID ownerId, Class<? extends Identifiable> ownerType, IdmFormDefinitionDto formDefinition, String attributeName, List<Serializable> persistentValues);
	
	/**
	 * Saves form values to given owner and form attribute - saves attribute values only. Main form definition will be used.
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param owner
	 * @param attributeName
	 * @param persistentValues
	 * @return persisted values
	 * @throws IllegalArgumentException if main definition does not exist
	 */
	List<IdmFormValueDto> saveValues(Identifiable owner, String attributeName, List<Serializable> persistentValues);
	List<IdmFormValueDto> saveValues(UUID ownerId, Class<? extends Identifiable> ownerType, String attributeName, List<Serializable> persistentValues);
	
	/**
	 * Reads form values by given owner. Return values from main form definition.
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param owner
	 * @return
	 * @throws IllegalArgumentException if main definition does not exist
	 */
	List<IdmFormValueDto> getValues(Identifiable owner);
	
	/**
	 * Reads form values by given owner. Return values from main form definition.
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param ownerId
	 * @param ownerType
	 * @return
	 * @throws IllegalArgumentException if main definition does not exist
	 */
	List<IdmFormValueDto> getValues(UUID ownerId, Class<? extends Identifiable> ownerType);
	
	/**
	 * Reads form values by given owner. Return values from main form definition.
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param owner
	 * @return
	 * @throws IllegalArgumentException if main definition does not exist
	 */
	IdmFormInstanceDto getFormInstance(Identifiable owner);

	/**
	 * Reads form values by given owner and form definition
	 * 
	 * @param owner
	 * @param formDefinition [optional] if form definition is not given, then return attribute values from main definition
	 * @return
	 * @throws IllegalArgumentException if form definition is not given and main definition does not exist
	 */
	List<IdmFormValueDto> getValues(Identifiable owner, IdmFormDefinitionDto formDefinition);
	List<IdmFormValueDto> getValues(UUID ownerId, Class<? extends Identifiable> ownerType, IdmFormDefinitionDto formDefinition);
	
	/**
	 * Reads form values by given owner and form definition
	 * 
	 * @param owner
	 * @param formDefinition [optional] if form definition is not given, then return attribute values from main definition
	 * @return
	 * @throws IllegalArgumentException if form definition is not given and main definition does not exist
	 */
	<O extends FormableEntity> IdmFormInstanceDto getFormInstance(Identifiable owner, IdmFormDefinitionDto formDefinition);
	
	/**
	 * Returns attribute values by attributeName from given definition, or empty collection
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param owner
	 * @param formDefinition [optional] if form definition is not given, then return attribute values from main definition
	 * @param attributeCode
	 * @return
	 * @throws IllegalArgumentException if form definition is not given and main definition does not exist
	 */
	<O extends FormableEntity> List<IdmFormValueDto> getValues(Identifiable owner, IdmFormDefinitionDto formDefinition, String attributeCode);
	List<IdmFormValueDto> getValues(UUID ownerId, Class<? extends Identifiable> ownerType, IdmFormDefinitionDto formDefinition, String attributeCode);
	
	/**
	 * Returns attribute values by given attribute definition, or empty collection.
	 * 
	 * @param owner
	 * @param attribute (required)
	 * @return
	 * @throws IllegalArgumentException if attribute is not given
	 */
	<O extends FormableEntity> List<IdmFormValueDto> getValues(Identifiable owner, IdmFormAttributeDto attribute);
	List<IdmFormValueDto> getValues(UUID ownerId, Class<? extends Identifiable> ownerType, IdmFormAttributeDto attribute);
	
	/**
	 * Returns attribute values by attributeName from main definition, or empty collection
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)}
	 * @param owner
	 * @param attributeCode
	 * @return
	 * @throws IllegalArgumentException if main definition does not exist
	 */
	<O extends FormableEntity> List<IdmFormValueDto> getValues(Identifiable owner, String attributeCode);
	List<IdmFormValueDto> getValues(UUID ownerId, Class<? extends Identifiable> ownerType, String attributeCode);
	
	/**
	 * Deletes given attribute definition
	 * 
	 * @param attribute
	 */
	void deleteAttribute(IdmFormAttributeDto attribute);
	
	/**
	 * Deletes form values by given owner
	 * 
	 * @param owner values owner
	 * @param <O> values owner
	 * @return
	 */
	void deleteValues(Identifiable owner);
	
	/**
	 * Deletes form values by given owner and form definition
	 * 
	 * @param owner values owner
	 * @param formDefinition
	 * @param <O> values owner
	 * @return
	 */
	<O extends FormableEntity> void deleteValues(Identifiable owner, IdmFormDefinitionDto formDefinition);
	
	/**
	 * Deletes form values by given owner and form attribute
	 * 
	 * @param owner
	 * @param formAttribute
	 */
	<O extends FormableEntity> void deleteValues(Identifiable owner, IdmFormAttributeDto formAttribute);
	
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
	String getConfidentialStorageKey(Identifiable owner, IdmFormAttributeDto attribute);
	
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
	Serializable getConfidentialPersistentValue(IdmFormValueDto guardedValue);
	
	/**
	 * Finds owners by attribute value
	 * 
	 * Warning: returns {@link FormableEntity} not dtos.
	 * 
	 * @param ownerType owner type
	 * @param attribute attribute
	 * @param persistentValue attribute value
	 * @param pageable
	 * @param <O> values owner
	 * @return owners dto
	 */
	<O extends FormableEntity> Page<O> findOwners(Class<? extends Identifiable> ownerType, IdmFormAttributeDto attribute, Serializable persistentValue, Pageable pageable);
	
	/**
	 * Finds owners by attribute value from default form definition.
	 * 
	 * Warning: returns {@link FormableEntity} not dtos.
	 * 
	 * @see {@link #getDefaultDefinitionType(Class)
	 * @param ownerType owner type
	 * @param attributeName attribute
	 * @param persistentValue attribute value
	 * @param pageable
	 * @param <O> values owner
	 * @return owners dto
	 */
	<O extends FormableEntity> Page<O> findOwners(Class<? extends Identifiable> ownerType, String attributeName, Serializable persistentValue, Pageable pageable);
	
	/**
	 * Method return full class name of all entity that implements {@link FormableEntity}
	 * 
	 * @return
	 */
	List<String> getOwnerTypes();
}
