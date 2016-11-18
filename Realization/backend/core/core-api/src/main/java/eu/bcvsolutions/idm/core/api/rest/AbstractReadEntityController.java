package eu.bcvsolutions.idm.core.api.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.core.support.EntityLookup;
import org.springframework.data.rest.webmvc.ControllerUtils;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.core.EmbeddedWrappers;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.api.service.ReadEntityService;

/**
 * Read operations (get, find)
 * 
 * @author Radek Tomiška
 *
 * @param <E>
 */
public abstract class AbstractReadEntityController<E extends BaseEntity, F extends BaseFilter> implements BaseEntityController<E> {
	
	private static final EmbeddedWrappers WRAPPERS = new EmbeddedWrappers(false);
	
	@Autowired
	private PagedResourcesAssembler<Object> pagedResourcesAssembler; // TODO: autowired in api package - move higher
	
	protected final EntityLookupService entityLookupService;
	
	private final ReadEntityService<E, F> entityService;
	
	@SuppressWarnings("unchecked")
	public AbstractReadEntityController(EntityLookupService entityLookupService) {
		this.entityLookupService = entityLookupService;
		//
		Class<E> entityClass = (Class<E>)GenericTypeResolver.resolveTypeArgument(getClass(), BaseEntityController.class);
		this.entityService = (ReadEntityService<E, F>)entityLookupService.getEntityService(entityClass);
	}
	
	public AbstractReadEntityController(EntityLookupService entityLookupService, ReadEntityService<E, F> entityService) {
		this.entityLookupService = entityLookupService;
		this.entityService = entityService;
	}
	
	/**
	 * Returns assembler to dto
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	protected ResourceAssemblerSupport<Object, ResourceWrapper> getAssembler() {
		return null;
	}
	
	/**
	 * Returns entity lookup for controller entity class. 
	 * 
	 * @return
	 */
	protected EntityLookup<E> getEntityLookup() {
		return entityLookupService.getEntityLookup(getEntityClass());
	}

	/**
	 * Returns entity service configured to current controller
	 * 
	 * @return
	 */
	protected ReadEntityService<E, F> getEntityService() {
		return entityService;
	}
	
	/**
	 * Returns controlled entity class
	 * 
	 * @return
	 */
	protected Class<E> getEntityClass() {
		return getEntityService().getEntityClass();
	}
	
	/**
	 * Returns response dto with entity by given backendId 
	 * 
	 * @param backendId
	 * @param assembler
	 * @return
	 */
	public ResponseEntity<?> get(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		E entity = getEntity(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}		
		return new ResponseEntity<>(toResource(entity, assembler), HttpStatus.OK);
	}
	
	/**
	 * Returns entity by given backendId
	 * 
	 * @param backendId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public E getEntity(String backendId) {
		// TODO: read events
		if(getEntityLookup() == null) {
			try {
				return getEntityService().get(backendId);
			} catch (IllegalArgumentException ex) {
				throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId), ex);
			}
		}		
		return (E) getEntityLookup().lookupEntity(backendId);
	}
	
	/**
	 * Returns entity identifier, if lookup is configured
	 * 
	 * @param entity
	 * @return
	 */
	public Serializable getEntityIdentifier(E entity) {
		if (getEntityLookup() == null) {
			return entity.getId();
		}
		return getEntityLookup().getResourceIdentifier(entity);
	}
	
	/**
	 * 
	 * @param parameters
	 * @param pageable
	 * @param assembler
	 * @return
	 */
	public Resources<?> find(
			@RequestParam MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable, 
			PersistentEntityResourceAssembler assembler) {
		return toResources(findEntities(toFilter(parameters), pageable), assembler, getEntityClass(), null);
	}
	
	public Page<E> findEntities(F filter, Pageable pageable) {
		// TODO: read event
		return getEntityService().find(filter, pageable);
	}	
	
	/**
	 * Converts entity to dto (using controller defined assembler or default)
	 * 
	 * @param entity
	 * @param assembler default PersistentEntityResourceAssembler assembler
	 * @return
	 */
	protected ResourceSupport toResource(E entity, PersistentEntityResourceAssembler assembler) {
		if(getAssembler() != null) {
			return getAssembler().toResource(entity);
		}
		return assembler.toFullResource(entity);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Resources<?> toResources(Iterable<?> source, PersistentEntityResourceAssembler assembler,
			Class<?> domainType, Link baseLink) {

		if (source instanceof Page) {
			Page<Object> page = (Page<Object>) source;
			return entitiesToResources(page, assembler, domainType, baseLink);
		} else if (source instanceof Iterable) {
			return entitiesToResources((Iterable<Object>) source, assembler, domainType);
		} else {
			return new Resources(ControllerUtils.EMPTY_RESOURCE_LIST);
		}
	}
	
	protected Resources<?> entitiesToResources(Page<Object> page, PersistentEntityResourceAssembler assembler,
			Class<?> domainType, Link baseLink) {

		if (page.getContent().isEmpty()) {
			return pagedResourcesAssembler.toEmptyResource(page, domainType, baseLink);
		}

		return baseLink == null ? pagedResourcesAssembler.toResource(page, assembler)
				: pagedResourcesAssembler.toResource(page, assembler, baseLink);
	}
	
	protected Resources<?> entitiesToResources(Iterable<Object> entities, PersistentEntityResourceAssembler assembler,
			Class<?> domainType) {

		if (!entities.iterator().hasNext()) {
			// empty collection
			List<Object> content = Arrays.<Object> asList(WRAPPERS.emptyCollectionOf(domainType));
			return new Resources<Object>(content, getDefaultSelfLink());
		}

		List<Resource<Object>> resources = new ArrayList<Resource<Object>>();

		for (Object obj : entities) {
			resources.add(obj == null ? null : assembler.toResource(obj));
		}

		return new Resources<Resource<Object>>(resources, getDefaultSelfLink());
	}
	
	protected Link getDefaultSelfLink() {
		return new Link(ServletUriComponentsBuilder.fromCurrentRequest().build().toUriString());
	}
	
	/**
	 * Transforms request parameters to {@link BaseFilter}.
	 * @param parameters
	 * @return
	 */
	protected F toFilter(MultiValueMap<String, Object> parameters) {
		return null;
	}
	
	protected String convertStringParameter(MultiValueMap<String, Object> parameters, String parameterName) {
		Assert.notNull(parameters);
	    Assert.notNull(parameterName);
	    //
		return (String)parameters.toSingleValueMap().get(parameterName);
	}
	
	protected Boolean convertBooleanParameter(MultiValueMap<String, Object> parameters, String parameterName) {
		String valueAsString = convertStringParameter(parameters, parameterName);
		if (StringUtils.isNotEmpty(valueAsString)) {
			return new Boolean(valueAsString);
		}
		return null;
	}
	
	/**
	 * Converts parameter to {@code Long} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	protected Long convertLongParameter(MultiValueMap<String, Object> parameters, String parameterName) {
		String valueAsString = convertStringParameter(parameters, parameterName);
		if(StringUtils.isNotEmpty(valueAsString)) {
			try {
				return Long.valueOf(valueAsString);
			} catch (NumberFormatException ex) {
				throw new ResultCodeException(CoreResultCode.BAD_VALUE, ImmutableMap.of(parameterName, valueAsString), ex);
			}		
		}
		return null;
	}
	
	/**
	 * Converts parameter to {@code UUID} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	protected UUID convertUuidParameter(MultiValueMap<String, Object> parameters, String parameterName) {
		String valueAsString = convertStringParameter(parameters, parameterName);
		if(StringUtils.isNotEmpty(valueAsString)) {
			try {
				return UUID.fromString(valueAsString);
			} catch (IllegalArgumentException ex) {
				throw new ResultCodeException(CoreResultCode.BAD_VALUE, ImmutableMap.of(parameterName, valueAsString), ex);
			}		
		}
		return null;
	}
	
	/**
	 * Converts parameter to given {@code enumClass} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @param enumClass
	 * @return
	 */
	protected <T extends Enum<T>> T convertEnumParameter(MultiValueMap<String, Object> parameters, String parameterName, Class<T> enumClass) {
		Assert.notNull(enumClass);
	    //
	    String valueAsString = convertStringParameter(parameters, parameterName);
	    if(StringUtils.isEmpty(valueAsString)) {
	    	return null;
	    }
        try {
            return Enum.valueOf(enumClass, valueAsString.trim().toUpperCase());
        } catch(IllegalArgumentException ex) {
        	throw new ResultCodeException(CoreResultCode.BAD_VALUE, ImmutableMap.of(parameterName, valueAsString), ex);
        }
	}
	
	/**
	 * Converts parameter to given {@code entityClass} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @param entityClass
	 * @return
	 */
	protected <T extends BaseEntity> T convertEntityParameter(MultiValueMap<String, Object> parameters, String parameterName, Class<T> entityClass) {
		 String valueAsString = convertStringParameter(parameters, parameterName);
	    if(StringUtils.isEmpty(valueAsString)) {
	    	return null;
	    }
		T entity = entityLookupService.lookup(entityClass, valueAsString);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.BAD_VALUE, "Entity type [%s] with identifier [%s] does not found", ImmutableMap.of("entityClass", entityClass.getSimpleName(), parameterName, valueAsString));
		}
		return entity;
	}
	
	/**
	 * Converts parameter to given {@code entityClass} from given parameters.
	 * 
	 * @param parameterValue
	 * @param parameterName
	 * @param entityClass
	 * @return
	 */
	protected <T extends BaseEntity> T convertEntityParameter(String parameterValue, Class<T> entityClass) {
	    if(StringUtils.isEmpty(parameterValue)) {
	    	return null;
	    }
		T entity = entityLookupService.lookup(entityClass, parameterValue);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.BAD_VALUE, "Entity type [%s] with identifier [%s] does not found", ImmutableMap.of("entityClass", entityClass.getSimpleName(), "identifier", parameterValue));
		}
		return entity;
	}
}
