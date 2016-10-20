package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.support.EntityLookup;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.rest.lookup.DefaultEntityLookup;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.api.service.ReadEntityService;

/**
 * Support for loading {@link BaseEntity} by identifier
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultEntityLookupService implements EntityLookupService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultEntityLookupService.class);
	private PluginRegistry<EntityLookup<?>, Class<?>> entityLookups;
	private final PluginRegistry<ReadEntityService<?, ?>, Class<?>> entityServices;
	
	@Autowired
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DefaultEntityLookupService(
			List<? extends EntityLookup<?>> entityLookups,
			List<? extends ReadEntityService<?, ?>> entityServices) {
		Assert.notNull(entityLookups, "Entity lookups are required");
		Assert.notNull(entityServices, "entity services are required");
		//
		this.entityServices = OrderAwarePluginRegistry.create(entityServices);
		//
		List<EntityLookup> entityLookupsWithDefault = new ArrayList<>(entityLookups);
		this.entityServices.getPlugins().forEach(entityService -> {
			if(entityService instanceof ReadEntityService) {
				// register default lookup for given entity class to prevent
				entityLookupsWithDefault.add(new DefaultEntityLookup(entityService));
			}
		});		
		this.entityLookups = OrderAwarePluginRegistry.create(entityLookupsWithDefault);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <E extends BaseEntity> ReadEntityService<E, ?> getEntityService(Class<E> entityClass) {
		return (ReadEntityService<E, ?>)entityServices.getPluginFor(entityClass);
	}
	
	@SuppressWarnings("unchecked")
	public <E extends BaseEntity> EntityLookup<E> getEntityLookup(Class<E> entityClass) {
		EntityLookup<E> lookup = (EntityLookup<E>)entityLookups.getPluginFor(entityClass);
		if(lookup == null) {
			ReadEntityService<E, ?> service = getEntityService(entityClass);
			if(service != null) {
				// register default lookup for given entity class to prevent
				return new DefaultEntityLookup<E>(service);
			}
		}
		return lookup;
		
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E extends BaseEntity> E lookup(Class<E> entityClass, Serializable entityId) {
		EntityLookup<E> lookup = getEntityLookup(entityClass);
		if (lookup == null) {
			log.warn("Lookup for type [{}] does not found. Entity class is not loadable", entityClass);
			return null;
		}
		return (E)lookup.lookupEntity(entityId);
	}	
}
