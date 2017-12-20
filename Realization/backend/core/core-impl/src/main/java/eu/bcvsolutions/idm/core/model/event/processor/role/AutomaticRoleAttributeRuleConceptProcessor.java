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
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.model.event.AutomaticRoleAttributeRuleEvent.AutomaticRoleAttributeRuleEventType;

/**
 * Processor that delete, update or create entity (persist). All these operations is done in this processor.
 * Also set concept for {@link IdmAutomaticRoleAttributeDto}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
@Description("Recalculate automatic roles after save identity contract.")
public class AutomaticRoleAttributeRuleConceptProcessor extends CoreEventProcessor<IdmAutomaticRoleAttributeRuleDto> {

	public static final String PROCESSOR_NAME = "automatic-role-attribute-rule-concept-crocessor";
	
	private final IdmAutomaticRoleAttributeService automactiRoleAttributeService;
	private final IdmAutomaticRoleAttributeRuleService automactiRoleAttributeRuleService;
	
	@Autowired
	public AutomaticRoleAttributeRuleConceptProcessor(
			IdmAutomaticRoleAttributeService automactiRoleAttributeService,
			IdmAutomaticRoleAttributeRuleService automactiRoleAttributeRuleService) {
		super(AutomaticRoleAttributeRuleEventType.CREATE, AutomaticRoleAttributeRuleEventType.DELETE, AutomaticRoleAttributeRuleEventType.UPDATE);
		//
		Assert.notNull(automactiRoleAttributeService);
		Assert.notNull(automactiRoleAttributeRuleService);
		//
		this.automactiRoleAttributeService = automactiRoleAttributeService;
		this.automactiRoleAttributeRuleService = automactiRoleAttributeRuleService;
	}

	@Override
	public EventResult<IdmAutomaticRoleAttributeRuleDto> process(EntityEvent<IdmAutomaticRoleAttributeRuleDto> event) {
		EventType type = event.getType();
		IdmAutomaticRoleAttributeRuleDto dto = event.getContent();
		IdmAutomaticRoleAttributeDto automaticRole = automactiRoleAttributeService.get(dto.getAutomaticRoleAttribute());
		//
		if (type == AutomaticRoleAttributeRuleEventType.CREATE || type == AutomaticRoleAttributeRuleEventType.UPDATE) {
			dto = automactiRoleAttributeRuleService.saveInternal(dto);
			event.setContent(dto);
		} else if (type == AutomaticRoleAttributeRuleEventType.DELETE) {
			automactiRoleAttributeRuleService.deleteInternal(dto);
		} else {
			throw new UnsupportedOperationException("Event type: " + type.toString() + ", isn't implemented yet.");
		}
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
		return super.getOrder();
	}
}
