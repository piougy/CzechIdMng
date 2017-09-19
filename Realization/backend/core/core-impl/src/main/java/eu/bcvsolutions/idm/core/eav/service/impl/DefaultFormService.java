package eu.bcvsolutions.idm.core.eav.service.impl;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.FormValueService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;

/**
 * Work with form definitions, attributes and their values
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultFormService implements FormService {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultFormService.class);
	private final IdmFormDefinitionService formDefinitionService;
	private final IdmFormAttributeService formAttributeService;
	private final PluginRegistry<FormValueService<?>, Class<?>> formValueServices;
	private final EntityEventManager entityEventManager;
	private final LookupService lookupService;
	
	@Autowired
	public DefaultFormService(
			IdmFormDefinitionService formDefinitionService,
			IdmFormAttributeService formAttributeService,
			List<? extends FormValueService<?>> formValueServices,
			EntityEventManager entityEventManager,
			LookupService lookupService) {
		Assert.notNull(formDefinitionService);
		Assert.notNull(formAttributeService);
		Assert.notNull(formValueServices);
		Assert.notNull(entityEventManager);
		Assert.notNull(lookupService);
		//
		this.formDefinitionService = formDefinitionService;
		this.formAttributeService = formAttributeService;
		this.formValueServices = OrderAwarePluginRegistry.create(formValueServices);
		this.entityEventManager = entityEventManager;
		this.lookupService = lookupService;
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmFormDefinitionDto getDefinition(String type) {
		return this.getDefinition(type, null);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmFormDefinitionDto> getDefinitions(String type) {
		return formDefinitionService.findAllByType(type);
	}

	@Override
	@Transactional(readOnly = true)
	public IdmFormDefinitionDto getDefinition(String type, String code) {
		if (StringUtils.isEmpty(code)) {
			formDefinitionService.findOneByMain(type);
		}
		return formDefinitionService.findOneByTypeAndCode(type, code);	
	}

	@Override
	@Transactional(readOnly = true)
	public IdmFormDefinitionDto getDefinition(Class<? extends Identifiable> ownerType) {
		return getDefinition(ownerType, null);		
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmFormDefinitionDto> getDefinitions(Class<? extends Identifiable> ownerType) {
		return getDefinitions(getDefaultDefinitionType(ownerType));	
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmFormDefinitionDto getDefinition(Class<? extends Identifiable> ownerType, String code) {
		Assert.notNull(ownerType, "Owner type is required!");
		//
		return getDefinition(getDefaultDefinitionType(ownerType), code);
	}
	
	/**
	 * Return default form definition, if no definition was given. Returns given definition otherwise.
	 * 
	 * @param ownerType
	 * @param formDefinitionId
	 * @return
	 * @throws IllegalArgumentException if default definition does not exist and no definition was given.
	 */
	private UUID checkDefaultDefinition(Class<? extends Identifiable> ownerType, UUID formDefinitionId) {
		if (formDefinitionId != null) {
			return formDefinitionId;
		}
		// values from default form definition
		return checkDefaultDefinition(ownerType, (IdmFormDefinitionDto) null).getId();
	}
	
	/**
	 * Return default form definition, if no definition was given. Returns given definition otherwise.
	 * 
	 * @param ownerType
	 * @param formDefinition
	 * @return
	 * @throws IllegalArgumentException if default definition does not exist and no definition was given.
	 */
	private IdmFormDefinitionDto checkDefaultDefinition(Class<? extends Identifiable> ownerType, IdmFormDefinitionDto formDefinition) {
		if (formDefinition == null) {
			// values from default form definition
			formDefinition = getDefinition(ownerType);
			Assert.notNull(formDefinition, MessageFormat.format("Default form definition for ownerType [{0}] not found and is required, fix application configuration!", ownerType));
		}
		return formDefinition;
	}

	@Override
	public String getDefaultDefinitionType(Class<? extends Identifiable> ownerType) {
		// dto class was given
		Class<? extends FormableEntity> ownerEntityType = getFormableOwnerType(ownerType);
		if (ownerEntityType == null) {
			throw new IllegalArgumentException(String.format("Owner type [%s] has to generatize [FormableEntity]", ownerType));
		}
		return ownerEntityType.getCanonicalName();
	}
	
	@Override
	public boolean isFormable(Class<? extends Identifiable> ownerType) {
		return getFormableOwnerType(ownerType) != null;
	}
	
	/**
	 * Returns {@link FormableEntity}
	 * 
	 * @param ownerType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Class<? extends FormableEntity> getFormableOwnerType(Class<? extends Identifiable> ownerType) {
		Assert.notNull(ownerType, "Owner type is required!");
		// formable entity class was given
		if (FormableEntity.class.isAssignableFrom(ownerType)) {
			return (Class<? extends FormableEntity>) ownerType;
		}
		// dto class was given
		Class<?> ownerEntityType = lookupService.getEntityClass(ownerType);
		if (FormableEntity.class.isAssignableFrom(ownerEntityType)) {
			return (Class<? extends FormableEntity>) ownerEntityType;
		}
		return null;
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmFormAttributeDto getAttribute(Class<? extends Identifiable> ownerType, String attributeCode) {
		Assert.notNull(ownerType, "Owner type is required!");
		Assert.hasLength(attributeCode, "Form attribute code is required!");
		//
		IdmFormDefinitionDto mainDefinition = getDefinition(ownerType);
		Assert.notNull(mainDefinition, "Main definition is required!");
		//
		return formAttributeService.findAttribute(getDefaultDefinitionType(ownerType), mainDefinition.getCode(), attributeCode);
	}
	
	@Override
	@Transactional
	public IdmFormDefinitionDto saveDefinition(IdmFormDefinitionDto formDefinition) {
		return formDefinitionService.save(formDefinition);
	}
	
	@Override
	@Transactional
	public IdmFormDefinitionDto createDefinition(String type, String code, List<IdmFormAttributeDto> formAttributes) {
		Assert.hasLength(type);
		//
		// create definition
		IdmFormDefinitionDto formDefinition = new IdmFormDefinitionDto();
		formDefinition.setType(type);	
		formDefinition.setCode(code);
		formDefinition = formDefinitionService.save(formDefinition);
		//
		// and their attributes
		if (formAttributes != null) {
			Short seq = 0;
			for (IdmFormAttributeDto formAttribute : formAttributes) {
				// default attribute order
				if (formAttribute.getSeq() == null) {
					formAttribute.setSeq(seq);
					seq++;
				}
				formAttribute.setFormDefinition(formDefinition.getId());
				formAttribute = formAttributeService.save(formAttribute);
				formDefinition.addFormAttribute(formAttribute);
			}
		}
		return formDefinition;
	}
	
	@Override
	@Transactional
	public IdmFormDefinitionDto createDefinition(Class<? extends Identifiable> ownerType, List<IdmFormAttributeDto> formAttributes) {
		Assert.notNull(ownerType, "Owner type is required!");
		//
		return createDefinition(getDefaultDefinitionType(ownerType), null, formAttributes);
	}

	@Override
	@Transactional
	public IdmFormDefinitionDto createDefinition(Class<? extends Identifiable> ownerType, String name,
			List<IdmFormAttributeDto> formAttributes) {
		Assert.notNull(ownerType, "Owner type is required!");
		//
		return createDefinition(getDefaultDefinitionType(ownerType), name, formAttributes);
	}
	
	@Override
	@Transactional
	public IdmFormAttributeDto saveAttribute(IdmFormAttributeDto attribute) {
		Assert.notNull(attribute);
		Assert.notNull(attribute.getFormDefinition(), String.format("Form definition for attribute [%s] is required!", attribute.getCode()));
		//
		return formAttributeService.save(attribute);
	}
	
	@Override
	@Transactional
	public IdmFormAttributeDto saveAttribute(Class<? extends Identifiable> ownerType, IdmFormAttributeDto attribute) {
		Assert.notNull(attribute);
		attribute.setFormDefinition(checkDefaultDefinition(ownerType, attribute.getFormDefinition()));
		//
		return saveAttribute(attribute);
	}
	
	@Override
	@Transactional
	public List<IdmFormValueDto> saveValues(
			Identifiable owner, IdmFormDefinitionDto formDefinition, List<IdmFormValueDto> values) {
		return saveFormInstance(owner, formDefinition, values).getValues();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * TODO: validations by given form definition? I don't think, it will not be useful in synchronization etc. - only FE validations will be enough ...
	 */
	@Override
	@Transactional
	public <O extends FormableEntity> IdmFormInstanceDto saveFormInstance(
			Identifiable owner, IdmFormDefinitionDto formDefinition, List<IdmFormValueDto> values) {
		O ownerEntity = getOwnerEntity(owner);
		Assert.notNull(values, "Form values are required!");
		Assert.notNull(ownerEntity, "Form values owner is required!");
		formDefinition = checkDefaultDefinition(ownerEntity.getClass(), formDefinition);
		//
		FormValueService<O> formValueService = getFormValueService(ownerEntity);
		//
		Map<UUID, IdmFormValueDto> previousValues = new HashMap<>();
	
		formValueService.getValues(ownerEntity, formDefinition).forEach(formValue -> {
			previousValues.put(formValue.getId(), formValue);
		});
		//
		List<IdmFormValueDto> results = new ArrayList<>();
		for (IdmFormValueDto value : values) {
			// value could contant attribute id only
			UUID attributeId = value.getFormAttribute();
			Assert.notNull(attributeId, "Form attribute is required");
			IdmFormAttributeDto attribute = formDefinition.getMappedAttribute(attributeId);
			Assert.notNull(attribute, "Form attribute is required");
			// 
			value.setOwnerAndAttribute(ownerEntity, attribute);
			//
			IdmFormValueDto previousValue = value.getId() == null ? null : previousValues.get(value.getId());
			if (previousValue != null) {
				// saved values will not be removed
				previousValues.remove(value.getId());
				// the same value should not be updated 
				// confidential value is always updated - only new values are sent from client
				if (value.isConfidential() || !value.isEquals(previousValue)) {
					// update value
					results.add(formValueService.save(value));
					LOG.trace("FormValue [{}:{}] for owner [{}] was updated", attribute.getCode(), value.getId(), ownerEntity);
				}
			} else {
				// create new value
				results.add(formValueService.save(value));
				LOG.trace("FormValue [{}:{}] for owner [{}] was created", attribute.getCode(), value.getId(), ownerEntity);
			}
		}
		//
		// remove unsaved values by attribute definition (patch method is not implemented now)
		previousValues.values().stream()
			.filter(formValue -> {
				// confidential values has to removed directly
				// they could not be sent with form (only changed values)
				return !formValue.isConfidential();
			})
			.forEach(value -> {
				formValueService.delete(value);
				LOG.trace("FormValue [{}:{}] for owner [{}] was deleted", value.getFormAttribute(), value.getId(), ownerEntity);
			});
		//
		// publish event - eav was saved
		if(lookupService.getDtoLookup(ownerEntity.getClass()) == null) {
			// TODO: remove this branch after all agends will be rewritten to dto usage
			entityEventManager.process(new CoreEvent<O>(CoreEventType.EAV_SAVE, ownerEntity));
		} else {
			entityEventManager.process(new CoreEvent<>(CoreEventType.EAV_SAVE, lookupService.lookupDto(ownerEntity.getClass(), ownerEntity.getId())));
		}
		//
		return new IdmFormInstanceDto(ownerEntity, formDefinition, results);
	}
	
	@Override
	@Transactional
	public List<IdmFormValueDto> saveValues(
			Identifiable owner, String attributeName, List<Serializable> persistentValues) {
		return saveValues(owner, null, attributeName, persistentValues);
	}
	
	@Override
	@Transactional
	public List<IdmFormValueDto> saveValues(
			Identifiable owner, IdmFormDefinitionDto formDefinition, String attributeCode, List<Serializable> persistentValues) {
		Assert.notNull(owner, "Form values owner is required!");
		Assert.notNull(owner.getId(), "Owner id is required!");
		Assert.hasLength(attributeCode, "Form attribute code is required!");
		formDefinition = checkDefaultDefinition(owner.getClass(), formDefinition);
		//
		return saveValues(owner, formDefinition.getMappedAttributeByCode(attributeCode), persistentValues);
	}
	
	@Override
	@Transactional
	public List<IdmFormValueDto> saveValues(
			UUID ownerId, Class<? extends Identifiable> ownerType, IdmFormAttributeDto attribute, List<Serializable> persistentValues) {
		return saveValues(getOwnerEntity(ownerId, ownerType), attribute, persistentValues);
	}
	
	@Override
	@Transactional
	public <O extends FormableEntity> List<IdmFormValueDto> saveValues(
			Identifiable owner, IdmFormAttributeDto attribute, List<Serializable> persistentValues) {
		Assert.notNull(owner, "Form values owner is required!");
		Assert.notNull(owner.getId(), "Owner id is required!");
		Assert.notNull(attribute, "Form attribute definition is required!");
		O ownerEntity = getOwnerEntity(owner);
		IdmFormDefinitionDto formDefinition = formDefinitionService.get(attribute.getFormDefinition());
		//
		if (persistentValues == null || persistentValues.isEmpty()) {
			// delete previous attributes
			deleteValues(ownerEntity, attribute);
			return null;
		}
		if (!attribute.isMultiple() && persistentValues.size() > 1) {
			throw new IllegalArgumentException(MessageFormat.format("Form attribute [{0}:{1}] does not support multivalue, sent [{2}] values.", 
					formDefinition.getCode(), attribute.getCode(), persistentValues.size()));
		}

		FormValueService<O> formValueService = getFormValueService(ownerEntity);
		
		// get old values
		List<IdmFormValueDto> values = formValueService.getValues(ownerEntity, attribute);
		
		// size isn't same drop and create
		if (values.size() != persistentValues.size()) {
			deleteValues(owner, attribute);
			// create
			List<IdmFormValueDto> results = new ArrayList<>();
			for (short seq = 0; seq < persistentValues.size(); seq++) {
				IdmFormValueDto value = new IdmFormValueDto();
				value.setOwnerAndAttribute(ownerEntity, attribute);
				//
				value.setValue(persistentValues.get(seq));
				value.setSeq(seq);
				results.add(formValueService.save(value));
			};
			//
			return results;
		}
		
		// compare values
		List<IdmFormValueDto> results = new ArrayList<>();
		for (IdmFormValueDto value : values) {
			IdmFormValueDto newValue = new IdmFormValueDto();
			newValue.setOwnerAndAttribute(ownerEntity, attribute);
			Serializable serializableValue = persistentValues.get(value.getSeq());
			newValue.setValue(serializableValue);
			
			if (!value.isEquals(newValue)) {
				value.setValue(serializableValue);
				results.add(formValueService.save(value));
			}
		}
		return results;
	}
	
	@Override
	@Transactional
	public List<IdmFormValueDto> saveValues(
			UUID ownerId, 
			Class<? extends Identifiable> ownerType, 
			IdmFormDefinitionDto formDefinition, 
			String attributeName,
			List<Serializable> persistentValues) {
		return saveValues(getOwnerEntity(ownerId, ownerType), formDefinition, attributeName, persistentValues);
	}
	
	@Override
	@Transactional
	public List<IdmFormValueDto> saveValues(UUID ownerId, Class<? extends Identifiable> ownerType, IdmFormDefinitionDto formDefinition, List<IdmFormValueDto> values) {
		return saveValues(getOwnerEntity(ownerId, ownerType), formDefinition, values);
	}
	
	@Override
	@Transactional
	public List<IdmFormValueDto> saveValues(
			UUID ownerId, Class<? extends Identifiable> ownerType, String attributeName, List<Serializable> persistentValues) {
		return saveValues(getOwnerEntity(ownerId, ownerType), attributeName, persistentValues);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmFormValueDto> getValues(Identifiable owner) {		
		return getValues(owner, (IdmFormDefinitionDto) null);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmFormValueDto> getValues(Identifiable owner, IdmFormDefinitionDto formDefinition) {
		return getFormInstance(owner, formDefinition).getValues();
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmFormInstanceDto getFormInstance(Identifiable owner) {		
		return getFormInstance(owner, (IdmFormDefinitionDto) null);
	}
	
	@Override
	@Transactional(readOnly = true)
	public <O extends FormableEntity> IdmFormInstanceDto getFormInstance(Identifiable owner, IdmFormDefinitionDto formDefinition) {
		Assert.notNull(owner, "Form values owner is required!");
		Assert.notNull(owner.getId(), "Owner id is required!");
		O ownerEntity = getOwnerEntity(owner);
		formDefinition = checkDefaultDefinition(owner.getClass(), formDefinition);
		//
		FormValueService<O> formValueService = getFormValueService(owner);
		//
		return new IdmFormInstanceDto(ownerEntity, formDefinition, formValueService.getValues(ownerEntity, formDefinition));
	}
	
	@Override
	@Transactional(readOnly = true)
	public <O extends FormableEntity> List<IdmFormValueDto> getValues(Identifiable owner, IdmFormAttributeDto attribute) {
		Assert.notNull(owner, "Form values owner is required!");
		Assert.notNull(owner.getId(), "Owner id is required!");
		Assert.notNull(attribute, "Form attribute definition is required!");
		//
		O ownerEntity = getOwnerEntity(owner);
		FormValueService<O> formValueService = getFormValueService(ownerEntity);
		return formValueService.getValues(ownerEntity, attribute);
	}
	
	@Override
	@Transactional(readOnly = true)
	public <O extends FormableEntity> List<IdmFormValueDto> getValues(Identifiable owner, String attributeCode) {
		return getValues(owner, null, attributeCode);
	}
	
	@Override
	@Transactional(readOnly = true)
	public <O extends FormableEntity> List<IdmFormValueDto> getValues(Identifiable owner, IdmFormDefinitionDto formDefinition, String attributeCode) {
		Assert.notNull(owner, "Form values owner is required!");
		Assert.notNull(owner.getId(), "Owner id is required!");
		Assert.hasLength(attributeCode, "Attribute code is required");
		formDefinition = checkDefaultDefinition(owner.getClass(), formDefinition);
		//
		return getValues(owner, formDefinition.getMappedAttributeByCode(attributeCode));
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmFormValueDto> getValues(UUID ownerId, Class<? extends Identifiable> ownerType) {
		return getValues(getOwnerEntity(ownerId, ownerType));
	}

	@Override
	@Transactional(readOnly = true)
	public List<IdmFormValueDto> getValues(UUID ownerId, Class<? extends Identifiable> ownerType,
			IdmFormDefinitionDto formDefinition) {
		return getValues(getOwnerEntity(ownerId, ownerType), formDefinition);
	}

	@Override
	@Transactional(readOnly = true)
	public List<IdmFormValueDto> getValues(UUID ownerId, Class<? extends Identifiable> ownerType,
			IdmFormDefinitionDto formDefinition, String attributeName) {
		return getValues(getOwnerEntity(ownerId, ownerType), formDefinition, attributeName);
	}

	@Override
	@Transactional(readOnly = true)
	public List<IdmFormValueDto> getValues(UUID ownerId, Class<? extends Identifiable> ownerType,
			IdmFormAttributeDto attribute) {
		return getValues(getOwnerEntity(ownerId, ownerType), attribute);
	}

	@Override
	@Transactional(readOnly = true)
	public List<IdmFormValueDto> getValues(UUID ownerId, Class<? extends Identifiable> ownerType, String attributeName) {
		return getValues(getOwnerEntity(ownerId, ownerType), attributeName);
	}
	
	@Override
	@Transactional
	public void deleteAttribute(IdmFormAttributeDto attribute) {
		formAttributeService.delete(attribute);
	}
	
	@Override
	@Transactional
	public void deleteValues(Identifiable owner) {
		deleteValues(owner, (IdmFormDefinitionDto) null);
	}
	
	@Override
	@Transactional
	public <O extends FormableEntity> void deleteValues(Identifiable owner, IdmFormDefinitionDto formDefinition) {
		Assert.notNull(owner, "Form values owner is required!");
		Assert.notNull(owner.getId(), "Owner id is required!");
		O ownerEntity = getOwnerEntity(owner);
		//
		FormValueService<O> formValueService = getFormValueService(ownerEntity);
		formValueService.deleteValues(ownerEntity, formDefinition);
	}
	
	@Override
	@Transactional
	public <O extends FormableEntity> void deleteValues(Identifiable owner, IdmFormAttributeDto attribute) {
		Assert.notNull(owner, "Form values owner is required!");
		Assert.notNull(owner.getId(), "Owner id is required!");
		Assert.notNull(attribute, "Form attribute definition is required!");
		//
		O ownerEntity = getOwnerEntity(owner);
		FormValueService<O> formValueService = getFormValueService(ownerEntity);
		formValueService.deleteValues(ownerEntity, attribute);
	}
	
	@Override
	public String getConfidentialStorageKey(Identifiable owner, IdmFormAttributeDto attribute) {
		Assert.notNull(owner, "Form values owner is required!");
		Assert.notNull(owner.getId(), "Owner id is required!");
		Assert.notNull(attribute, "Form attribute is required!");
		//
		FormValueService<FormableEntity> formValueService = getFormValueService(owner);
		String key = formValueService.getConfidentialStorageKey(attribute.getId());
		LOG.debug("Confidential storage key for attribute [{}] is [{}].", attribute.getCode(), key);
		return key;
	}
	
	@Override
	public Serializable getConfidentialPersistentValue(IdmFormValueDto guardedValue) {
		Assert.notNull(guardedValue);
		Assert.notNull(guardedValue.getOwnerId());
		//
		FormableEntity ownerEntity = getOwnerEntity(guardedValue.getOwnerId(), guardedValue.getOwnerType());
		FormValueService<?> formValueService = getFormValueService(ownerEntity);
		return formValueService.getConfidentialPersistentValue(guardedValue);
	}
	
	@Override
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public <O extends FormableEntity> Page<O> findOwners(Class<? extends Identifiable> ownerType, IdmFormAttributeDto attribute, Serializable persistentValue, Pageable pageable) {
		Assert.notNull(ownerType, "Owner type is required!");
		Assert.notNull(attribute, "Form attribute is required!");
		if (attribute.isConfidential()) {
			throw new UnsupportedOperationException(MessageFormat.format("Find owners by confidential attributes [{0}] are not supported.", attribute.getCode()));
		}
		//
		FormValueService<O> formValueService = (FormValueService<O>) formValueServices.getPluginFor(lookupService.getEntityClass(ownerType));
		//
		return formValueService.findOwners(attribute, persistentValue, pageable);
	}
	
	@Override
	@Transactional(readOnly = true)
	public <O extends FormableEntity> Page<O> findOwners(Class<? extends Identifiable> ownerType, String attributeName, Serializable persistentValue, Pageable pageable) {
		IdmFormAttributeDto attribute = getAttribute(ownerType, attributeName);
		Assert.notNull(attribute, MessageFormat.format("Attribute [{0}] does not exist in default form definition for owner [{1}]", attributeName, ownerType));
		//
		return findOwners(ownerType, attribute, persistentValue, pageable);
	}
	
	@Override
	public List<String> getOwnerTypes() {
		return formValueServices.getPlugins()
			.stream()
			.map(service -> {
				return service.getOwnerClass().getCanonicalName();
			})
			.sorted()
			.collect(Collectors.toList());
	}
	
	/**
	 * Returns FormValueService for given owner 
	 * 
	 * @param owner
	 * @param <O> values owner
	 * @param <E> values entity
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	private <O extends FormableEntity> FormValueService<O> getFormValueService(Identifiable owner) {
		O ownerEntity = getOwnerEntity(owner);
		FormValueService<O> formValueService = (FormValueService<O>) formValueServices.getPluginFor(ownerEntity.getClass());
		if (formValueService == null) {
			throw new IllegalStateException(MessageFormat.format("FormValueService for class [{0}] not found, please check configuration", ownerEntity.getClass()));
		}
		return formValueService;
	}
	
	/**
	 * 
	 * @param owner
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <O extends FormableEntity> O getOwnerEntity(Identifiable owner) {
		Assert.notNull(owner, "Form values owner instance is required!");
		if(owner instanceof FormableEntity) {
			return (O) owner;
		}
		//
		return getOwnerEntity(owner.getId(), owner.getClass());
	}
	
	/**
	 * Returns owner entity by given id and type
	 * 
	 * @param ownerId
	 * @param ownerType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <O extends FormableEntity> O getOwnerEntity(Serializable ownerId, Class<? extends Identifiable> ownerType) {
		Assert.notNull(ownerId, "Form values owner id is required!");
		Assert.notNull(ownerType, "Form values owner type is required!");
		O owner =  (O) lookupService.lookupEntity(ownerType, ownerId);
		Assert.notNull(owner, "Form values owner is required!");
		//
		return owner;
	}	
}
