package eu.bcvsolutions.idm.core.rest.impl;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.EmptyFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityWorkingPosition;
import eu.bcvsolutions.idm.core.model.service.IdmIdentityWorkingPositionService;
import eu.bcvsolutions.idm.core.rest.BaseEntityController;

@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/workingPositions")
public class IdmWorkingPositionController extends DefaultReadWriteEntityController<IdmIdentityWorkingPosition, EmptyFilter> {
	
	@Autowired
	public IdmWorkingPositionController(IdmIdentityWorkingPositionService identityWorkingPositionService) {
		super(identityWorkingPositionService);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.SYSTEM_ADMIN + "')")
	public ResponseEntity<?> create(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.create(nativeRequest, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.SYSTEM_ADMIN + "')")
	public ResponseEntity<?> patch(String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		// TODO Auto-generated method stub
		return super.patch(backendId, nativeRequest, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.SYSTEM_ADMIN + "')")
	public ResponseEntity<?> update(String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.update(backendId, nativeRequest, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.SYSTEM_ADMIN + "')")
	public ResponseEntity<?> delete(String backendId) {
		return super.delete(backendId);
	}
}
