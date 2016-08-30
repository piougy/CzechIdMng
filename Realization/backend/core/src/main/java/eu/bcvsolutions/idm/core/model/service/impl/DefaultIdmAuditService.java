package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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

import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.service.IdmAuditService;

/**
 * Implementation of service for auditing
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Service
public class DefaultIdmAuditService implements IdmAuditService {
	
	@PersistenceContext
    private EntityManager entityManager;
	
	@Override
	@SuppressWarnings("unchecked")
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.AUDIT_READ + "')")
	public Revision<Integer, ? extends AbstractEntity> findRevision(Class<?> classType, Integer idRev, Long identityId) throws RevisionDoesNotExistException  {
		AuditReader reader = getAuditReader();
		
		DefaultRevisionEntity revision = (DefaultRevisionEntity) reader.findRevision(classType, idRev);

		Object entity = reader.find(classType, identityId, idRev);
		return new Revision<Integer, AbstractEntity>((RevisionMetadata<Integer>) getRevisionMetadata(revision), (AbstractEntity) entity);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.AUDIT_READ + "')")
	public List<Revision<Integer, ? extends AbstractEntity>> findRevisions(Class<?> classType, Long identityId) throws RevisionDoesNotExistException {	
		List<Revision<Integer, ? extends AbstractEntity>> result = new ArrayList<>();
		AuditReader reader = AuditReaderFactory.get(entityManager);
		
		// reader.createQuery().forRevisionsOfEntity(c, selectEntitiesOnly, selectDeletedEntities)
		
		List<Number> ids = reader.getRevisions(classType, identityId);
		
		Map<Number, DefaultRevisionEntity> revisionsResult = (Map<Number, DefaultRevisionEntity>) reader.findRevisions(classType, new HashSet<Number>(ids));

		
		for (Number revisionId : revisionsResult.keySet()) {
			Revision<Integer, ? extends AbstractEntity> revision = this.findRevision(classType, (Integer)revisionId, identityId);
			result.add(revision);
		}
		return result;
	}
	
	private RevisionMetadata<?> getRevisionMetadata(Object object) {
		return new AnnotationRevisionMetadata<Integer>(object, RevisionNumber.class, RevisionTimestamp.class);
	}

	private AuditReader getAuditReader() {
		return AuditReaderFactory.get(entityManager);
	}
}
