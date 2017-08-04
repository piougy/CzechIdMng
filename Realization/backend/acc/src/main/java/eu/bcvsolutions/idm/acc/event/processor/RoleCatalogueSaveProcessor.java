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
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.event.RoleCatalogueEvent.RoleCatalogueEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleCatalogueRepository;

/**
 * Persists role catalogue items.
 * 
 * @author Svanda
 */
@Component("accRoleCatalogueSaveProcessor")
@Description("Persists role catalogue items.")
public class RoleCatalogueSaveProcessor extends CoreEventProcessor<IdmRoleCatalogueDto> {

	private static final String PROCESSOR_NAME = "role-catalogue-save-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RoleCatalogueSaveProcessor.class);
	private ProvisioningService provisioningService;
	private final ApplicationContext applicationContext;
	private final IdmRoleCatalogueRepository catalogueRepository;
	
	@Autowired
	public RoleCatalogueSaveProcessor(ApplicationContext applicationContext,
			IdmRoleCatalogueRepository catalogueRepository) {
		super(RoleCatalogueEventType.CREATE, RoleCatalogueEventType.UPDATE);
		//
		Assert.notNull(applicationContext);
		Assert.notNull(catalogueRepository);
		//
		this.applicationContext = applicationContext;
		this.catalogueRepository = catalogueRepository;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleCatalogueDto> process(EntityEvent<IdmRoleCatalogueDto> event) {
		Object breakProvisioning = event.getProperties().get(ProvisioningService.SKIP_PROVISIONING);
		
		if(breakProvisioning instanceof Boolean && (Boolean)breakProvisioning){
			return new DefaultEventResult<>(event, this);
		}
		
		doProvisioning(catalogueRepository.findOne(event.getContent().getId()));
		return new DefaultEventResult<>(event, this);
	}
	
	private void doProvisioning(IdmRoleCatalogue node) {
		LOG.debug("Call account managment (create accounts for all systems) for role catalogue [{}]", node.getCode());
		getProvisioningService().createAccountsForAllSystems(node);
		LOG.debug("Call provisioning for role catalogue [{}]", node.getCode());
		getProvisioningService().doProvisioning(node);
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
