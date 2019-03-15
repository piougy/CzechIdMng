package eu.bcvsolutions.idm.core.model.event.processor.role;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleProcessor;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.event.AutomaticRoleAttributeEvent;
import eu.bcvsolutions.idm.core.model.event.AutomaticRoleAttributeEvent.AutomaticRoleAttributeEventType;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;

/**
 * Duplicate role - automatic roles by attribute.
 * 
 * @author Radek Tomiška
 * @since 9.5.0
 */
@Component(DuplicateRoleAutomaticByAttributeProcessor.PROCESSOR_NAME)
@Description("Duplicate role - automatic roles by attribute.")
public class DuplicateRoleAutomaticByAttributeProcessor
		extends CoreEventProcessor<IdmRoleDto> 
		implements RoleProcessor {
	
	public static final String PROCESSOR_NAME = "core-duplicate-role-automatic-by-attribute-processor";
	//
	@Autowired private IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	@Autowired private IdmAutomaticRoleAttributeRuleService automaticRoleAttributeRuleService;
	@Autowired private EntityStateManager entityStateManager;
	
	public DuplicateRoleAutomaticByAttributeProcessor() {
		super(RoleEventType.DUPLICATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		// Lookout: DuplicateRoleAutomaticByTreeProcessor can be disabled => no method can be called on the disabled @Component => register attribute again for sure.
		IdmFormAttributeDto include = new IdmFormAttributeDto(
				DuplicateRoleAutomaticByTreeProcessor.PARAMETER_INCLUDE_AUTOMATIC_ROLE,
				"Duplicate automatic roles", 
				PersistentType.BOOLEAN);
		include.setDefaultValue(Boolean.TRUE.toString());
		//
		return Lists.newArrayList(include);
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmRoleDto> event) {
		return super.conditional(event) 
				&& getBooleanProperty(DuplicateRoleAutomaticByTreeProcessor.PARAMETER_INCLUDE_AUTOMATIC_ROLE, event.getProperties());
	}

	@Override
	public EventResult<IdmRoleDto> process(EntityEvent<IdmRoleDto> event) {
		IdmRoleDto cloned = event.getContent();
		IdmRoleDto originalSource = event.getOriginalSource();
		//
		IdmAutomaticRoleFilter filter = new IdmAutomaticRoleFilter();
		filter.setRoleId(cloned.getId());
		Set<UUID> usedAutomaticRoles = new HashSet<>();
		List<IdmAutomaticRoleAttributeDto> currentAutomaticRoles = automaticRoleAttributeService.find(filter,  null).getContent();
		//
		filter.setRoleId(originalSource.getId());
		automaticRoleAttributeService
			.find(filter, null)
			.forEach(automaticRole -> {
				UUID exists = exists(currentAutomaticRoles, automaticRole);
				if (exists != null) {
					usedAutomaticRoles.add(exists);
				} else {
					// create new with all rules
					IdmAutomaticRoleAttributeDto clonedAutomaticRole = new IdmAutomaticRoleAttributeDto();
					clonedAutomaticRole.setName(automaticRole.getName());
					clonedAutomaticRole.setRole(cloned.getId());
					clonedAutomaticRole.setConcept(true);
					//
					clonedAutomaticRole = automaticRoleAttributeService.save(clonedAutomaticRole);
					//
					for (IdmAutomaticRoleAttributeRuleDto rule : automaticRoleAttributeRuleService.findAllRulesForAutomaticRole(automaticRole.getId())) {
						IdmAutomaticRoleAttributeRuleDto clonedRule = new IdmAutomaticRoleAttributeRuleDto();
						clonedRule.setAutomaticRoleAttribute(clonedAutomaticRole.getId());
						clonedRule.setAttributeName(rule.getAttributeName());
						clonedRule.setFormAttribute(rule.getFormAttribute());
						clonedRule.setType(rule.getType());
						clonedRule.setValue(rule.getValue());
						clonedRule.setComparison(rule.getComparison());
						//
						automaticRoleAttributeRuleService.save(clonedRule);
					}
					AutomaticRoleAttributeEvent automaticRoleEvent = new AutomaticRoleAttributeEvent(AutomaticRoleAttributeEventType.UPDATE, clonedAutomaticRole);
					automaticRoleEvent.setPriority(PriorityType.IMMEDIATE); // execute sync
					// FIXME: event parent, setTransactionId ...
					automaticRoleAttributeService.recalculate(automaticRoleEvent);
				}
			});
		//
		// remove not used originals
		currentAutomaticRoles
			.stream()
			.filter(automaticRole -> {
				return !usedAutomaticRoles.contains(automaticRole.getId());
			})
			.forEach(automaticRole -> {
				// dirty flag automatic role only - will be processed after parent action ends
				IdmEntityStateDto stateDeleted = new IdmEntityStateDto();
				stateDeleted.setEvent(event.getId());
				stateDeleted.setTransactionId(event.getTransactionId());
				stateDeleted.setSuperOwnerId(cloned.getId());
				stateDeleted.setResult(new OperationResultDto.Builder(OperationState.RUNNING)
						.setModel(new DefaultResultModel(CoreResultCode.DELETED)).build());
				entityStateManager.saveState(automaticRole, stateDeleted);
			});
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return 300;
	}
	
	private UUID exists(List<IdmAutomaticRoleAttributeDto> from, IdmAutomaticRoleAttributeDto automaticRole) {
		return from
				.stream()
				.filter(a -> {
					return new EqualsBuilder()
							.append(a.getName(), automaticRole.getName())
							.isEquals();
				})
				.filter(a -> {
					List<IdmAutomaticRoleAttributeRuleDto> fromRules = automaticRoleAttributeRuleService.findAllRulesForAutomaticRole(a.getId());
					List<IdmAutomaticRoleAttributeRuleDto> toRules = automaticRoleAttributeRuleService.findAllRulesForAutomaticRole(automaticRole.getId());
					// all rules has to be the same
					return equals(fromRules, toRules);
				})
				.findFirst()
				.map(IdmAutomaticRoleAttributeDto::getId)
				.orElse(null);
	}
	
	private boolean equals(List<IdmAutomaticRoleAttributeRuleDto> from, List<IdmAutomaticRoleAttributeRuleDto> to) {
		if (from.size() != to.size()) {
			return false;
		}
		return from
			.stream()
			.allMatch(rule -> {
				// rule with the same setting is the same => more occurrences of the "same" rule are ignored
				return exists(to, rule) != null;
			});
	}
	
	private UUID exists(List<IdmAutomaticRoleAttributeRuleDto> from, IdmAutomaticRoleAttributeRuleDto rule) {
		return from
				.stream()
				.filter(a -> {
					return new EqualsBuilder()
							.append(a.getAttributeName(), rule.getAttributeName())
							.append(a.getFormAttribute(), rule.getFormAttribute())
							.append(a.getType(), rule.getType())
							.append(a.getValue(), rule.getValue())
							.append(a.getComparison(), rule.getComparison())
							.isEquals();
				})
				.findFirst()
				.map(IdmAutomaticRoleAttributeRuleDto::getId)
				.orElse(null);
	}
}
