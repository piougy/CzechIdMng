package eu.bcvsolutions.idm.vs.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.vs.TestHelper;
import eu.bcvsolutions.idm.vs.domain.VsOperationType;
import eu.bcvsolutions.idm.vs.domain.VsRequestState;
import eu.bcvsolutions.idm.vs.dto.VsRequestDto;

/**
 * CRUD rest test.
 * 
 * @author Radek Tomiška
 *
 */
public class CommonVsRequestControllerRestTest extends AbstractReadWriteDtoControllerRestTest<VsRequestDto> {

	@Autowired private TestHelper helper;
	@Autowired private VsRequestController controller;
	
	@Override
	protected AbstractReadWriteDtoController<VsRequestDto, ?> getController() {
		return controller;
	}
	
	@Override
	protected VsRequestDto prepareDto() {
		VsRequestDto dto = new VsRequestDto();
		SysSystemDto virtualSystem = helper.createVirtualSystem(helper.createName());
		dto.setSystem(virtualSystem.getId());
		dto.setUid(helper.createName());
		dto.setConnectorKey("mock");
		dto.setOperationType(VsOperationType.CREATE);
		dto.setState(VsRequestState.CONCEPT);
		//
		return dto;
	}
	
	@Override
	protected boolean isReadOnly() {
		return true;
	}
}
