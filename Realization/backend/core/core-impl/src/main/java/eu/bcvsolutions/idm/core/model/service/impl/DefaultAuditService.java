package eu.bcvsolutions.idm.core.model.service.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.Audited;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.history.AnnotationRevisionMetadata;
import org.springframework.data.history.Revision;
import org.springframework.data.history.RevisionMetadata;
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
	
	@Override
	@SuppressWarnings("unchecked")
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.AUDIT_READ + "')")
	public List<Revision<Integer, ? extends BaseEntity>> findRevisions(Class<?> classType, Long entityId) throws RevisionDoesNotExistException {	
		List<Revision<Integer, ? extends BaseEntity>> result = new ArrayList<>();
		AuditReader reader = AuditReaderFactory.get(entityManager);
		
		// reader.createQuery().forRevisionsOfEntity(c, selectEntitiesOnly, selectDeletedEntities)
		
		List<Number> ids = reader.getRevisions(classType, entityId);
		
		Map<Number, DefaultRevisionEntity> revisionsResult = (Map<Number, DefaultRevisionEntity>) reader.findRevisions(classType, new HashSet<>(ids));

		for (Number revisionId : revisionsResult.keySet()) {
			result.add(this.findRevision(classType, (Integer)revisionId, entityId));
		}
		// TODO: refactor to own class / or use query above
		Collections.sort(result, (Revision<Integer, ? extends BaseEntity> o1, Revision<Integer, ? extends BaseEntity> o2) -> o2.compareTo(o1));		
		return result;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.AUDIT_READ + "')")
	public Revision<Integer, ? extends BaseEntity> findRevision(Class<?> classType, Integer revisionId, Long entityId) throws RevisionDoesNotExistException  {
		AuditReader reader = getAuditReader();
		
		DefaultRevisionEntity revision = (DefaultRevisionEntity) reader.findRevision(classType, revisionId);

		Object entity = reader.find(classType, entityId, revisionId);
		return new Revision<>((RevisionMetadata<Integer>) getRevisionMetadata(revision), (BaseEntity) entity);
	}
	
	private RevisionMetadata<?> getRevisionMetadata(Object object) {
		return new AnnotationRevisionMetadata<>(object, RevisionNumber.class, RevisionTimestamp.class);
	}
	
	@Override
	public AuditReader getAuditReader() {
		return AuditReaderFactory.get(entityManager);
	}

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
}
