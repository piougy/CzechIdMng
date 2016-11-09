package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.springframework.data.history.AnnotationRevisionMetadata;
import org.springframework.data.history.Revision;
import org.springframework.data.history.RevisionMetadata;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.AuditService;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;

/**
 * Implementation of service for auditing
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Service
public class DefaultAuditService implements AuditService {
	
	@PersistenceContext
    private EntityManager entityManager;
	
	@Override
	@SuppressWarnings("unchecked")
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.AUDIT_READ + "')")
	public List<Revision<Integer, ? extends BaseEntity>> findRevisions(Class<?> classType, UUID entityId) throws RevisionDoesNotExistException {	
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
	public Revision<Integer, ? extends BaseEntity> findRevision(Class<?> classType, Integer revisionId, UUID entityId) throws RevisionDoesNotExistException  {
		AuditReader reader = getAuditReader();
		
		DefaultRevisionEntity revision = (DefaultRevisionEntity) reader.findRevision(classType, revisionId);

		Object entity = reader.find(classType, entityId, revisionId);
		return new Revision<>((RevisionMetadata<Integer>) getRevisionMetadata(revision), (BaseEntity) entity);
	}
	
	private RevisionMetadata<?> getRevisionMetadata(Object object) {
		return new AnnotationRevisionMetadata<>(object, RevisionNumber.class, RevisionTimestamp.class);
	}

	private AuditReader getAuditReader() {
		return AuditReaderFactory.get(entityManager);
	}
}
