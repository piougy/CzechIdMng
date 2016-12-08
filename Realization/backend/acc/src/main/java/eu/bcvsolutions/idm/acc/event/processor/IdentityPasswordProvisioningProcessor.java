package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.processor.IdentityPasswordProcessor;
import eu.bcvsolutions.idm.security.api.domain.Enabled;

/**
 * Identity's password provisioning
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Enabled(AccModuleDescriptor.MODULE_ID)
public class IdentityPasswordProvisioningProcessor extends AbstractEntityEventProcessor<IdmIdentity> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityPasswordProvisioningProcessor.class);
	private SysProvisioningService provisioningService;
	private final ApplicationContext applicationContext;
	
	@Autowired
	public IdentityPasswordProvisioningProcessor(ApplicationContext applicationContext) {
		super(IdentityEventType.PASSWORD);
		//
		Assert.notNull(applicationContext);
		//
		this.applicationContext = applicationContext;
	}

	@Override
	public EventResult<IdmIdentity> process(EntityEvent<IdmIdentity> event) {
		IdmIdentity identity = event.getContent();
		PasswordChangeDto passwordChangeDto = (PasswordChangeDto) event.getProperties().get(IdentityPasswordProcessor.PROPERTY_PASSWORD_CHANGE_DTO);
		Assert.notNull(passwordChangeDto);
		//
		LOG.debug("Call provisioning for idnetity password [{}]", event.getContent().getUsername());
		getProvisioningService().changePassword(identity, passwordChangeDto);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * provisioningService has dependency everywhere - so we need lazy init ...
	 * 
	 * @return
	 */
	private SysProvisioningService getProvisioningService() {
		if (provisioningService == null) {
			provisioningService = applicationContext.getBean(SysProvisioningService.class);
		}
		return provisioningService;
	}

	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}
}