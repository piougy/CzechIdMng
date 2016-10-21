package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.RoleCatalogueFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.service.IdmRoleCatalogueService;

/**
 * Role catalogue controller
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/roleCatalogues")
public class IdmRoleCatalogueController extends DefaultReadWriteEntityController<IdmRoleCatalogue, RoleCatalogueFilter> {
	
	@Autowired
	private IdmRoleCatalogueService roleCatalogueService;
	
	@Autowired
	public IdmRoleCatalogueController(EntityLookupService entityLookupService, IdmRoleCatalogueService entityService) {
		super(entityLookupService, entityService);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_CATALOGUE_WRITE + "')")
	public ResponseEntity<?> create(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.create(nativeRequest, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_CATALOGUE_WRITE + "')")
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_CATALOGUE_DELETE + "')")
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_CATALOGUE_WRITE + "')")
	public ResponseEntity<?> update(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.update(backendId, nativeRequest, assembler);
	}
	
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_CATALOGUE_READ + "')")
	@RequestMapping(value = "/search/roots", method = RequestMethod.GET)
	public Resources<?> findRoots(PersistentEntityResourceAssembler assembler) {	
		List<IdmRoleCatalogue> listOfRoots = this.roleCatalogueService.findRoots();
		
		return toResources((Iterable<?>) listOfRoots, assembler, IdmRoleCatalogue.class, null);
	}
	
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_CATALOGUE_READ + "')")
	@RequestMapping(value = "/search/children", method = RequestMethod.GET)
	public Resources<?> findChildren(@Param(value = "parent") @NotNull Long parent, PersistentEntityResourceAssembler assembler) {	
		List<IdmRoleCatalogue> listOfChildren = this.roleCatalogueService.findChildrenByParent(parent);
		
		return toResources((Iterable<?>) listOfChildren, assembler, IdmRoleCatalogue.class, null);
	}
	
	@Override
	protected RoleCatalogueFilter toFilter(MultiValueMap<String, Object> parameters) {
		RoleCatalogueFilter filter = new RoleCatalogueFilter();
		filter.setText(this.convertStringParameter(parameters, "text"));
		filter.setParent(this.convertEntityParameter(parameters, "parent", IdmRoleCatalogue.class));
		return filter;
	}
	
	
}
