package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.bcvsolutions.idm.core.api.dto.IdmScriptAuthorityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.ScriptAuthorityFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.AvailableServiceDto;
import eu.bcvsolutions.idm.core.model.service.api.IdmScriptAuthorityService;

/**
 * Default controller for script authority (allowed services and class)
 * TODO: Same permission as scripts?
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RepositoryRestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/script-authorities")
public class IdmScriptAuthorityController extends DefaultReadWriteDtoController<IdmScriptAuthorityDto, ScriptAuthorityFilter>{
	
	private final IdmScriptAuthorityService service;
	
	@Autowired
	public IdmScriptAuthorityController(IdmScriptAuthorityService service) {
		super(service);
		//
		Assert.notNull(service);
		//
		this.service = service;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_READ + "')")
	public ResponseEntity<?> get(@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_READ + "')")
	public Resources<?> find(@RequestParam MultiValueMap<String, Object> parameters, @PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_READ + "')")
	public Resources<?> findQuick(@RequestParam MultiValueMap<String, Object> parameters, @PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/service", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_READ + "')")
	public Resources<?> findService(@RequestParam MultiValueMap<String, Object> parameters, @PageableDefault Pageable pageable) {
		String serviceName = parameters.get("serviceName") == null ? null : String.valueOf(parameters.get("serviceName"));
		List<AvailableServiceDto> services = service.findServices(serviceName);
		// TODO: pageable?
//		int start = pageable.getPageNumber() * pageable.getPageSize();
//		int end = (pageable.getPageNumber() * pageable.getPageSize()) + pageable.getPageSize();
//		if (end > services.size()) {
//			end = services.size();
//		}
		Page<AvailableServiceDto> pages = new PageImpl<AvailableServiceDto>(
				services, pageable, services.size());
		return toResources(pages, AvailableServiceDto.class);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_AUTOCOMPLETE + "')")
	public Resources<?> autocomplete(
			@RequestParam MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_UPDATE + "')")
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_CREATE + "') or hasAuthority('" + CoreGroupPermission.SCRIPT_UPDATE + "')")
	public ResponseEntity<?> post(@RequestBody @NotNull IdmScriptAuthorityDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_UPDATE + "')")
	public ResponseEntity<?> put(@PathVariable @NotNull String backendId, @RequestBody @NotNull IdmScriptAuthorityDto dto) {
		return super.put(backendId, dto);
	}
}
