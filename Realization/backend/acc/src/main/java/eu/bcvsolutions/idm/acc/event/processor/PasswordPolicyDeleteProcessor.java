package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.PasswordPolicyEvent.PasswordPolicyEvenType;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Remove password policies from system after delete.
 * 
 * @author Ondrej Kopr
 */
@Component("accPasswordPolicyDeleteProcessor")
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Remove password policies from system after delete.")
public class PasswordPolicyDeleteProcessor extends CoreEventProcessor<IdmPasswordPolicyDto> {
	
	public static final String PROCESSOR_NAME = "password-policy-delete-processor";
	//
	@Autowired public SysSystemService systemService;
	
	public PasswordPolicyDeleteProcessor() {
		super(PasswordPolicyEvenType.DELETE);
	}
	
	@Override
	public EventResult<IdmPasswordPolicyDto> process(EntityEvent<IdmPasswordPolicyDto> event) {
		// cascade set to null all references in sysSystem to remove password policy
		IdmPasswordPolicyDto dto = event.getContent();
		Assert.notNull(dto.getId(), "Password policy identifier is required.");
		// remove references to password policy 
		SysSystemFilter filter = new SysSystemFilter();
		filter.setPasswordPolicyGenerationId(dto.getId());
		systemService
			.find(filter, null)
			.forEach(system -> {
				system.setPasswordPolicyGenerate(null);
				systemService.save(system);
			});
		filter.setPasswordPolicyGenerationId(null);
		filter.setPasswordPolicyValidationId(dto.getId());
		systemService
			.find(filter, null)
			.forEach(system -> {
				system.setPasswordPolicyValidate(null);
				systemService.save(system);
			});
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER - 1;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

}
