package eu.bcvsolutions.idm.core.model.event.processor.role;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleFormAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFormAttributeFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmRoleFormAttributeService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;

/**
 * Duplicate role - duplicate / update role form attributes.
 * 
 * @author Radek Tomiška
 * @since 9.5.0
 */
@Component(DuplicateRoleFormAttributeProcessor.PROCESSOR_NAME)
@Description("Duplicate role - duplicate / update role form attributes. ")
public class DuplicateRoleFormAttributeProcessor
		extends CoreEventProcessor<IdmRoleDto> 
		implements RoleProcessor {
	
	public static final String PROCESSOR_NAME = "core-duplicate-role-form-attribute-processor";
	public static final String PARAMETER_INCLUDE_ROLE_FORM_ATTRIBUTE = "include-role-form-attribute";
	//
	@Autowired private IdmRoleFormAttributeService roleFormAttributeService;
	
	public DuplicateRoleFormAttributeProcessor() {
		super(RoleEventType.DUPLICATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto include = new IdmFormAttributeDto(
				PARAMETER_INCLUDE_ROLE_FORM_ATTRIBUTE,
				"Duplicate role form attributes", 
				PersistentType.BOOLEAN);
		include.setDefaultValue(Boolean.TRUE.toString());
		//
		return Lists.newArrayList(include);
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmRoleDto> event) {
		return super.conditional(event) 
				&& getBooleanProperty(PARAMETER_INCLUDE_ROLE_FORM_ATTRIBUTE, event.getProperties());
	}

	@Override
	public EventResult<IdmRoleDto> process(EntityEvent<IdmRoleDto> event) {
		IdmRoleDto duplicate = event.getContent(); // newly setted role
		IdmRoleDto originalSource = event.getOriginalSource(); // cloned role
		Assert.notNull(originalSource.getId(), "Original source identifier is required."); // just for sure
		//
		// search current, create, update, remove
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		Map<UUID, IdmRoleFormAttributeDto> currentAttributes = null;
		if (duplicate.getId() != null) {
			filter.setRole(duplicate.getId());
			currentAttributes = roleFormAttributeService
				.find(filter,  null)
				.getContent()
				.stream()
				.collect(Collectors.toMap(IdmRoleFormAttributeDto::getFormAttribute, Function.identity()));
		} else {
			// we need prepare id - role will be persisted after
			duplicate.setId(UUID.randomUUID());
			currentAttributes = new HashMap<>();
		}
		//
		filter.setRole(originalSource.getId());
		for (IdmRoleFormAttributeDto originalRoleFormAttribute : roleFormAttributeService.find(filter,  null).getContent()) {
			IdmRoleFormAttributeDto duplicateRoleFormAttribute;
			if (currentAttributes.containsKey(originalRoleFormAttribute.getFormAttribute())) {
				duplicateRoleFormAttribute = currentAttributes.get(originalRoleFormAttribute.getFormAttribute());
				currentAttributes.remove(originalRoleFormAttribute.getFormAttribute()); // used => remove. Unused attributes will be removed at end.
			} else {
				duplicateRoleFormAttribute = new IdmRoleFormAttributeDto();
				duplicateRoleFormAttribute.setRole(duplicate.getId());
			}
			duplicateProperties(originalRoleFormAttribute, duplicateRoleFormAttribute);
			//
			roleFormAttributeService.save(duplicateRoleFormAttribute);
		}
		// remove removed attributed at end
		currentAttributes.values().forEach(attribute -> {
			roleFormAttributeService.delete(attribute);
		});
		//
		event.setContent(duplicate);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return 50;
	}
	
	private void duplicateProperties(IdmRoleFormAttributeDto original, IdmRoleFormAttributeDto duplicate) {
		duplicate.setFormAttribute(original.getFormAttribute());
		duplicate.setDefaultValue(original.getDefaultValue());
		duplicate.setUnique(original.isUnique());
		duplicate.setMin(original.getMin());
		duplicate.setMax(original.getMax());
		duplicate.setRegex(original.getRegex());
		duplicate.setRequired(original.isRequired());
		duplicate.setValidationMessage(original.getValidationMessage());
	}

}
