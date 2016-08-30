package eu.bcvsolutions.idm.core.model.service;

import java.util.List;
import java.util.Map;

import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.springframework.data.history.Revision;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import eu.bcvsolutions.idm.core.model.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.model.entity.AbstractEntity;

/**
 * Interface for Audit service
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * TODO: Pageable method to find all revisions
 */

public interface IdmAuditService {
	
	/**
	 * Method find one revision by class type of entity, id revision and id identity.
	 * Id of revision may be found by method {@link #findRevisions(Class, Long)}
	 * @param classType
	 * @param idRev
	 * @param identityId
	 * @return
	 * @throws RevisionDoesNotExistException when no revision found
	 */
	Revision<Integer, ? extends AbstractEntity> findRevision(Class<?> classType, Integer idRev, Long identityId) throws RevisionDoesNotExistException;
	
	/**
	 * Method for find all revisions by class type and id identity.
	 * Method return list of {@link AbstractEntity} not list of Revison. For compare entity with revision
	 * use {@link #findRevision(Class, Long, Long)}
	 * @param classType
	 * @param identityId
	 * @return
	 * @throws RevisionDoesNotExistException when no revision found
	 */
	List<Revision<Integer, ? extends AbstractEntity>> findRevisions(Class<?> classType, Long identityId) throws RevisionDoesNotExistException;
}
