package eu.bcvsolutions.idm.core.api.service;

import java.util.List;
import java.util.UUID;

import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.springframework.data.history.Revision;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Interface for Audit service
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * TODO: Pageable method to find all revisions
 */

public interface AuditService {
	
	/**
	 * Method for find all revisions by class type and id identity.
	 * Method return list of {@link BaseEntity} not list of Revison. For compare entity with revision
	 * use {@link #findRevision(Class, Long, Long)}
	 * @param classType
	 * @param entityId
	 * @return
	 * @throws RevisionDoesNotExistException when no revision found
	 */
	List<Revision<Integer, ? extends BaseEntity>> findRevisions(Class<?> classType, UUID entityId) throws RevisionDoesNotExistException;
	
	/**
	 * Method find one revision by class type of entity, id revision and id identity.
	 * Id of revision may be found by method {@link #findRevisions(Class, Long)}
	 * @param classType
	 * @param revisionId
	 * @param entityId
	 * @return
	 * @throws RevisionDoesNotExistException when no revision found
	 */
	Revision<Integer, ? extends BaseEntity> findRevision(Class<?> classType, Integer revisionId, UUID entityId) throws RevisionDoesNotExistException;
}
