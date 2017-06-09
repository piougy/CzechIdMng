package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmAuditDto;
import eu.bcvsolutions.idm.core.api.dto.filter.AuditFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.entity.IdmAudit;

/**
 * Interface for Audit service
 * 
 * Methods with word:
 * 
 * - revision - return object type of {@link IdmAudit}
 * - version - return object from audit tables
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * 
 */

public interface IdmAuditService extends ReadWriteDtoService<IdmAuditDto, AuditFilter> {
	
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
	<T> List<IdmAuditDto> findRevisions(Class<T> classType, UUID entityId);
	
	/**
	 * Return previous version of entity. Return Entity object not {@link IdmAuditDto}.
	 * 
	 * @param entity
	 * @param currentRevId
	 * @return
	 */
	<T> T findPreviousVersion(T entity, Long currentRevId);
	
	/**
	 * Method find revision by id get class and return previous version of this entity
	 * 
	 * @param currentRevId
	 * @return
	 */
	Object findPreviousVersion(Long currentRevId);
	
	/**
	 * TODO:
	 * 
	 * @param entityClass
	 * @param entityId
	 * @param currentRevId
	 * @return
	 */
	<T> T findPreviousVersion(Class<T> entityClass, UUID entityId, Long currentRevId);
	
	/**
	 * Method return version of entity for currentRevId
	 * 
	 * @param entityClass
	 * @param entityId
	 * @param currentRevId
	 * @return
	 */
	<T> T findVersion(Class<T> entityClass, UUID entityId, Long currentRevId);
	
	/**
	 * Return last revision number id.
	 * NOTE: this method works with commited transaction, not commited entity will be not found!!
	 * Otherwise use getCurrentRevision from envers with persist parameter to found entity in transaction
	 * 
	 * @param entityClass
	 * @param entityId
	 * @return
	 */
	<T> Number findLastRevisionNumber(Class<T> entityClass, UUID entityId);
	
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
	Page<IdmAuditDto> findRevisionsForEntity(String clazz, UUID entityId, Pageable pageable);
	
	/**
	 * Method get two revision and compare their values. The difference is first from second. 
	 * If @param secondRevision is null, difference will be from a previous revision.
	 * 
	 * @param clazz
	 * @param firstRev
	 * @return map with only changed values
	 */
	Map<String, Object> getDiffBetweenVersion(String clazz, Long firstRevId, Long secondRevId);
	
	/**
	 * Method get two revision and compare their values. Method call {@link getDiffBetweenRevision}
	 * with @param clazz null. The difference is first from second. 
	 * If @param secondRevision is null, difference will be from a previous revision.
	 * 
	 * @param clazz
	 * @param firstRev
	 * @return map with only changed values
	 */
	Map<String, Object> getDiffBetweenVersion(Long firstRevId, Long secondRevId);
	
	/**
	 * Method return all values from @param revisionObject.
	 * 
	 * @param revisionObject
	 * @param auditedClass
	 * @return map key is name of attribute.
	 */
	Map<String, Object> getValuesFromVersion(Object revisionObject, List<String> auditedClass);
	
	/**
	 * Method return all values from @param revisionObject.
	 * 
	 * @param revisionObject
	 * @return map key is name of attribute.
	 */
	Map<String, Object> getValuesFromVersion(Object revisionObject);
	
	/**
	 * Method return previous revision for entity with current revision
	 * 
	 * @param revision
	 * @return
	 */
	IdmAuditDto findPreviousRevision(IdmAuditDto revision);
	
	/**
	 * Method user {@link getPreviousRevision} and {@link get} for geting current revision
	 * @param revisionId
	 * @return
	 */
	IdmAuditDto findPreviousRevision(Long revisionId);
	
	Page<IdmAuditDto> findEntityWithRelation(Class<? extends AbstractEntity> clazz, MultiValueMap<String, Object> parameters, Pageable pageable);
}
