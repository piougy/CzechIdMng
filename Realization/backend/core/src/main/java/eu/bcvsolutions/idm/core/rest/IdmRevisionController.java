package eu.bcvsolutions.idm.core.rest;

import org.hibernate.envers.DefaultRevisionEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import eu.bcvsolutions.idm.core.model.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;

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
	 * @param entityId
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
