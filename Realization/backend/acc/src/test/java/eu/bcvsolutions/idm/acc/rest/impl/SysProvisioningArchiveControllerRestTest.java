package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.DefaultAccTestHelper;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Controller tests
 * - CRUD
 * - TODO: all filters
 * 
 * @author Radek Tomiška
 *
 */
public class SysProvisioningArchiveControllerRestTest extends AbstractReadWriteDtoControllerRestTest<SysProvisioningArchiveDto> {

	@Autowired private SysProvisioningArchiveController controller;
	
	@Override
	protected AbstractReadWriteDtoController<SysProvisioningArchiveDto, ?> getController() {
		return controller;
	}

	@Override
	protected SysProvisioningArchiveDto prepareDto() {
		SysProvisioningArchiveDto dto = new SysProvisioningArchiveDto();
		dto.setSystem(getHelper().createTestResourceSystem(false).getId());
		dto.setEntityIdentifier(UUID.randomUUID());
		dto.setOperationType(ProvisioningEventType.CANCEL);
		dto.setEntityType(SystemEntityType.CONTRACT);
		dto.setProvisioningContext(new ProvisioningContext());
		dto.setResult(new OperationResult(OperationState.CANCELED));
		//
		return dto;
	}
	
	@Override
	protected boolean isReadOnly() {
		return true;
	}
	
	@Override
	protected boolean supportsAutocomplete() {
		return false;
	}
	
	@Override
	protected DefaultAccTestHelper getHelper() {
		return (DefaultAccTestHelper) super.getHelper();
	}
}
