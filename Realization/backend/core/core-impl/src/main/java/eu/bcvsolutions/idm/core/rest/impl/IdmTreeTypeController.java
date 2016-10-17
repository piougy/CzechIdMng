package eu.bcvsolutions.idm.core.rest.impl;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.dto.QuickFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;

/**
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH + BaseEntityController.TREE_BASE_PATH + "/types")
public class IdmTreeTypeController extends DefaultReadWriteEntityController<IdmTreeType, QuickFilter> {
	
	@Autowired
	public IdmTreeTypeController(EntityLookupService entityLookupService) {
		super(entityLookupService);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.TREETYPE_WRITE + "')")
	public ResponseEntity<?> create(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.create(nativeRequest, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.TREETYPE_DELETE + "')")
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.TREETYPE_WRITE + "')")
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.TREETYPE_WRITE + "')")
	public ResponseEntity<?> update(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.update(backendId, nativeRequest, assembler);
	}
	
	@Override
	protected QuickFilter toFilter(MultiValueMap<String, Object> parameters) {
		QuickFilter filter = new QuickFilter();
		filter.setText((String)parameters.toSingleValueMap().get("text"));
		return filter;
	}
}
