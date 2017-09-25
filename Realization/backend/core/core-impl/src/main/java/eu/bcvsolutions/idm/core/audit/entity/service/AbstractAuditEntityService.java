package eu.bcvsolutions.idm.core.audit.entity.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.plugin.core.Plugin;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.audit.dto.filter.AuditEntityFilter;
import eu.bcvsolutions.idm.core.audit.entity.IdmAudit;
import eu.bcvsolutions.idm.core.audit.repository.IdmAuditRepository;

/**
 * Abstract service for entities that is audited. From implementation will be
 * received entity with their relations.
 * 
 * TODO: search revision is realized by AuditReader (for now is load all version
 * from envers tables) Pageable isnt implemented!!
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public abstract class AbstractAuditEntityService implements Plugin<Class<? extends AbstractEntity>> {

	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private IdmAuditRepository auditRepository;

	protected static final int ENTITY = 0;
	protected static final int REVISION_DATA = 1;
	protected static final int REVISION_TYPE = 2;

	/**
	 * Return audit reader from entity manager
	 * 
	 * @return
	 */
	protected AuditReader getAuditReader() {
		return AuditReaderFactory.get(entityManager);
	}

	/**
	 * Return revision {@link IdmAudit} for filter
	 * 
	 * @param filter
	 * @return
	 */
	public abstract Page<IdmAudit> findRevisionBy(AuditEntityFilter filter, Pageable pageable);

	/**
	 * Return only entity id from list of revisions get by audit reader
	 * 
	 * @param entities
	 * @return
	 */
	protected List<UUID> getEntityIdFromList(List<Object[]> entities) {
		List<UUID> ids = new ArrayList<>();
		for (Object[] entity : entities) {
			UUID entityId = getUUID(((AbstractEntity) entity[ENTITY]).getId());
			if (!ids.contains(entityId)) {
				ids.add(entityId);
			}
		}
		return ids;
	}

	/**
	 * Return only revision id from list of revisions get by audit reader
	 * 
	 * @param entities
	 * @return
	 */
	protected List<Long> getRevisionId(List<Object[]> entities) {
		List<Long> revisionIds = new ArrayList<>();
		for (Object[] entity : entities) {
			Serializable id = ((IdmAudit) entity[REVISION_DATA]).getId();
			//
			if (id instanceof Long) {
				revisionIds.add(Long.valueOf(id.toString()));
			}
		}
		return revisionIds;
	}

	/**
	 * Get UUID from serializable value
	 * 
	 * @param value
	 * @return
	 */
	protected UUID getUUID(Serializable value) {
		if (value instanceof UUID) {
			return (UUID) value;
		}
		return null;
	}

	/**
	 * Return {@link IdmAudit} from objects get by audit reader
	 * 
	 * @param entities
	 * @return
	 */
	protected List<IdmAudit> getRevisionFromList(List<Object[]> entities) {
		List<IdmAudit> result = new ArrayList<>();
		for (Object[] entity : entities) {
			if (!result.contains((IdmAudit) entity[REVISION_DATA])) {
				result.add((IdmAudit) entity[REVISION_DATA]);
			}
		}
		return result;
	}

	/**
	 * Get specific implementation of filter for implementation this service
	 * 
	 * @param parameters
	 * @return
	 */
	public abstract AuditEntityFilter getFilter(MultiValueMap<String, Object> parameters);
	
	/**
	 * Return audit repository see {@link IdmAuditRepository}.
	 * @return
	 */
	protected IdmAuditRepository getAuditRepository() {
		return this.auditRepository;
	}
}
