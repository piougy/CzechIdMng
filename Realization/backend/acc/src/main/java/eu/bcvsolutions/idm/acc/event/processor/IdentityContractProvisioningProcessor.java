package eu.bcvsolutions.idm.acc.event.processor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Executes provisioning after identity contract is saved or deleted. 
 * Executes provisioning for all contract's subordinates (newly added and previous) - depends on configuration property.
 * 
 * @author Radek Tomiška
 */
@Component
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Executes provisioing after identity contract is saved or deleted.")
public class IdentityContractProvisioningProcessor extends AbstractEntityEventProcessor<IdmIdentityContractDto> {

	public static final String PROCESSOR_NAME = "identity-contract-provisioning-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityContractProvisioningProcessor.class);
	protected static final String PROPERTY_INCLUDE_SUBORDINATES = "includeSubordinates";
	protected static final String PROPERTY_PREVIOUS_SUBORDINATES = "idm:previous-subordinates"; // contains Set<UUID>
	protected static final boolean DEFAULT_INCLUDE_SUBORDINATES = true;
	//
	@Autowired private ProvisioningService provisioningService;
	@Autowired private LookupService lookupService;
	@Autowired private FilterManager filterManager;
	
	public IdentityContractProvisioningProcessor() {
		super(CoreEventType.CREATE, CoreEventType.UPDATE, CoreEventType.DELETE, CoreEventType.EAV_SAVE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	@SuppressWarnings("unchecked")
	public EventResult<IdmIdentityContractDto> process(EntityEvent<IdmIdentityContractDto> event) {
		IdmIdentity identity = (IdmIdentity) lookupService.lookupEntity(IdmIdentity.class, event.getContent().getIdentity());
		LOG.debug("Call provisioning for identity [{}]", identity.getUsername());
		provisioningService.doProvisioning(identity);
		//
		// execute provisioning for all subordinates by given contract
		if (isIncludeSubordinates()) {
			Set<UUID> originalSubordinates = (Set<UUID>) event.getProperties().get(PROPERTY_PREVIOUS_SUBORDINATES);
			if (CollectionUtils.isNotEmpty(originalSubordinates)) {
				findAllSubordinates(identity.getId())
					.forEach(subordinate -> {
						if (originalSubordinates.contains(subordinate.getId())) {
							originalSubordinates.remove(subordinate.getId());
						} else {
							LOG.debug("Call provisioning for identity's [{}] newly assigned subordinate [{}]", identity.getUsername(), subordinate.getUsername());
							provisioningService.doProvisioning(subordinate);
						}
					});
				originalSubordinates.forEach(originalSubordinateId -> {
					IdmIdentity originalSubordinate = (IdmIdentity) lookupService.lookupEntity(IdmIdentity.class, originalSubordinateId);
					LOG.debug("Call provisioning for identity's [{}] previous subordinate [{}]", identity.getUsername(), originalSubordinate.getUsername());
					provisioningService.doProvisioning(originalSubordinate);
				});
			}
		}	
		return new DefaultEventResult<>(event, this);
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
	
	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}	
}