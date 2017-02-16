package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.repository.SysSystemRepository;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.core.model.event.PasswordPolicyEvent.PasswordPolicyEvenType;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

@Component("accPasswordPolicyDeleteProcessor")
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Remove password policies from system after delete.")
public class PasswordPolicyDeleteProcessor extends AbstractEntityEventProcessor<IdmPasswordPolicy> {
	
	public static final String PROCESSOR_NAME = "password-policy-delete-processor";
	public final SysSystemRepository systemRepository;
	
	@Autowired
	public PasswordPolicyDeleteProcessor(SysSystemRepository systemRepository) {
		super(PasswordPolicyEvenType.DELETE);
		//
		Assert.notNull(systemRepository);
		//
		this.systemRepository = systemRepository;
	}
	
	@Override
	public EventResult<IdmPasswordPolicy> process(EntityEvent<IdmPasswordPolicy> event) {
		// cascade set to null all references in sysSystem to remove password policy
		IdmPasswordPolicy entity = event.getContent();
		// remove references to password policy 
		// this information it will not be saved in audit
		systemRepository.clearPasswordPolicy(entity);
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
