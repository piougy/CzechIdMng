package eu.bcvsolutions.idm.core.audit.service.impl;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.audit.dto.IdmAuditDto;
import eu.bcvsolutions.idm.core.api.audit.dto.filter.IdmAuditFilter;
import eu.bcvsolutions.idm.core.api.audit.service.IdmAuditService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.audit.entity.IdmAudit;
import eu.bcvsolutions.idm.core.audit.entity.service.AbstractAuditEntityService;
import eu.bcvsolutions.idm.core.audit.repository.IdmAuditRepository;
import eu.bcvsolutions.idm.core.model.repository.listener.IdmAuditListener;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Implementation of service for auditing
 * 
 * @see {@link IdmAuditListener}
 * @see {@link IdmAudit}
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Service
public class DefaultAuditService extends AbstractReadWriteDtoService<IdmAuditDto, IdmAudit, IdmAuditFilter> implements IdmAuditService {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultAuditService.class);
	
	@PersistenceContext
	private EntityManager entityManager;
	
	private final IdmAuditRepository auditRepository;
	
	@LazyCollection(LazyCollectionOption.TRUE)
	private List<String> allAuditedEntititesNames;
	
	private final PluginRegistry<AbstractAuditEntityService, Class<? extends AbstractEntity>> pluginExecutors; 
	
	@Autowired
	public DefaultAuditService(IdmAuditRepository auditRepository,
			List<AbstractAuditEntityService> evaluators) {
		super(auditRepository);
		//
		Assert.notNull(auditRepository);
		Assert.notNull(evaluators);
		//
		this.auditRepository = auditRepository;
		this.pluginExecutors = OrderAwarePluginRegistry.create(evaluators);
	}
	
	@Override
	protected Page<IdmAudit> findEntities(IdmAuditFilter filter, Pageable pageable, BasePermission... permission) {
		if (filter == null) {
			return getRepository().findAll(pageable);
		}
		return auditRepository.find(filter, pageable);
	}
	
	@Override
	public <T> T findRevision(Class<T> classType, UUID entityId, Long revisionNumber) throws RevisionDoesNotExistException  {
		return this.find(classType, entityId, revisionNumber);
	}
	
	private AuditReader getAuditReader() {
		return AuditReaderFactory.get(entityManager);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T findPreviousVersion(T entity, Long currentRevId) {
		IdmAuditDto previousRevision = this.findPreviousRevision(currentRevId);

	    if (previousRevision != null) {
	        return (T) this.find(entity.getClass(), (UUID)((BaseEntity) entity).getId(), Long.valueOf(previousRevision.getId().toString()));
	    } else {
	        return null;
	    }
	}
	
	@Override
	public <T> List<IdmAuditDto> findRevisions(Class<T> classType, UUID entityId) {
		IdmAuditFilter filter = new IdmAuditFilter();
		filter.setEntityId(entityId);
		filter.setType(classType.getName());
		Pageable page = new PageRequest(0, Integer.MAX_VALUE, new Sort("timestamp"));
		Page<IdmAuditDto> result = this.find(filter, page);
		return result.getContent();
	}
	
	@Override
	public <T> T findPreviousVersion(Class<T> entityClass, UUID entityId, Long currentRevisionId) {
		IdmAuditDto previousRevision = this.findPreviousRevision(currentRevisionId);

	    if (previousRevision != null) {
	        return this.find(entityClass, entityId, Long.valueOf(previousRevision.getId().toString()));
	    }
	    return null;
	}
	
	@Override
	public Object findPreviousVersion(Long currentRevisionId) {
		IdmAuditDto revision = this.get(currentRevisionId);
		
		Object result = null;

		IdmAuditDto previousRevision;
		try {
			previousRevision = this.findPreviousRevision(currentRevisionId);
			
			if (previousRevision != null) {
				result = this.find(Class.forName(previousRevision.getType()), previousRevision.getEntityId(), Long.valueOf(previousRevision.getId().toString()));
		    }
		} catch (ClassNotFoundException e) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("class", revision.getType()), e);
		}
		return result;
	}
	
	@Override
	public <T> T findVersion(Class<T> entityClass, UUID entityId, Long currentRevId) {
		return this.find(entityClass, entityId, currentRevId);
	}

	@Override
	public <T> List<String> getNameChangedColumns(Class<T> entityClass, UUID entityId, Long currentRevId,
			T currentEntity) {
		List<String> changedColumns = new ArrayList<>();
		
		T previousEntity = null;
		
		if (currentRevId == null) {
			IdmAudit currentRevision = this.getAuditReader().getCurrentRevision(IdmAudit.class, true);
			// current revision doesn't exist return empty list
			if (currentRevision == null) {
				return Collections.emptyList();
			}
			currentRevId = Long.valueOf(currentRevision.getId().toString());
		}
		previousEntity = this.findPreviousVersion(entityClass, entityId, currentRevId);
		
		// previous revision doesn't exist return empty list
		if (previousEntity == null) {
			return Collections.emptyList();
		}
		
		Class<?> clazz = entityClass;
		while (!(clazz.equals(AbstractEntity.class))) {
			Field[] fields = clazz.getDeclaredFields();
			
			for (Field field : fields) {
				if (field.getAnnotation(Audited.class) != null) {
					Object previousValue;
					Object currentValue;
					try {
						PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(currentEntity, field.getName());
						Assert.notNull(propertyDescriptor, MessageFormat.format("DefaultAuditService: read method for audited field {0}, can't be null.", field.getName()));
						//
						Method readMethod = propertyDescriptor.getReadMethod();
						// check if exists readMethod
						Assert.notNull(readMethod, MessageFormat.format("DefaultAuditService: read method for audited field {0}, can't be null.", field.getName()));
						
						previousValue = readMethod.invoke(previousEntity);
						currentValue = readMethod.invoke(currentEntity);
						
						if (previousValue == null && currentValue == null) {
							continue;
						}
						
						if (previousValue == null || !previousValue.equals(currentValue)) {
							changedColumns.add(field.getName());
						}
					} catch (IllegalArgumentException | IllegalAccessException | 
							NoSuchMethodException | InvocationTargetException ex) {
						throw new IllegalArgumentException(
								MessageFormat.format("For entity class [{0}] with id [{1}] and revision id [{2}], name of changed columns cannot be found.",
										clazz, entityId, currentRevId), ex);
					} catch (EntityNotFoundException e) {
						// TODO: Try to found better solution for get entity that was not found
						LOG.info("Audit service entity not found. Method [getNameChangedColumns]", e);
						break;
					}
				}
			}
			clazz = clazz.getSuperclass();
		}

		return changedColumns;
	}

	@Override
	public Page<IdmAuditDto> findRevisionsForEntity(String entityClass, UUID entityId, Pageable pageable) {
		IdmAuditFilter filter = new IdmAuditFilter();
		filter.setType(entityClass);
		filter.setEntityId(entityId);
		return this.find(filter, pageable);
	}

	@Override
	public List<String> getAllAuditedEntitiesNames() {
		// load from 'cache'
		// TODO: disable or enable modules?
		if (this.allAuditedEntititesNames != null) {
			// return this.allAuditedEntititesNames;
		}
		//
		List<String> result = new ArrayList<>();
		Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();
		for (EntityType<?> entityType : entities) {
			if (entityType.getJavaType() == null) {
				continue;
			}
			// get entities methods and search annotation Audited in fields.
			if (getAuditedField(entityType.getJavaType().getDeclaredFields())) {
				result.add(entityType.getJavaType().getCanonicalName());
				continue;
			}
			//
			// TODO: add some better get of all class annotations
			Annotation[] annotations = null;
			try {
				annotations = entityType.getJavaType().newInstance().getClass().getAnnotations();
			} catch (InstantiationException | IllegalAccessException e) {
				// class is not accessible
				continue;
			}
			// entity can be annotated for all class
			if (getAuditedAnnotation(annotations)) {
				result.add(entityType.getJavaType().getCanonicalName());
				continue;
			}
		}
		// sort entities by name
		Collections.sort(result);
		//
		this.allAuditedEntititesNames = result;
		return result;
	}

	@Override
	public <T> Number findLastRevisionNumber(Class<T> entityClass, UUID entityId) {
		return (Number) this.getAuditReader().createQuery()
			    .forRevisionsOfEntity(entityClass, false, true)
			    .addProjection(AuditEntity.revisionNumber().max())
			    .add(AuditEntity.id().eq(entityId))
			    .getSingleResult();
	}
	
	/**
	 * Is necessary to override method get, because old get transform id to UUID and audits have Long ID.
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public IdmAuditDto get(Serializable id, BasePermission... permission) {
		// TODO: add permission, now can't be use find, because Authentication object is null when call from IdmAuditLisener
		Assert.notNull(id, "Id is required");
		IdmAudit audit = this.auditRepository.findOneById(Long.valueOf(id.toString()));

		if (audit == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("audit", id));
		}
		// return only one element
		return this.toDto(audit);
	}
	
	@Override
	public Map<String, Object> getDiffBetweenVersion(String clazz, Long firstRevId, Long secondRevId) {
		Map<String, Object> result = new HashMap<>();
		IdmAuditDto firstRevision = this.get(firstRevId);
		// first revision must exist
		if (firstRevision == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("audit", firstRevId));
		}
		
		if (clazz == null) {
			clazz = firstRevision.getType();
		}
		IdmAuditDto secondRevision = null;
		
		if (secondRevId == null) {
			try {
				secondRevision = (IdmAuditDto)this.findPreviousVersion(Class.forName(clazz), firstRevision.getEntityId(), Long.valueOf(firstRevision.getId().toString()));
			} catch (NumberFormatException e) {
				throw new ResultCodeException(CoreResultCode.BAD_VALUE, ImmutableMap.of("audit", firstRevision.getId()), e);
			} catch (ClassNotFoundException e) {
				throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("audit class", clazz), e);
			}
		} else {
			secondRevision = this.get(secondRevId);
		}
		// check if we have second revision
		if (secondRevision == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("audit", secondRevId));
		}
		
		// check if revision are from same type (class, entity id)
		if (!firstRevision.getEntityId().equals(secondRevision.getEntityId())) {
			throw new ResultCodeException(CoreResultCode.AUDIT_REVISION_NOT_SAME, ImmutableMap.of("revision", clazz));
		}
		
		// now we will receive version from audit tables
		try {
			Object firstVersion = this.getRevision(clazz, firstRevision.getEntityId(), firstRevId);
			Object secondVersion = this.getRevision(clazz, secondRevision.getEntityId(), secondRevId);
			
			List<String> auditedClass = this.getAllAuditedEntitiesNames();
			
			Map<String, Object> firstValues = null;
			Map<String, Object> secondValues = null;
			
			// first revision is DEL, all attributes are null
			if (firstRevision.getModification().equals(RevisionType.DEL.name())) {
				firstValues = Collections.emptyMap();
			} else {
				firstValues = this.getValuesFromVersion(firstVersion, auditedClass);
			}
			
			if (secondRevision.getModification().equals(RevisionType.DEL.name())) {
				secondValues = Collections.emptyMap();
			} else {
				secondValues = this.getValuesFromVersion(secondVersion, auditedClass);
			}
			
			Set<Entry<String, Object>> entries = firstValues.entrySet();
			
			if (entries.isEmpty()) {
				entries = secondValues.entrySet();
			}
			
			for (Entry<String, Object> entry : entries) {
				if (!Objects.equals(entry.getValue(), secondValues.get(entry.getKey()))) {
					result.put(entry.getKey(), secondValues.get(entry.getKey()));
				}
			}
			
		} catch (ClassNotFoundException e) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("audit class", clazz), e);
		}
		
		return result;
	}
	
	public Map<String, Object> getDiffBetweenVersion(Long firstRevId, Long secondRevId) {
		return this.getDiffBetweenVersion(null, firstRevId, secondRevId);
	}
	
	@Override
	public Map<String, Object> getValuesFromVersion(Object revisionObject) {
		return this.getValuesFromVersion(revisionObject, this.getAllAuditedEntitiesNames());
	}
	
	@Override
	public Map<String, Object> getValuesFromVersion(Object revisionObject, List<String> auditedClass) {
		Map<String, Object> revisionValues = new HashMap<>();
		if (revisionObject == null) {
			return Collections.emptyMap();
		}
		
		// for better debug and readable there is no stream
		// we cannot use Introspector.getBeanInfo
		
		// getAllFieldsList get all field also with field from superclass
		List<Field> fields = FieldUtils.getAllFieldsList(revisionObject.getClass());
		
		for (Field field : fields) {
			try {
				
				// check if field has Audited annotation, or class
				if (!field.isAnnotationPresent(Audited.class) && !field.getDeclaringClass().isAnnotationPresent(Audited.class)) {
					continue;
				}
				//
				PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(revisionObject, field.getName());
				//
				// get property descriptor for readMethod
				if (propertyDescriptor == null) {
					continue;
				}
				//
				Method readMethod = propertyDescriptor.getReadMethod();
				Object value = readMethod.invoke(revisionObject);
				//
				// value can be null, but we want it
				if (value == null) {
					revisionValues.put(field.getName(), null);
					continue;
				}
				//
				LazyInitializer hibernateLI = null;
				String className = null;
				if (value instanceof HibernateProxy) {
                    HibernateProxy proxy = (HibernateProxy) value;
                    hibernateLI = proxy.getHibernateLazyInitializer();
                    className = hibernateLI.getEntityName();
                }
				// we have all audited class, then some not audited class (binding) and others primitive types
				if (className != null) {
					// get id from hibernate lazy initializer, entity may no longer exist, but ID in DB is always
					revisionValues.put(field.getName(), hibernateLI.getIdentifier());
				} else {
					revisionValues.put(field.getName(), value);
				}
				//
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new ResultCodeException(CoreResultCode.BAD_REQUEST, ImmutableMap.of("field", field.getName()), e);
			}
		}
		
		return revisionValues;
	}
	
	@Override
	public IdmAuditDto findPreviousRevision(IdmAuditDto revision) {
		Assert.notNull(revision, MessageFormat.format("DefaultAuditService: method getPreviousRevision - current revision [{0}] can't be null.", revision));
		
		if (revision.getEntityId() == null) {
			return null;
		}
		
		List<IdmAudit> results = this.auditRepository.getPreviousVersion(revision.getEntityId(), Long.valueOf(revision.getId().toString()), new PageRequest(0, 1)).getContent();
		if (!results.isEmpty() && results.size() == 1) {
			return  this.toDto(results.get(0));
		} else {
			return null;
		}
	}
	
	@Override
	public IdmAuditDto findPreviousRevision(Long revisionId) {
		Assert.notNull(revisionId, MessageFormat.format("DefaultAuditService: method getPreviousRevision - current revision id [{0}] can't be null.", revisionId));
		return this.findPreviousRevision(this.get(revisionId));
	}
	
	/**
	 * Method get version for @param revisionId
	 * 
	 * @param clazz
	 * @param entityId
	 * @param revisionId
	 * @return
	 * @throws ClassNotFoundException
	 */
	private Object getRevision(String clazz, UUID entityId, Long revisionId) throws ClassNotFoundException {
		return this.find(Class.forName(clazz), entityId, revisionId);
	}
	
	/**
	 * Method working with envers, find is realized in audited tables
	 * 
	 * @param entityClass
	 * @param entityId
	 * @param revisionId
	 * @return
	 */
	private <T> T find(Class<T> entityClass, UUID entityId, Long revisionId) {
		AuditReader reader = this.getAuditReader();

		return reader.find(entityClass, entityId, revisionId);
	}
	
	/**
	 * Method return true if at least one fields has annotation {@link Audited} 
	 * @param fields
	 * @return
	 */
	private boolean getAuditedField(Field[] fields) {
		if (fields == null) {
			return false;
		}
		//
		for (Field field : fields) {
			if (field.getAnnotation(Audited.class) != null) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Method return true if found {@link Audited} in annotations
	 * @param annotations
	 * @return
	 */
	private boolean getAuditedAnnotation(Annotation[] annotations) {
		if (annotations == null) {
			return false;
		}
		for (Annotation annotation : annotations) {
			if (annotation.annotationType().equals(Audited.class)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Page<IdmAuditDto> findEntityWithRelation(Class<? extends AbstractEntity> clazz, MultiValueMap<String, Object> parameters, Pageable pageable) {
		AbstractAuditEntityService service = pluginExecutors.getPluginFor(clazz);
		return this.toDtoPage(service.findRevisionBy(service.getFilter(parameters), pageable));
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public AbstractEntity getActualRemovedEntity(Class<AbstractEntity> entityClass, Object primaryKey) {
		return (AbstractEntity) entityManager.find(entityClass, primaryKey);
	}
}
