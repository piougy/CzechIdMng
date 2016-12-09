package eu.bcvsolutions.idm.core.model.service.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.Audited;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.filter.AuditFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmAudit;
import eu.bcvsolutions.idm.core.model.repository.IdmAuditRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuditService;

/**
 * Implementation of service for auditing
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Service
public class DefaultAuditService extends AbstractReadWriteEntityService<IdmAudit, AuditFilter> implements IdmAuditService {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@LazyCollection(LazyCollectionOption.TRUE)
	private List<String> allAuditedEntititesNames;
	
	@Autowired
	public DefaultAuditService(IdmAuditRepository auditRepository) {
		super(auditRepository);
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
	public <T> T getPreviousVersion(T entity, Long currentRevId) {
		AuditReader reader = this.getAuditReader();

	    Number previousRevId = (Number) reader.createQuery()
	    .forRevisionsOfEntity(entity.getClass(), false, true)
	    .addProjection(AuditEntity.revisionNumber().max())
	    .add(AuditEntity.id().eq(((BaseEntity) entity).getId()))
	    .add(AuditEntity.revisionNumber().lt(currentRevId))
	    .getSingleResult();

	    if (previousRevId != null) {
	        return (T) this.find(entity.getClass(), (UUID)((BaseEntity) entity).getId(), previousRevId.longValue());
	    } else {
	        return null;
	    }
	}
	
	@Override
	public <T> List<IdmAudit> findRevisions(Class<T> classType, UUID entityId) {
		AuditFilter filter = new AuditFilter();
		filter.setEntityId(entityId);
		filter.setType(classType.getName());
		Page<IdmAudit> result = this.find(filter, null);
		return result.getContent();
	}
	
	@Override
	public <T> T getPreviousVersion(Class<T> entityClass, UUID entityId, Long currentRevisionId) {
		AuditReader reader = this.getAuditReader();

	    Number previousRevisionId = (Number) reader.createQuery()
		    .forRevisionsOfEntity(entityClass, false, true)
		    .addProjection(AuditEntity.revisionNumber().max())
		    .add(AuditEntity.id().eq(entityId))
		    .add(AuditEntity.revisionNumber().lt(currentRevisionId))
		    .getSingleResult();

	    if (previousRevisionId != null) {
	        return this.find(entityClass, entityId, previousRevisionId.longValue());
	    } else {
	    	return this.find(entityClass, entityId, currentRevisionId);
	    }
	}
	
	private <T> T find(Class<T> entityClass, UUID entityId, Long revisionId) {
		AuditReader reader = this.getAuditReader();
		return reader.find(entityClass, entityId, revisionId);
	}

	@Override
	public <T> List<String> getNameChangedColumns(Class<T> entityClass, UUID entityId, Long currentRevId,
			T currentEntity) {
		List<String> changedColumns = new ArrayList<>();
		
		T previousEntity = null;
		if (currentRevId == null) {
			currentRevId = this.getLastVersionNumber(entityClass, entityId).longValue();
		}
		previousEntity = this.getPreviousVersion(entityClass, entityId, currentRevId);
		
		if (previousEntity == null) {
			return changedColumns;
		}
		
		Field[] fields = entityClass.getDeclaredFields();
		
		for (Field field : fields) {
			if (field.getAnnotation(Audited.class) != null) {
				Object previousValue;
				Object currentValue;
				try {
					Method readMethod = PropertyUtils.getPropertyDescriptor(currentEntity, field.getName()).getReadMethod();
					
					// check if exists readMethod
					Assert.notNull(readMethod, "DefaultAuditService: read method for audited field " + field.getName() + ", can't be null.");
					
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
									entityClass, entityId, currentRevId), ex);
				}
			}
		}
		return changedColumns;
	}

	@Override
	public Page<IdmAudit> getRevisionsForEntity(String entityClass, UUID entityId, Pageable pageable) {
		AuditFilter filter = new AuditFilter();
		filter.setType(entityClass);
		filter.setEntityId(entityId);
		return this.find(filter, pageable);
	}

	@Override
	public List<String> getAllAuditedEntitiesNames() {
		// load from cache
		if (this.allAuditedEntititesNames != null) {
			return this.allAuditedEntititesNames;
		}
		
		List<String> result = new ArrayList<>();
		Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();
		for (EntityType<?> entityType : entities) {
			if (entityType.getJavaType() == null) {
				continue;
			}
			// get entities methods and search annotation Audited.
			for (Field field : entityType.getJavaType().getDeclaredFields()) {
				if (field.getAnnotation(Audited.class) != null) {
					result.add(entityType.getJavaType().getSimpleName());
					break;
				}
			}
		}
		this.allAuditedEntititesNames = result;
		return result;
	}

	@Override
	public <T> Number getLastVersionNumber(Class<T> entityClass, UUID entityId) {
		return (Number) this.getAuditReader().createQuery()
			    .forRevisionsOfEntity(entityClass, false, true)
			    .addProjection(AuditEntity.revisionNumber().max())
			    .add(AuditEntity.id().eq(entityId))
			    .getSingleResult();
	}
}
