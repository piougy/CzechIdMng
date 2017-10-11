package eu.bcvsolutions.idm.acc.event.processor.contract;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;

/**
 * Identity Ccontract provisioning super class
 * 
 * @author Radek Tomiška
 */
public abstract class AbstractIdentityContractProvisioningProcessor
		extends CoreEventProcessor<IdmIdentityContractDto> 
		implements IdentityContractProcessor {

	protected static final String PROPERTY_INCLUDE_SUBORDINATES = "includeSubordinates";
	protected static final String PROPERTY_PREVIOUS_SUBORDINATES = "idm:previous-subordinates"; // contains Set<UUID>
	protected static final boolean DEFAULT_INCLUDE_SUBORDINATES = true;
	//
	@Autowired private IdmIdentityService identityService;
	
	public AbstractIdentityContractProvisioningProcessor() {
		super(CoreEventType.CREATE, CoreEventType.UPDATE, CoreEventType.DELETE, CoreEventType.EAV_SAVE);
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