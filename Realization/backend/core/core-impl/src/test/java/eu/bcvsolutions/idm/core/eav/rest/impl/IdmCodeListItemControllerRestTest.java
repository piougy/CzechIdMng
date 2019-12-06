package eu.bcvsolutions.idm.core.eav.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListItemDto;
import eu.bcvsolutions.idm.core.eav.api.service.IdmCodeListService;

/**
 * Controller tests
 * - CRUD
 * 
 * @author Radek Tomiška
 *
 */
public class IdmCodeListItemControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmCodeListItemDto> {

	@Autowired private IdmCodeListItemController controller;
	
	@Override
	protected boolean supportsFormValues() {
		return false;
	}
	
	@Override
	protected AbstractReadWriteDtoController<IdmCodeListItemDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmCodeListItemDto prepareDto() {
		IdmCodeListDto codeList = new IdmCodeListDto();
		codeList.setName(getHelper().createName());
		codeList.setCode(getHelper().createName());
		codeList = getHelper().getService(IdmCodeListService.class).save(codeList);
		//
		IdmCodeListItemDto dto = new IdmCodeListItemDto();
		dto.setName(getHelper().createName());
		dto.setCode(getHelper().createName());
		dto.setCodeList(codeList.getId());
		//
		return dto;
	}
}
