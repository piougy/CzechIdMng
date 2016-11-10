package eu.bcvsolutions.idm.core.model.service.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.repository.BaseRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadEntityService;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.AuditFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmAudit;
import eu.bcvsolutions.idm.core.model.repository.IdmAuditRepository;
import eu.bcvsolutions.idm.core.model.service.IdmAuditService;

/**
 * Implementation of service for auditing
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Service
public class DefaultAuditService extends AbstractReadEntityService<IdmAudit, AuditFilter> implements IdmAuditService {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private IdmAuditRepository auditRepository;
	
	@LazyCollection(LazyCollectionOption.TRUE)
	private List<String> allAuditedEntititesNames;
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.AUDIT_READ + "')")
	public <T> T findRevision(Class<T> classType, Long revisionId, Long entityId) throws RevisionDoesNotExistException  {
		AuditReader reader = getAuditReader();
		return reader.find(classType, entityId, revisionId);
	}
	
	private AuditReader getAuditReader() {
		return AuditReaderFactory.get(entityManager);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getPreviousVersion(T entity, long currentRevId) {
		AuditReader reader = this.getAuditReader();

	    Number prior_revision = (Number) reader.createQuery()
	    .forRevisionsOfEntity(entity.getClass(), false, true)
	    .addProjection(AuditEntity.revisionNumber().max())
	    .add(AuditEntity.id().eq(((BaseEntity) entity).getId()))
	    .add(AuditEntity.revisionNumber().lt(currentRevId))
	    .getSingleResult();

	    if (prior_revision != null) {
	        return (T) reader.find(entity.getClass(), ((BaseEntity) entity).getId(), prior_revision);
	    } else {
	        return null;
	    }
	}

	@Override
	public <T> T getPreviousVersion(Class<T> entityClass, long entityId, long currentRevId) {
		AuditReader reader = this.getAuditReader();

	    Number prior_revision = (Number) reader.createQuery()
	    .forRevisionsOfEntity(entityClass, false, true)
	    .addProjection(AuditEntity.revisionNumber().max())
	    .add(AuditEntity.id().eq(entityId))
	    .add(AuditEntity.revisionNumber().lt(currentRevId))
	    .getSingleResult();

	    if (prior_revision != null) {
	        return (T) reader.find(entityClass, entityId, prior_revision);
	    } else {
	        return null;
	    }
	}

	@Override
	public <T> List<String> getNameChangedColumns(Class<T> entityClass, long entityId, long currentRevId,
			T currentEntity) {
		List<String> changedColumns = new ArrayList<>();
		T previousEntity = this.getPreviousVersion(entityClass, entityId, currentRevId);
		
		Field[] fields = entityClass.getDeclaredFields();
		
		for (Field field : fields) {
			if (field.getAnnotation(Audited.class) != null) {
				Object previousValue;
				Object currentValue;
				try {
					Method readMethod = PropertyUtils.getPropertyDescriptor(currentEntity, field.getName()).getReadMethod();
					
					previousValue = readMethod.invoke(previousEntity);
					currentValue = readMethod.invoke(currentEntity);
					
					if (previousValue == null && currentValue == null) {
						continue;
					}
					
					if (previousValue == null || !previousValue.equals(currentValue)) {
						changedColumns.add(field.getName());
					}
				} catch (IllegalArgumentException | IllegalAccessException | 
						NoSuchMethodException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		return changedColumns;
	}

	@Override
	protected BaseRepository<IdmAudit, AuditFilter> getRepository() {
		return this.auditRepository;
	}

	@Override
	public Page<IdmAudit> getRevisionsForEntity(String entityClass, long entityId, Pageable pageable) {
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
}
