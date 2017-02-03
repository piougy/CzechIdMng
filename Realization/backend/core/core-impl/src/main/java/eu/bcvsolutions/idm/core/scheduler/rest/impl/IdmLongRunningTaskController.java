package eu.bcvsolutions.idm.core.scheduler.rest.impl;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteEntityController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.scheduler.dto.filter.LongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmLongRunningTaskService;

/**
 * Default controller for scripts, basic methods.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RepositoryRestController
@RequestMapping(value = BaseController.BASE_PATH + "/long-running-tasks")
public class IdmLongRunningTaskController extends AbstractReadWriteEntityController<IdmLongRunningTask, LongRunningTaskFilter> {
	
	@Autowired
	public IdmLongRunningTaskController(EntityLookupService entityLookupService) {
		super(entityLookupService);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	public Resources<?> find(@RequestParam MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable, 			
			PersistentEntityResourceAssembler assembler) {
		return super.find(parameters, pageable, assembler);
	}
	
	/**
	 * All endpoints will support find quick method.
	 * 
	 * @param parameters
	 * @param pageable
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	public Resources<?> findQuick(@RequestParam MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable, 			
			PersistentEntityResourceAssembler assembler) {
		return super.find(parameters, pageable, assembler);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	public ResponseEntity<?> get(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		return super.get(backendId, assembler);
	}
	
	/**
	 * Cancels running job
	 *
	 * @param taskName name of task
	 * @param triggerName name of trigger
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.PUT, value = "/{backendId}/cancel")
	public ResponseEntity<?> cancel(@PathVariable UUID backendId) {
		entityLookupService.getEntityService(IdmLongRunningTask.class, IdmLongRunningTaskService.class).cancel(backendId);
		//
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
	
	/**
	 * Kills running job
	 *
	 * @param taskName name of task
	 * @param triggerName name of trigger
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.PUT, value = "/{backendId}/interrupt")
	public ResponseEntity<?> interrupt(@PathVariable UUID backendId) {
		entityLookupService.getEntityService(IdmLongRunningTask.class, IdmLongRunningTaskService.class).interrupt(backendId);
		//
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
}
