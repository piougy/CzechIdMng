package eu.bcvsolutions.idm.acc.event.processor.contract;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Identity Ccontract provisioning super class
 * 
 * @author Radek Tomiška
 */
public abstract class AbstractIdentityContractProvisioningProcessor extends AbstractEntityEventProcessor<IdmIdentityContractDto> {

	protected static final String PROPERTY_INCLUDE_SUBORDINATES = "includeSubordinates";
	protected static final String PROPERTY_PREVIOUS_SUBORDINATES = "idm:previous-subordinates"; // contains Set<UUID>
	protected static final boolean DEFAULT_INCLUDE_SUBORDINATES = true;
	//
	@Autowired private FilterManager filterManager;
	
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
	protected List<IdmIdentity> findAllSubordinates(UUID identityId) {
		IdentityFilter filter = new IdentityFilter();
		filter.setSubordinatesFor(identityId);
		return filterManager.getBuilder(IdmIdentity.class, IdentityFilter.PARAMETER_SUBORDINATES_FOR).find(filter, null).getContent();
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> propertyNames =  super.getPropertyNames();
		propertyNames.add(PROPERTY_INCLUDE_SUBORDINATES);
		return propertyNames;
	}	
}