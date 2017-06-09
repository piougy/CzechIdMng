package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.filter.RoleCatalogueAccountFilter;
import eu.bcvsolutions.idm.acc.service.api.AccRoleCatalogueAccountService;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.RoleCatalogueEvent.RoleCatalogueEventType;

/**
 * Deletes role catalogue items.
 * 
 * @author Radek Tomiška
 */
@Component("accRoleCatalogueDeleteProcessor")
@Description("Ensures referential integrity. Cannot be disabled.")
public class RoleCatalogueDeleteProcessor extends CoreEventProcessor<IdmRoleCatalogueDto> {

	private static final String PROCESSOR_NAME = "role-catalogue-delete-processor";
	private final AccRoleCatalogueAccountService catalogueAccountService;
	
	@Autowired
	public RoleCatalogueDeleteProcessor(AccRoleCatalogueAccountService service) {
		super(RoleCatalogueEventType.DELETE);
		//
		Assert.notNull(service);
		//
		this.catalogueAccountService = service;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleCatalogueDto> process(EntityEvent<IdmRoleCatalogueDto> event) {
		RoleCatalogueAccountFilter filter = new RoleCatalogueAccountFilter();
		filter.setEntityId(event.getContent().getId());
		catalogueAccountService.find(filter, null).forEach(treeAccount -> {
			catalogueAccountService.delete(treeAccount);
		});
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// right now before entity delete
		return CoreEvent.DEFAULT_ORDER - 1;
	}
	
	@Override
	public boolean isDisableable() {
		return false;
	}

}
