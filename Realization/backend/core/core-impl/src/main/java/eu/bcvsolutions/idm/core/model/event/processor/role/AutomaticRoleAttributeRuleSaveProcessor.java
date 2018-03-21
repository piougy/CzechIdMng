package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleService;
import eu.bcvsolutions.idm.core.model.event.AutomaticRoleAttributeRuleEvent.AutomaticRoleAttributeRuleEventType;

/**
 * Persist rule for automic role
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
@Description("Save or update rule for automatic role.")
public class AutomaticRoleAttributeRuleSaveProcessor extends CoreEventProcessor<IdmAutomaticRoleAttributeRuleDto> {

	public static final String PROCESSOR_NAME = "automatic-role-attribute-rule-save-crocessor";
	
	private final IdmAutomaticRoleAttributeRuleService automactiRoleAttributeRuleService;
	
	@Autowired
	public AutomaticRoleAttributeRuleSaveProcessor(
			IdmAutomaticRoleAttributeRuleService automactiRoleAttributeRuleService) {
		super(AutomaticRoleAttributeRuleEventType.CREATE, AutomaticRoleAttributeRuleEventType.UPDATE);
		//
		Assert.notNull(automactiRoleAttributeRuleService);
		//
		this.automactiRoleAttributeRuleService = automactiRoleAttributeRuleService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<IdmAutomaticRoleAttributeRuleDto> process(EntityEvent<IdmAutomaticRoleAttributeRuleDto> event) {
		IdmAutomaticRoleAttributeRuleDto dto = event.getContent();
		//
		dto = automactiRoleAttributeRuleService.saveInternal(dto);
		event.setContent(dto);
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public boolean isDisableable() {
		return false;
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}

}
