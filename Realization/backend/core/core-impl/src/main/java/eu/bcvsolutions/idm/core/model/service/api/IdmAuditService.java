package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;
import java.util.UUID;

import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.AuditFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmAudit;

/**
 * Interface for Audit service
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * TODO: Pageable method to find all revisions
 */

public interface IdmAuditService extends ReadWriteEntityService<IdmAudit, AuditFilter> {
	
	/**
	 * Method find one revision by class type of entity, id revision and id identity.
	 * Id of revision may be found by method {@link #findRevisions(Class, Long)}
	 * 
	 * @param <T>
	 * @param classType
	 * @param revisionId
	 * @param entityId
	 * @return
	 * @throws RevisionDoesNotExistException when no revision found
	 */
	<T> T findRevision(Class<T> classType, UUID entityId, Long revisionId) throws RevisionDoesNotExistException;	
	
	/**
	 * Method find all revisions for class and entity id
	 * 
	 * @param classType
	 * @param entityId
	 * @return
	 */
	<T> List<IdmAudit> findRevisions(Class<T> classType, UUID entityId);
	
	/**
	 * Return previous version of entity. Return Entity object not {@link IdmAudit}.
	 * 
	 * @param entity
	 * @param currentRevId
	 * @return
	 */
	<T> T getPreviousVersion(T entity, Long currentRevId);
	
	/**
	 * TODO:
	 * 
	 * @param entityClass
	 * @param entityId
	 * @param currentRevId
	 * @return
	 */
	<T> T getPreviousVersion(Class<T> entityClass, UUID entityId, Long currentRevId);
	
	/**
	 * Return last version number id.
	 * 
	 * @param entityClass
	 * @param entityId
	 * @return
	 */
	<T> Number getLastVersionNumber(Class<T> entityClass, UUID entityId);
	
	/**
	 * Return names of changed columns with annotation @Audited. Diff is realized by previous revision and actual entity.
	 * 
	 * @param entityClass
	 * @param entityId
	 * @param currentRevId
	 * @param currentEntity
	 * @return
	 */
	<T> List<String> getNameChangedColumns(Class<T> entityClass, UUID entityId, Long currentRevId, T currentEntity);
	
	/**
	 * Method return list of class simple name for which is audited. Must at least one attribute with
	 * annotation {@value Audited}
	 * 
	 * @return instance of list IdmSimpleEntityDto
	 */
	List<String> getAllAuditedEntitiesNames();
	
	/**
	 * Get all revision for entity class. Method use {@link AuditFilter} and method for quick
	 * search - {@link find}. Method set filter with enum {@link AuditClassMapping} and {@code entityId}. 
	 * When is entityId null, you will recive all entities for {@link AuditClassMapping}.
	 * 
	 * @param clazz
	 * @param entityId
	 * @param pageable
	 * @return
	 */
	Page<IdmAudit> getRevisionsForEntity(String clazz, UUID entityId, Pageable pageable);
}
