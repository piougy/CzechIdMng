package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleCatalogueFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleCatalogueService;

/**
 * Role catalogue controller
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RepositoryRestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/role-catalogues")
public class IdmRoleCatalogueController extends DefaultReadWriteEntityController<IdmRoleCatalogue, RoleCatalogueFilter> {
	
	@Autowired
	private IdmRoleCatalogueService roleCatalogueService;
	
	@Autowired
	public IdmRoleCatalogueController(LookupService entityLookupService, IdmRoleCatalogueService entityService) {
		super(entityLookupService, entityService);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_CREATE + "') or hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_UPDATE + "')")
	public ResponseEntity<?> post(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.post(nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_UPDATE + "')")
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_UPDATE + "')")
	public ResponseEntity<?> put(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.put(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_DELETE + "')")
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/roots", method = RequestMethod.GET)
	public Resources<?> findRoots(PersistentEntityResourceAssembler assembler) {	
		List<IdmRoleCatalogue> listOfRoots = this.roleCatalogueService.findRoots(null).getContent();
		
		return toResources((Iterable<?>) listOfRoots, assembler, IdmRoleCatalogue.class, null);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/children", method = RequestMethod.GET)
	public Resources<?> findChildren(@RequestParam(name = "parent", required = true) @NotNull String parentId, PersistentEntityResourceAssembler assembler) {
		IdmRoleCatalogue parent = getEntity(parentId);
		if (parent == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("roleCatalogue", parentId));
		}
		List<IdmRoleCatalogue> listOfChildren = this.roleCatalogueService.findDirectChildren(parent, null).getContent();
		
		return toResources((Iterable<?>) listOfChildren, assembler, IdmRoleCatalogue.class, null);
	}	
	
	@Override
	protected RoleCatalogueFilter toFilter(MultiValueMap<String, Object> parameters) {
		RoleCatalogueFilter filter = new RoleCatalogueFilter();
		filter.setText(getParameterConverter().toString(parameters, "text"));
		filter.setName(getParameterConverter().toString(parameters, "name"));
		filter.setCode(getParameterConverter().toString(parameters, "code"));
		filter.setParent(getParameterConverter().toEntity(parameters, "parent", IdmRoleCatalogue.class));
		return filter;
	}
}
