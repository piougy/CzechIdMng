package eu.bcvsolutions.idm.core.model.service;

import java.util.List;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.springframework.data.history.Revision;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.ReadEntityService;
import eu.bcvsolutions.idm.core.model.dto.AuditFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmAudit;

/**
 * Interface for Audit service
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * TODO: Pageable method to find all revisions
 */

public interface IdmAuditService extends ReadEntityService<IdmAudit, AuditFilter> {
	
	/**
	 * Method for find all revisions by class type and id identity.
	 * Method return list of {@link BaseEntity} not list of Revison. For compare entity with revision
	 * use {@link #findRevision(Class, Long, Long)}
	 * @param classType
	 * @param entityId
	 * @return
	 * @throws RevisionDoesNotExistException when no revision found
	 */
	List<Revision<Integer, ? extends BaseEntity>> findRevisions(Class<?> classType, Long entityId) throws RevisionDoesNotExistException;
	
	/**
	 * Method find one revision by class type of entity, id revision and id identity.
	 * Id of revision may be found by method {@link #findRevisions(Class, Long)}
	 * @param classType
	 * @param revisionId
	 * @param entityId
	 * @return
	 * @throws RevisionDoesNotExistException when no revision found
	 */
	Revision<Integer, ? extends BaseEntity> findRevision(Class<?> classType, Integer revisionId, Long entityId) throws RevisionDoesNotExistException;	
	
	/**
	 * Method return AuditReader for entity manager. Used for create specific queries.
	 * 
	 * @return AuditReader
	 */
	AuditReader getAuditReader();
	
	/**
	 * TODO:
	 * 
	 * @param entity
	 * @param currentRevId
	 * @return
	 */
	<T> T getPreviousVersion(T entity, long currentRevId);
	
	/**
	 * TODO:
	 * 
	 * @param entityClass
	 * @param entityId
	 * @param currentRevId
	 * @return
	 */
	<T> T getPreviousVersion(Class<T> entityClass, long entityId, long currentRevId);
	
	/**
	 * Return names of changed columns with annotation @Audited. Diff is realized by previous revision and actual entity.
	 * 
	 * @param entityClass
	 * @param entityId
	 * @param currentRevId
	 * @param currentEntity
	 * @return
	 */
	<T> List<String> getNameChangedColumns(Class<T> entityClass, long entityId, long currentRevId, T currentEntity);	
}
