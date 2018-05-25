package eu.bcvsolutions.idm.core.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.IdmScriptCategory;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

public class IdmScriptControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmScriptDto> {

	@Autowired
	private IdmScriptController controller;

	@Override
	protected AbstractReadWriteDtoController<IdmScriptDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmScriptDto prepareDto() {
		IdmScriptDto script = new IdmScriptDto();
		script.setCode(getHelper().createName());
		script.setName(getHelper().createName());
		script.setCategory(IdmScriptCategory.DEFAULT);
		return script;
	}
}
