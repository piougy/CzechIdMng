package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.rest.lookup.CodeableDtoLookup;
import eu.bcvsolutions.idm.core.api.rest.lookup.CodeableServiceEntityLookup;
import eu.bcvsolutions.idm.core.api.rest.lookup.DefaultDtoLookup;
import eu.bcvsolutions.idm.core.api.rest.lookup.DefaultEntityLookup;
import eu.bcvsolutions.idm.core.api.rest.lookup.DtoLookup;
import eu.bcvsolutions.idm.core.api.rest.lookup.EntityLookup;
import eu.bcvsolutions.idm.core.api.service.CodeableService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.api.service.ReadEntityService;

/**
 * Provide entity services through whole application. 
 * Support for loading {@link BaseEntity} by identifier.
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultLookupService implements LookupService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultLookupService.class);
	private final PluginRegistry<EntityLookup<?>, Class<?>> entityLookups;
	private final PluginRegistry<DtoLookup<?>, Class<?>> dtoLookups;
	// loaded services cache
	private final Map<Class<? extends Identifiable>, Object> services = new HashMap<>();
	private final EntityManager entityManager;
	
	@Autowired
	public DefaultLookupService(
			EntityManager entityManager,
			List<? extends EntityLookup<?>> entityLookups,
			List<? extends DtoLookup<?>> dtoLookups,
			List<? extends ReadEntityService<?, ?>> entityServices,
			List<? extends ReadDtoService<?, ?>> dtoServices) {
		Assert.notNull(entityLookups, "Entity lookups are required");
		//
		entityServices.forEach(service -> {
			services.put(service.getEntityClass(), service);
		});
		dtoServices.forEach(service -> {
			services.put(service.getEntityClass(), service);
			services.put(service.getDtoClass(), service);
		});
		// TODO: init default lookups
		this.entityLookups = OrderAwarePluginRegistry.create(entityLookups);
		this.dtoLookups = OrderAwarePluginRegistry.create(dtoLookups);
		this.entityManager = entityManager;
	}
	
	@Override
	public BaseEntity lookupEntity(Class<? extends Identifiable> identifiableType, Serializable entityId) { // vracim entitu  - class muze by entita i dto
		EntityLookup<BaseEntity> lookup = getEntityLookup(identifiableType);
		if (lookup == null) {
			throw new IllegalArgumentException(String.format("Entity lookup for identifiable type [%s] is not supported", identifiableType));
		}
		return lookup.lookup(entityId);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public BaseDto lookupDto(Class<? extends Identifiable> identifiableType, Serializable entityId) { // vracim entitu  - class muze by entita i dto
		DtoLookup<BaseDto> lookup = getDtoLookup(identifiableType);
		if (lookup == null) {
			throw new IllegalArgumentException(String.format("Dto lookup for identifiable type [%s] is not supported", identifiableType));
		}
		return lookup.lookup(entityId);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <E extends BaseEntity> ReadEntityService<E, ?> getEntityService(Class<E> entityClass) {
		return (ReadEntityService<E, ?>) services.get(entityClass);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <E extends BaseEntity, S extends ReadEntityService<E, ?>> S getEntityService(Class<E> entityClass, Class<S> entityServiceClass) {
		return (S) services.get(entityClass);
	}
	
	@Override
	public ReadDtoService<?, ?> getDtoService(Class<? extends Identifiable> identifiableType) {
		Object service = services.get(identifiableType);
		if (service == null) {
			LOG.debug("ReadDtoService for identifiable type [{}] is not found", identifiableType);
		}
		//
		if (service instanceof ReadDtoService) {
			return (ReadDtoService<?, ?>) service;
		}
		LOG.debug("Service for identifiable type [{}] is not ReadDtoService, type [{}] ", identifiableType, service.getClass().getCanonicalName());
		return null;
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
	public <I extends BaseEntity> EntityLookup<I> getEntityLookup(Class<? extends Identifiable> identifiableType) {			
		Class<I> entityClass = (Class<I>) getEntityClass(identifiableType);
		if (entityClass == null) {
			LOG.debug("Service for identifiable type [{}] is not found, lookup not found", identifiableType);
			return null;
		}
		//
		EntityLookup<I> lookup = (EntityLookup<I>) entityLookups.getPluginFor(entityClass);
		if (lookup == null) {
			Object service = services.get(identifiableType);
			if ((service instanceof ReadEntityService) && (service instanceof CodeableService)) {
				return new CodeableServiceEntityLookup<I>((CodeableService)service);
			}
			return new DefaultEntityLookup<I>(entityManager, entityClass);
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
	
	@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
	private Class<? extends BaseEntity> getEntityClass(Class<? extends Identifiable> identifiableType) {
		Object service = services.get(identifiableType);
		if (service == null) {
			return null;
		}
		//
		if (service instanceof ReadDtoService) {
			return ((ReadDtoService) service).getEntityClass();
		}		
		return ((ReadEntityService) service).getEntityClass();
	}
}
