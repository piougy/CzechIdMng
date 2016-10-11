package eu.bcvsolutions.idm.core.rest.impl;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.dto.EmptyFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.service.IdmIdentityContractService;

@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/identityContracts")
public class IdmIdentityContractController extends DefaultReadWriteEntityController<IdmIdentityContract, EmptyFilter> {
	
	@Autowired
	public IdmIdentityContractController(IdmIdentityContractService identityWorkingPositionService) {
		super(identityWorkingPositionService);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	public ResponseEntity<?> create(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.create(nativeRequest, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		// TODO Auto-generated method stub
		return super.patch(backendId, nativeRequest, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	public ResponseEntity<?> update(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.update(backendId, nativeRequest, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
}
