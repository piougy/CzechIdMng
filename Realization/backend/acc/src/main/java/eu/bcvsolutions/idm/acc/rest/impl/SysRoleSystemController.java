package eu.bcvsolutions.idm.acc.rest.impl;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.dto.RoleSystemFilter;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.acc.service.api.AccRoleSystemService;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.rest.impl.DefaultReadWriteEntityController;
import eu.bcvsolutions.idm.security.api.domain.IfEnabled;;

/**
 * Role could assign identity account on target system.
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@IfEnabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseEntityController.BASE_PATH + "/role-systems")
public class SysRoleSystemController extends DefaultReadWriteEntityController<SysRoleSystem, RoleSystemFilter> {
	
	@Autowired
	public SysRoleSystemController(EntityLookupService entityLookupService, AccRoleSystemService roleSysteService) {
		super(entityLookupService, roleSysteService);
	}
	
	@Override
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_READ + "')")
	public Resources<?> find(@RequestParam MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable, 			
			PersistentEntityResourceAssembler assembler) {
		return super.find(parameters, pageable, assembler);
	}
	
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_READ + "')")
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	public Resources<?> findQuick(@RequestParam MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable, 			
			PersistentEntityResourceAssembler assembler) {
		return super.find(parameters, pageable, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	public ResponseEntity<?> get(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		return super.get(backendId, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_WRITE + "')")
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> create(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.create(nativeRequest, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_WRITE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	public ResponseEntity<?> update(
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.update(backendId, nativeRequest, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_WRITE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler) 
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_WRITE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	protected RoleSystemFilter toFilter(MultiValueMap<String, Object> parameters) {
		RoleSystemFilter filter = new RoleSystemFilter();
		filter.setRoleId(convertUuidParameter(parameters, "roleId"));
		filter.setSystemId(convertUuidParameter(parameters, "systemId"));
		return filter;
	}
}
