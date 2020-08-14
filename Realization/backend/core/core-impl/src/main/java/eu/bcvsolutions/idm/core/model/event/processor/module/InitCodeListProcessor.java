package eu.bcvsolutions.idm.core.model.event.processor.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractInitApplicationProcessor;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseCodeList;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListDto;
import eu.bcvsolutions.idm.core.eav.api.service.CodeListManager;

/**
 * Init base codelists (environment).
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(InitCodeListProcessor.PROCESSOR_NAME)
@Description("Init base codelists (environment).")
public class InitCodeListProcessor extends AbstractInitApplicationProcessor {

	public static final String PROCESSOR_NAME = "core-init-codelist-processor";
	//
	@Autowired private CodeListManager codeListManager;
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		//
		// prepare system code lists
		if (codeListManager.get(BaseCodeList.ENVIRONMENT) == null) {
			IdmCodeListDto environment = codeListManager.create(BaseCodeList.ENVIRONMENT);
			codeListManager.createItem(environment, "development", "environment.development.title");
			codeListManager.createItem(environment, "test", "environment.test.title");
			codeListManager.createItem(environment, "production", "environment.production.title");
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// before eav => codelist should be prepared for usage in fomr definitions.
		return CoreEvent.DEFAULT_ORDER - 300;
	}
}
