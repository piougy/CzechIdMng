package eu.bcvsolutions.idm.core.rest.impl;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.RoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.exception.AcceptedException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.service.impl.StatelessAsynchronousTask;

/**
 * Automatic role controller
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/role-tree-nodes")
public class IdmRoleTreeNodeController extends AbstractReadWriteDtoController<IdmRoleTreeNodeDto, RoleTreeNodeFilter> {
	
	@Autowired private LongRunningTaskManager taskManager;
	
	@Autowired
	public IdmRoleTreeNodeController(IdmRoleTreeNodeService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLETREENODE_READ + "')")
	public Resources<?> find(@RequestParam MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLETREENODE_READ + "')")
	public Resources<?> findQuick(@RequestParam MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLETREENODE_READ + "')")
	public ResponseEntity<?> get(@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLETREENODE_CREATE + "') or hasAuthority('" + CoreGroupPermission.ROLETREENODE_UPDATE + "')")
	public ResponseEntity<?> post(@Valid @RequestBody IdmRoleTreeNodeDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLETREENODE_UPDATE + "')")
	public ResponseEntity<?> put(@PathVariable @NotNull String backendId, @Valid @RequestBody IdmRoleTreeNodeDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLETREENODE_UPDATE + "')")
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLETREENODE_DELETE + "')")
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		LongRunningTaskExecutor<?> asyncTask = new StatelessAsynchronousTask() {
			
			@Override
			public String getName() {
				return "RemoveAutomaticRoleTask";
			}
			
			@Override
			public String getDescription() {
				return String.format("Remove automatic role [%s] asynchronously", backendId);
			}
			
			@Override
			public Boolean process() {
				IdmRoleTreeNodeController.super.delete(backendId);
				return Boolean.TRUE;			
			}
		};
		taskManager.execute(asyncTask);
		// TODO: improve status handling on FE
		// return new ResponseEntity<Object>(HttpStatus.ACCEPTED);
		throw new AcceptedException();
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLETREENODE_READ + "')")
	public Set<String> getPermissions(@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@Override
	public IdmRoleTreeNodeDto postDto(IdmRoleTreeNodeDto entity) {
		if (!getService().isNew(entity)) {
			throw new ResultCodeException(CoreResultCode.METHOD_NOT_ALLOWED, "Automatic role update is not supported");
		}
		return super.postDto(entity);
	}
}
