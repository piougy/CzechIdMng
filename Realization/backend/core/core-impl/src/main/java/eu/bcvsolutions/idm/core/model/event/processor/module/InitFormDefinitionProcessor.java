package eu.bcvsolutions.idm.core.model.event.processor.module;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractInitApplicationProcessor;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;

/**
 * Init default extended form definitions for formable types, if no definition exists.
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(InitFormDefinitionProcessor.PROCESSOR_NAME)
@Description("Init default extended form definitions for formable types (identity, role, contract, tree node).")
public class InitFormDefinitionProcessor extends AbstractInitApplicationProcessor {

	public static final String PROCESSOR_NAME = "core-init-form-definition-processor";
	//
	@Autowired private FormService formService;
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		//
		// prepare default form definitions
		// + definitions will be cached 
		if (formService.getDefinition(IdmIdentity.class) == null) {
			formService.createDefinition(IdmIdentity.class, new ArrayList<>());
		}
		if (formService.getDefinition(IdmRole.class) == null) {
			formService.createDefinition(IdmRole.class, new ArrayList<>());
		}
		if (formService.getDefinition(IdmTreeNode.class) == null) {
			formService.createDefinition(IdmTreeNode.class, null);
		}
		if (formService.getDefinition(IdmIdentityContract.class) == null) {
			formService.createDefinition(IdmIdentityContract.class, new ArrayList<>());
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// before 0 => eav has to be prepared before entities will be saved (generate default eav values).
		return CoreEvent.DEFAULT_ORDER - 200;
	}
}
