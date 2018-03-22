package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.RoleCatalogueEvent.RoleCatalogueEventType;

/**
 * Executes provisioning after role catalogue was changed.
 * 
 * @author Svanda
 * @author Radek Tomiška
 */
@Component("accRoleCatalogueSaveProcessor")
@Description("Executes provisioning after role catalogue was changed.")
public class RoleCatalogueSaveProcessor extends CoreEventProcessor<IdmRoleCatalogueDto> {

	private static final String PROCESSOR_NAME = "role-catalogue-save-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RoleCatalogueSaveProcessor.class);
	private ProvisioningService provisioningService;
	private final ApplicationContext applicationContext;
	
	@Autowired
	public RoleCatalogueSaveProcessor(ApplicationContext applicationContext) {
		super(RoleCatalogueEventType.NOTIFY);
		//
		Assert.notNull(applicationContext);
		//
		this.applicationContext = applicationContext;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmRoleCatalogueDto> event) {
		// Skip provisioning
		return !this.getBooleanProperty(ProvisioningService.SKIP_PROVISIONING, event.getProperties());
	}

	@Override
	public EventResult<IdmRoleCatalogueDto> process(EntityEvent<IdmRoleCatalogueDto> event) {		
		doProvisioning(event.getContent());
		//
		return new DefaultEventResult<>(event, this);
	}
	
	private void doProvisioning(IdmRoleCatalogueDto catalogue) {
		LOG.debug("Call account managment (create accounts for all systems) for role catalogue [{}]", catalogue.getCode());
		getProvisioningService().accountManagement(catalogue);
		LOG.debug("Call provisioning for role catalogue [{}]", catalogue.getCode());
		getProvisioningService().doProvisioning(catalogue);
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
