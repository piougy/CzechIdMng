package eu.bcvsolutions.idm.core.revision;

import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.bcvsolutions.idm.core.model.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.model.entity.AbstractEntity;

/**
 * Interface for implements revision to another controller.
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmRevisionController {
	
	/**
	 * Method find one revision defined by entityId (string) and revision number (Integer)
	 * Method will be implemented with 
	 * @RequestMapping(value = "{identityId}/revisions/{revId}", method = RequestMethod.GET)
	 * 
	 * @param identityId
	 * @param revId
	 * @return
	 */
	ResponseEntity<ResourceWrapper<DefaultRevisionEntity>> findRevision(@PathVariable("entityId") String entityId, @PathVariable("revId") Integer revId);
	
	/**
	 * Method find all revision for entityId (string).
	 * Method will be implemented with 
	 * @RequestMapping(value = "{entityId}/revisions", method = RequestMethod.GET)
	 * 
	 * @param entityId
	 * @return
	 */
	ResponseEntity<ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>>> findRevisions(@PathVariable("entityId") String entityId);
}
