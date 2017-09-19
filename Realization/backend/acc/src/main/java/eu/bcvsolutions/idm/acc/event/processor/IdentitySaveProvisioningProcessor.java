package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Run provisioning after identity was saved.
 * 
 * @author Radek Tomiška
 *
 */
@Component("accIdentitySaveProcessor")
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Executes provisioning after identity is saved.")
public class IdentitySaveProvisioningProcessor extends AbstractEntityEventProcessor<IdmIdentityDto> {

	public static final String PROCESSOR_NAME = "identity-save-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentitySaveProvisioningProcessor.class);
	private ProvisioningService provisioningService;
	private final ApplicationContext applicationContext;
	
	@Autowired
	public IdentitySaveProvisioningProcessor(ApplicationContext applicationContext, IdmIdentityRepository identityRepository) {
		super(IdentityEventType.CREATE, IdentityEventType.UPDATE, CoreEventType.EAV_SAVE);
		//
		Assert.notNull(applicationContext);
		Assert.notNull(identityRepository);
		//
		this.applicationContext = applicationContext;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		Object skipProvisioning = event.getProperties().get(ProvisioningService.SKIP_PROVISIONING);
		
		if(skipProvisioning instanceof Boolean && (Boolean)skipProvisioning){
			return new DefaultEventResult<>(event, this);
		}
		doProvisioning(event.getContent());
		return new DefaultEventResult<>(event, this);
	}
	
	private void doProvisioning(IdmIdentityDto identity) {
		LOG.debug("Call provisioning for identity [{}]", identity.getUsername());
		getProvisioningService().doProvisioning(identity);
	}
	
	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}
	
	/**
	 * provisioningService has dependency everywhere - so we need lazy init ...
	 * 
	 * @return
	 */
	private ProvisioningService getProvisioningService() {
		if (provisioningService == null) {
			provisioningService = applicationContext.getBean(ProvisioningService.class);
		}
		return provisioningService;
	}
	
}