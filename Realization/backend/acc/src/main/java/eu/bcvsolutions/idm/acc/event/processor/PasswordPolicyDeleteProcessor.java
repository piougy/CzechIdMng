package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.repository.SysSystemRepository;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.PasswordPolicyEvent.PasswordPolicyEvenType;
import eu.bcvsolutions.idm.core.model.repository.IdmPasswordPolicyRepository;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

@Component("accPasswordPolicyDeleteProcessor")
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Remove password policies from system after delete.")
public class PasswordPolicyDeleteProcessor extends CoreEventProcessor<IdmPasswordPolicyDto> {
	
	public static final String PROCESSOR_NAME = "password-policy-delete-processor";
	public final SysSystemRepository systemRepository;
	private final IdmPasswordPolicyRepository passwordPolicyRepository;
	
	@Autowired
	public PasswordPolicyDeleteProcessor(SysSystemRepository systemRepository,
			IdmPasswordPolicyRepository passwordPolicyRepository) {
		super(PasswordPolicyEvenType.DELETE);
		//
		Assert.notNull(systemRepository);
		Assert.notNull(passwordPolicyRepository);
		//
		this.systemRepository = systemRepository;
		this.passwordPolicyRepository = passwordPolicyRepository;
	}
	
	@Override
	public EventResult<IdmPasswordPolicyDto> process(EntityEvent<IdmPasswordPolicyDto> event) {
		// cascade set to null all references in sysSystem to remove password policy
		IdmPasswordPolicyDto dto = event.getContent();
		// remove references to password policy 
		// this information it will not be saved in audit
		// TODO: remove repository after refactor system to DTO
		systemRepository.clearPasswordPolicy(passwordPolicyRepository.findOne(dto.getId()));
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
