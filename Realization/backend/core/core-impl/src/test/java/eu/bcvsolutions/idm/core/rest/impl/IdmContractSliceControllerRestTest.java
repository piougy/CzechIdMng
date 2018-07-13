package eu.bcvsolutions.idm.core.rest.impl;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Contract slices rest test
 * 
 * @author Radek Tomiška
 *
 */
public class IdmContractSliceControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmContractSliceDto> {

	@Autowired private IdmContractSliceController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmContractSliceDto, ?> getController() {
		return controller;
	}

	@Override
	protected boolean supportsPatch() {
		return false;
	}

	@Override
	protected IdmContractSliceDto prepareDto() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmContractSliceDto slice = new IdmContractSliceDto();
		slice.setIdentity(identity.getId());
		slice.setPosition(getHelper().createName());
		slice.setContractCode(getHelper().createName());
		slice.setValidFrom(new LocalDate());
		slice.setContractValidFrom(new LocalDate());
		//
		return slice;
	}
	
	/**
	 * Slices has form definitions by contract
	 */
	@Override
	protected Class<? extends Identifiable> getFormOwnerType() {
		return IdmIdentityContractDto.class;
	}
}
