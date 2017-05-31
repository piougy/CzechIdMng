package eu.bcvsolutions.idm.core.rest.impl;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryRestController;
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

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.filter.RoleCatalogueFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleCatalogueService;

/**
 * Role catalogue controller
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RepositoryRestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/role-catalogues")
public class IdmRoleCatalogueController extends DefaultReadWriteDtoController<IdmRoleCatalogueDto, RoleCatalogueFilter> {
	
	@Autowired
	private IdmRoleCatalogueService roleCatalogueService;
	
	@Autowired
	public IdmRoleCatalogueController(IdmRoleCatalogueService entityService) {
		super(entityService);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_CREATE + "') or hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_UPDATE + "')")
	public ResponseEntity<?> post(@Valid @RequestBody IdmRoleCatalogueDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_UPDATE + "')")
	public ResponseEntity<?> put(@PathVariable @NotNull String backendId, @Valid @RequestBody IdmRoleCatalogueDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_UPDATE + "')")
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_DELETE + "')")
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/roots", method = RequestMethod.GET)
	public Resources<?> findRoots(@PageableDefault Pageable pageable) {	
		Page<IdmRoleCatalogueDto> roots = roleCatalogueService.findRoots(pageable);
		return toResources(roots, IdmRoleCatalogue.class);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/children", method = RequestMethod.GET)
	public Resources<?> findChildren(
			@RequestParam(name = "parent", required = true) @NotNull String parentId,
			@PageableDefault Pageable pageable) {
		Page<IdmRoleCatalogueDto> children = roleCatalogueService.findChildrenByParent(UUID.fromString(parentId), pageable);
		return toResources(children, IdmRoleCatalogue.class);
	}	
	
	@Override
	protected RoleCatalogueFilter toFilter(MultiValueMap<String, Object> parameters) {
		RoleCatalogueFilter filter = new RoleCatalogueFilter();
		filter.setText(getParameterConverter().toString(parameters, "text"));
		filter.setName(getParameterConverter().toString(parameters, "name"));
		filter.setCode(getParameterConverter().toString(parameters, "code"));
		filter.setParent(getParameterConverter().toEntityUuid(parameters, "parent", IdmRoleCatalogue.class));
		return filter;
	}
}
