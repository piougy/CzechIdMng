package eu.bcvsolutions.idm.core.audit.entity.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.plugin.core.Plugin;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.audite.dto.filter.AuditEntityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmAudit;

/**
 * Abstract service for entities that is audited.
 * From implementation will be received entity with their relations.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 * @param <ENTITY>
 */
public abstract class AbstractAuditEntityService implements Plugin<Class<? extends AbstractEntity>> {

	@PersistenceContext
	private EntityManager entityManager;
	
	protected static final int ENTITY = 0;
	protected static final int REVISION_DATA = 1;
	protected static final int REVISION_TYPE = 2;
	
	public abstract List<Class<?>> getRelationship();
	
	protected AuditReader getAuditReader() {
		return AuditReaderFactory.get(entityManager);
	}
	
	public abstract List<IdmAudit> findRevisionBy(AuditEntityFilter filter);
	
	protected List<UUID> getEntityIdFromList(List<Object[]> entities) {
		List<UUID> ids = new ArrayList<>();
		for (Object[] entity : entities) {
			if (!ids.contains(getUUID(((AbstractEntity)entity[ENTITY]).getId()))) {
				ids.add(getUUID(((AbstractEntity)entity[ENTITY]).getId()));
			}
		}
		return ids;
	}
	
	protected List<Long> getRevisionId(List<Object[]> entities) {
		List<Long> revisionIds = new ArrayList<>();
		for (Object[] entity : entities) {
			Serializable id = ((IdmAudit)entity[REVISION_DATA]).getId();
			//
			if (id instanceof Long) {
				revisionIds.add(Long.valueOf(id.toString()));
			}
		}
		return revisionIds;
	}
	
	
	protected UUID getUUID (Serializable value) {
		if (value instanceof UUID) {
			return (UUID)value;
		}
		return null;
	}

	protected List<IdmAudit> getRevisionFromList(List<Object[]> entities) {
		List<IdmAudit> result = new ArrayList<>();
		for (Object[] entity : entities) {
			if (!result.contains(getUUID((IdmAudit)entity[REVISION_DATA]))) {
				result.add((IdmAudit)entity[REVISION_DATA]);
			}
		}
		return result;
	}
	
	public abstract AuditEntityFilter getFilter(MultiValueMap<String, Object> parameters);
}
