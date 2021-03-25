package eu.bcvsolutions.idm.core.model.service.impl;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.SingularAttribute;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.rest.lookup.CodeableDtoLookup;
import eu.bcvsolutions.idm.core.api.rest.lookup.DefaultDtoLookup;
import eu.bcvsolutions.idm.core.api.rest.lookup.DefaultEntityLookup;
import eu.bcvsolutions.idm.core.api.rest.lookup.DtoLookup;
import eu.bcvsolutions.idm.core.api.rest.lookup.DtoLookupByExample;
import eu.bcvsolutions.idm.core.api.rest.lookup.EntityLookup;
import eu.bcvsolutions.idm.core.api.service.CodeableService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;

/**
 * Provide entity services through whole application. 
 * Support for loading {@link BaseDto} and {@link BaseEntity} by identifier.
 * 
 * @author Radek Tomiška
 */
public class DefaultLookupService implements LookupService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultLookupService.class);
	private final ApplicationContext context;
	private final EntityManager entityManager;
	private final PluginRegistry<EntityLookup<?>, Class<?>> entityLookups;
	private final PluginRegistry<DtoLookup<?>, Class<?>> dtoLookups;
	private final PluginRegistry<DtoLookupByExample<?>, Class<?>> dtoLookupByExamples;
	// loaded services cache
	private final Map<Class<? extends Identifiable>, Object> services = new HashMap<>();
	
	@Autowired
	public DefaultLookupService(
			ApplicationContext context,
			EntityManager entityManager,
			List<? extends EntityLookup<?>> entityLookups,
			List<? extends DtoLookup<?>> dtoLookups,
			List<? extends DtoLookupByExample<?>> dtoLookupByExamples) {
		Assert.notNull(context, "Context is required.");
		Assert.notNull(entityManager, "Manager is required.");
		Assert.notNull(entityLookups, "Entity lookups are required");
		Assert.notNull(dtoLookups, "Dto lookups are required");
		Assert.notNull(dtoLookupByExamples, "Dto lookups by example are required");
		//
		this.context = context;
		this.entityManager = entityManager;
		this.entityLookups = OrderAwarePluginRegistry.create(entityLookups);
		this.dtoLookups = OrderAwarePluginRegistry.create(dtoLookups);
		this.dtoLookupByExamples = OrderAwarePluginRegistry.create(dtoLookupByExamples);
	}
	
	@Override
	public <E extends BaseEntity> E lookupEntity(Class<? extends Identifiable> identifiableType, Serializable entityId) {
		EntityLookup<E> lookup = getEntityLookup(identifiableType);
		if (lookup == null) {
			throw new IllegalArgumentException(String.format("Entity lookup for identifiable type [%s] is not supported", identifiableType));
		}
		E entity = lookup.lookup(entityId);
		//
		LOG.trace("Identifiable type [{}] with identifier [{}] found [{}]", identifiableType, entityId, entity != null);
		//
		return entity;
		
	}
	
	@Override
	public <DTO extends BaseDto> DTO lookupDto(Class<? extends Identifiable> identifiableType, Serializable entityId) {
		DtoLookup<DTO> lookup = getDtoLookup(identifiableType);
		if (lookup == null) {
			throw new IllegalArgumentException(String.format("Dto lookup for identifiable type [%s] is not supported", identifiableType));
		}
		DTO dto = lookup.lookup(entityId);
		//
		LOG.trace("Identifiable type [{}] with identifier [{}] found [{}]", identifiableType, entityId, dto != null);
		//
		return dto;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <DTO extends BaseDto> DTO lookupDto(String identifiableType, Serializable entityId) {
		try {
			return lookupDto((Class<? extends Identifiable>) Class.forName(identifiableType), entityId);
		} catch (ClassNotFoundException ex) {
			throw new IllegalArgumentException(String.format("Dto lookup for identifiable type [%s] is not supported", identifiableType), ex);
		}
	}
	
	@Override
	public <DTO extends BaseDto> DTO lookupEmbeddedDto(AbstractDto dto, SingularAttribute<?, ?> attribute) {
		Assert.notNull(attribute, "Singular attribute is required to get DTO from embedded.");
		//
		return lookupEmbeddedDto(dto, attribute.getName());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <DTO extends BaseDto> DTO lookupEmbeddedDto(AbstractDto dto, String attributeName) {
		Assert.notNull(dto, "DTO is required.");
		Assert.notNull(dto.getEmbedded(), "DTO does not have embedded DTO map initialized and is required.");
		Assert.hasLength(attributeName, "Singular attribute is required to get embedded DTO.");
		// 
		// from embedded
		DTO embeddedDto = DtoUtils.getEmbedded(dto, attributeName, (DTO) null);
		if (embeddedDto != null) {
			return embeddedDto;
		}
		//
		// try to load by lookup
		try {
			// get target DTO type by embedded annotation - annotation is required
			Field embeddableField = EntityUtils.getFirstFieldInClassHierarchy(dto.getClass(), attributeName);
			if (!embeddableField.isAnnotationPresent(Embedded.class)) {
				throw new IllegalArgumentException(String.format("Dto lookup for dto type [%s] attribute [%s] is not supported. Embedded annotion is missing.",
						dto.getClass(), attributeName));
			}
			Embedded embedded = embeddableField.getAnnotation(Embedded.class);
			//
			// get DTO identifier ~ field value
			PropertyDescriptor fieldDescriptor = EntityUtils.getFieldDescriptor(dto, attributeName);
			Object fieldValue = fieldDescriptor.getReadMethod().invoke(dto);
			if (!(fieldValue instanceof Serializable)) {
				throw new IllegalArgumentException(String.format("Dto lookup for dto type [%s] attribute [%s] is not supported.",
						dto.getClass(), attributeName));
			}
			//
			return (DTO) lookupDto(embedded.dtoClass(), (Serializable) fieldValue);
		} catch (ReflectiveOperationException | IntrospectionException ex) {
			throw new IllegalArgumentException(String.format("Dto lookup for dto type [%s] attribute [%s] is not supported.",
					dto.getClass(), attributeName), ex);
		}
	}
	
	@Override
	public <DTO extends BaseDto> DTO lookupByExample(DTO example) {
		Assert.notNull(example, "Example is required for lookup.");
		//
		Class<? extends BaseDto> identifiableType = example.getClass();
		DtoLookupByExample<DTO> lookup = getDtoLookupByExample(identifiableType);
		if (lookup == null) {
			LOG.debug("Lookup by example is not supported for type [{}].");
			return null;
		}
		DTO dto = lookup.lookup(example);
		//
		if (dto != null) {
			LOG.trace("Identifiable type [{}] with identifier [{}] found by example.", identifiableType, dto.getId());
		} else {
			LOG.trace("Identifiable type [{}] with identifier not found by example.", identifiableType);
		}
		//
		return dto;
	}
	
	@Override
	public ReadDtoService<?, ?> getDtoService(Class<? extends Identifiable> identifiableType) {
		Object service = getService(identifiableType);
		if (service == null) {
			LOG.debug("ReadDtoService for identifiable type [{}] is not found", identifiableType);
			return null;
		}
		//
		if (service instanceof ReadDtoService) {
			return (ReadDtoService<?, ?>) service;
		}
		LOG.debug("Service for identifiable type [{}] is not ReadDtoService, current type [{}] ", identifiableType, service.getClass().getCanonicalName());
		return null;
	}
	
	@Override
	@SuppressWarnings({ "unchecked" })
	public <E extends BaseEntity> EntityLookup<E> getEntityLookup(Class<? extends Identifiable> identifiableType) {			
		Class<E> entityClass = (Class<E>) getEntityClass(identifiableType);
		if (entityClass == null) {
			LOG.debug("Service for identifiable type [{}] is not found, lookup not found", identifiableType);
			return null;
		}
		//
		EntityLookup<E> lookup = (EntityLookup<E>) entityLookups.getPluginFor(entityClass);
		if (lookup == null) {
			return new DefaultEntityLookup<E>(entityManager, entityClass, getDtoLookup(identifiableType));
		}
		return lookup;	
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <I extends BaseDto> DtoLookup<I> getDtoLookup(Class<? extends Identifiable> identifiableType) {			
		ReadDtoService service = getDtoService(identifiableType);
		if (service == null) {
			LOG.debug("Service for identifiable type [{}] is not found, lookup not found.", identifiableType);
			return null;
		}
		//
		DtoLookup<I> lookup = (DtoLookup<I>) dtoLookups.getPluginFor(service.getDtoClass());
		if (lookup == null) {
			if (service instanceof CodeableService) {
				return new CodeableDtoLookup<I>((CodeableService<I>) service);
			}
			return new DefaultDtoLookup<I>(service);
		}
		return lookup;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <I extends BaseDto> DtoLookupByExample<I> getDtoLookupByExample(Class<? extends Identifiable> identifiableType) {			
		ReadDtoService service = getDtoService(identifiableType);
		if (service == null) {
			LOG.debug("Service for identifiable type [{}] is not found, lookup not found.", identifiableType);
			return null;
		}
		//
		DtoLookupByExample<I> lookup = (DtoLookupByExample<I>) dtoLookupByExamples.getPluginFor(service.getDtoClass());
		if (lookup == null) {
			// TODO: default lookup by reflection and filter properties?
		}
		return lookup;
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes"})
	public Class<? extends BaseEntity> getEntityClass(Class<? extends Identifiable> identifiableType) {
		Assert.notNull(identifiableType, "Identifiable type is required!");
		//
		// given identifiable type is already entity class
		if (BaseEntity.class.isAssignableFrom(identifiableType)) {
			return (Class<? extends BaseEntity>) identifiableType;
		}
		//
		// try to find entity class by dto class
		Object service = getService(identifiableType);
		if (service == null) {
			return null;
		}
		return ((ReadDtoService) service).getEntityClass();
	}
	
	@Override
	public UUID getOwnerId(Identifiable owner) {
		Assert.notNull(owner, "Owner is required.");
		if (owner.getId() == null) {
			return null;
		}		
		Assert.isInstanceOf(UUID.class, owner.getId(), "Entity with UUID identifier is supported as owner for some related entity.");
		//
		return (UUID) owner.getId();
	}
	
	@Override
	public String getOwnerType(Identifiable owner) {
		Assert.notNull(owner, "Owner is required.");
		//
		return getOwnerType(owner.getClass());
	}
	
	@Override
	public String getOwnerType(Class<? extends Identifiable> ownerType) {
		Assert.notNull(ownerType, "Owner type is required.");
		//
		// dto class was given
		Class<? extends BaseEntity> ownerEntityType = getOwnerClass(ownerType);
		if (ownerEntityType == null) {
			throw new IllegalArgumentException(String.format("Owner type [%s] has to generalize [BaseEntity]", ownerType));
		}
		return ownerEntityType.getCanonicalName();
	}
	
	/**
	 * Returns service for given {@link Identifiable} type.
	 * 
	 * @param identifiableType
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	private Object getService(Class<? extends Identifiable> identifiableType) {
		if (!services.containsKey(identifiableType)) {
			context.getBeansOfType(ReadDtoService.class).values().forEach(s -> {
				services.put(s.getEntityClass(), s);
				services.put(s.getDtoClass(), s);
			});
		}
		return services.get(identifiableType);
	}
	
	/**
	 * Returns {@link BaseEntity} class. Owner type has to be entity class - dto class can be given.
	 * 
	 * @param ownerType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Class<? extends BaseEntity> getOwnerClass(Class<? extends Identifiable> ownerType) {
		Assert.notNull(ownerType, "Owner type is required!");
		// formable entity class was given
		if (BaseEntity.class.isAssignableFrom(ownerType)) {
			return (Class<? extends BaseEntity>) ownerType;
		}
		// dto class was given
		Class<?> ownerEntityType = getEntityClass(ownerType);
		if (ownerEntityType == null) {
			return null;
		}
		if (BaseEntity.class.isAssignableFrom(ownerEntityType)) {
			return (Class<? extends BaseEntity>) ownerEntityType;
		}
		return null;
	}
}
