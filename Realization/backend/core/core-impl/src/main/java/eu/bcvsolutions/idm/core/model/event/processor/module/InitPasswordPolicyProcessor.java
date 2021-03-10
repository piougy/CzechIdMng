package eu.bcvsolutions.idm.core.model.event.processor.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractInitApplicationProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;

/**
 * Init base password policies.
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(InitPasswordPolicyProcessor.PROCESSOR_NAME)
@Description("Init base password policies for password validate and generate, when no other policy is defined. "
		+ "Validation policy set 30s fogin blocking time with 5 "
		+ "unsuccessful login attempts, minimum 8 char length passwords. "
		+ "Generate policy is configured to generate 8-12 char length passwords with "
		+ "2 lower, 2 upper, 2 number and 2 special chars.")
public class InitPasswordPolicyProcessor extends AbstractInitApplicationProcessor {

	public static final String PROCESSOR_NAME = "core-init-password-policy-processor";
	//
	@Autowired private IdmPasswordPolicyService passwordPolicyService;
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<ModuleDescriptorDto> event) {
		return super.conditional(event) 
				&& isInitDataEnabled()
				&& passwordPolicyService.count(null) == 0;
	}
	
	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		// create default password policy for validate
		IdmPasswordPolicyDto validatePolicy = new IdmPasswordPolicyDto();
		validatePolicy.setName("Validate password policy");
		validatePolicy.setDefaultPolicy(true);
		validatePolicy.setType(IdmPasswordPolicyType.VALIDATE);
		validatePolicy.setBlockLoginTime(30);
		validatePolicy.setMaxUnsuccessfulAttempts(5);
		validatePolicy.setMinPasswordLength(8);
		passwordPolicyService.save(validatePolicy);
		//
		// create default password policy for generate
		IdmPasswordPolicyDto passGenerate = new IdmPasswordPolicyDto();
		passGenerate.setName("Generate password policy");
		passGenerate.setDefaultPolicy(true);
		passGenerate.setType(IdmPasswordPolicyType.GENERATE);
		passGenerate.setMinLowerChar(2);
		passGenerate.setMinNumber(2);
		passGenerate.setMinSpecialChar(2);
		passGenerate.setMinUpperChar(2);
		passGenerate.setMinPasswordLength(8);
		passGenerate.setMaxPasswordLength(12);
		passwordPolicyService.save(passGenerate);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// before identity is created
		return CoreEvent.DEFAULT_ORDER - 150;
	}
}
