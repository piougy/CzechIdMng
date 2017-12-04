package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;

@Component
@Description("Delete automatic role by attribute.")
public class AutomaticRoleAttributeDeleteProcessor extends CoreEventProcessor<IdmAutomaticRoleAttributeDto> {

	public static final String PROCESSOR_NAME = "automatic-role-attribute-delete-processor";
	
	private final IdmAutomaticRoleAttributeRuleService automaticRoleAttributeRuleService;
	private final IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	
	@Autowired
	public AutomaticRoleAttributeDeleteProcessor(
			IdmAutomaticRoleAttributeRuleService automaticRoleAttributeRuleService,
			IdmAutomaticRoleAttributeService automaticRoleAttributeService) {
		Assert.notNull(automaticRoleAttributeRuleService);
		Assert.notNull(automaticRoleAttributeService);
		//
		this.automaticRoleAttributeRuleService = automaticRoleAttributeRuleService;
		this.automaticRoleAttributeService = automaticRoleAttributeService;
	}
	
	@Override
	public EventResult<IdmAutomaticRoleAttributeDto> process(EntityEvent<IdmAutomaticRoleAttributeDto> event) {
		IdmAutomaticRoleAttributeDto content = event.getContent();
		//
		// remove all rules
		automaticRoleAttributeRuleService.deleteAllByAttribute(content.getId());
		//
		// delete
		automaticRoleAttributeService.deleteInternal(content);
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean isDisableable() {
		return false;
	}
}
