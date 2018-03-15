package eu.bcvsolutions.idm.acc.event.processor.contract;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;

/**
 * Identity contract provisioning super class
 * 
 * @author Radek Tomiška
 */
public abstract class AbstractIdentityContractProvisioningProcessor
		extends CoreEventProcessor<IdmIdentityContractDto> 
		implements IdentityContractProcessor {

	protected static final String PROPERTY_INCLUDE_SUBORDINATES = "includeSubordinates"; // configuration property
	protected static final String PROPERTY_PREVIOUS_SUBORDINATES = "idm:previous-subordinates"; // event property, contains Set<UUID>
	protected static final boolean DEFAULT_INCLUDE_SUBORDINATES = true;
	//
	@Autowired private IdmIdentityService identityService;
	
	public AbstractIdentityContractProvisioningProcessor(EventType... type) {
		super(type);
	}
	
	/**
	 * Execute provisioning for subordinates
	 * 
	 * @return
	 */
	protected boolean isIncludeSubordinates() {
		return getConfigurationBooleanValue(PROPERTY_INCLUDE_SUBORDINATES, DEFAULT_INCLUDE_SUBORDINATES);
	}
	
	/**
	 * Returns all identity's subordinates
	 * 
	 * @param identityId
	 * @return
	 */
	protected List<IdmIdentityDto> findAllSubordinates(UUID identityId) {
		if (identityId == null) {
			// just for sure - processor can be wrong configured (e.g. has to have order after identity is saved)
			return new ArrayList<>();
		}
		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setSubordinatesFor(identityId);
		return identityService.find(filter, null).getContent();
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> propertyNames =  super.getPropertyNames();
		propertyNames.add(PROPERTY_INCLUDE_SUBORDINATES);
		return propertyNames;
	}	
}