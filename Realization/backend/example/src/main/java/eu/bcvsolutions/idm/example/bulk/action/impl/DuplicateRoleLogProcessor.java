package eu.bcvsolutions.idm.example.bulk.action.impl;

import java.util.List;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleProcessor;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.example.ExampleModuleDescriptor;

/**
 * Duplicate role - example processor, just create log
 * 
 * @author Radek Tomiška
 * @since 9.5.0
 */
@Enabled(ExampleModuleDescriptor.MODULE_ID)
@Component(DuplicateRoleLogProcessor.PROCESSOR_NAME)
@Description("Duplicate role - composition and recursion.")
public class DuplicateRoleLogProcessor
		extends CoreEventProcessor<IdmRoleDto> 
		implements RoleProcessor {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DuplicateRoleLogProcessor.class);
	//
	public static final String PROCESSOR_NAME = "example-duplicate-role-log-processor";
	public static final String PARAMETER_INCLUDE_LOG = "include-log";
	
	public DuplicateRoleLogProcessor() {
		super(RoleEventType.DUPLICATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto include = new IdmFormAttributeDto(
				PARAMETER_INCLUDE_LOG,
				"Log duplicated roles", 
				PersistentType.BOOLEAN);
		include.setDefaultValue(Boolean.TRUE.toString());
		//
		return Lists.newArrayList(include);
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmRoleDto> event) {
		return super.conditional(event) 
				&& getBooleanProperty(PARAMETER_INCLUDE_LOG, event.getProperties());
	}

	@Override
	public EventResult<IdmRoleDto> process(EntityEvent<IdmRoleDto> event) {
		IdmRoleDto duplicate = event.getContent();
		IdmRoleDto originalSource = event.getOriginalSource();
		//
		LOG.info("Duplicate role [{}] from environment [{}] to [{}]", originalSource.getBaseCode(), originalSource.getEnvironment(), duplicate.getEnvironment());
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return 10000;
	}
}
