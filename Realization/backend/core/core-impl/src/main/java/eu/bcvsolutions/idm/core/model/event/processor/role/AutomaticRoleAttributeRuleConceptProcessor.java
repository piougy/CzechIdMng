package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.model.event.AutomaticRoleAttributeRuleEvent.AutomaticRoleAttributeRuleEventType;

/**
 * Set concept to {@link IdmAutomaticRoleAttributeDto}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
@Description("Set concept to automatic role after update, create or delete rule.")
public class AutomaticRoleAttributeRuleConceptProcessor extends CoreEventProcessor<IdmAutomaticRoleAttributeRuleDto> {

	public static final String PROCESSOR_NAME = "automatic-role-attribute-rule-concept-crocessor";
	
	private final IdmAutomaticRoleAttributeService automactiRoleAttributeService;
	
	@Autowired
	public AutomaticRoleAttributeRuleConceptProcessor(
			IdmAutomaticRoleAttributeService automactiRoleAttributeService) {
		super(AutomaticRoleAttributeRuleEventType.CREATE, AutomaticRoleAttributeRuleEventType.DELETE, AutomaticRoleAttributeRuleEventType.UPDATE);
		//
		Assert.notNull(automactiRoleAttributeService);
		//
		this.automactiRoleAttributeService = automactiRoleAttributeService;
	}

	@Override
	public EventResult<IdmAutomaticRoleAttributeRuleDto> process(EntityEvent<IdmAutomaticRoleAttributeRuleDto> event) {
		IdmAutomaticRoleAttributeRuleDto dto = event.getContent();
		IdmAutomaticRoleAttributeDto automaticRole = automactiRoleAttributeService.get(dto.getAutomaticRoleAttribute());
		//
		if (automaticRole == null) {
			throw new IllegalStateException("Automatic role [" + dto.getAutomaticRoleAttribute()  + "] is null. Please check this rule: " + dto.getId());
		}
		// set concept
		if (!automaticRole.isConcept()) {
			automaticRole.setConcept(true);
			automaticRole = automactiRoleAttributeService.save(automaticRole);
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public int getOrder() {
		// after save
		return super.getOrder() + 100;
	}
}
