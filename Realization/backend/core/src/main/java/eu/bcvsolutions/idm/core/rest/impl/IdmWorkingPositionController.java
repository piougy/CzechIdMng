package eu.bcvsolutions.idm.core.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentityWorkingPosition;
import eu.bcvsolutions.idm.core.model.service.IdmIdentityWorkingPositionService;

@RestController
@RequestMapping(value = "/api/workingPositions")
public class IdmWorkingPositionController extends DefaultReadWriteEntityController<IdmIdentityWorkingPosition> {
	
	@Autowired
	public IdmWorkingPositionController(IdmIdentityWorkingPositionService identityWorkingPositionService) {
		super(identityWorkingPositionService);
	}
}
