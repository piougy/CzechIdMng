package eu.bcvsolutions.idm.core.model.event.processor.contract;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormProjectionDto;
import eu.bcvsolutions.idm.core.eav.service.impl.IdentityFormProjectionRoute;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.event.ContractGuaranteeEvent;
import eu.bcvsolutions.idm.core.model.event.ContractGuaranteeEvent.ContractGuaranteeEventType;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Add currently logged user as identity contract guarantee, when new contract is created with projection with enabled direct guarantees.
 * 
 * @see IdentityFormProjectionRoute#PARAMETER_SET_CONTRACT_GUARANTEE
 * @author Radek Tomiška
 * @since 11.0.0
 */
@Component(IdentityContractAddGuaranteeByProjectionProcessor.PROCESSOR_NAME)
@Description("Add currently logged user as identity contract guarantee, "
		+ "when new contract is created with projection with enabled direct guarantees.")
public class IdentityContractAddGuaranteeByProjectionProcessor 
		extends CoreEventProcessor<IdmIdentityContractDto>
		implements IdentityContractProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityContractAddGuaranteeByProjectionProcessor.class);
	public static final String PROCESSOR_NAME = "core-identity-contract-add-guarantee-by-projection-processor";
	//
	@Autowired private LookupService lookupService;
	@Autowired private SecurityService securityService;
	@Autowired private IdmContractGuaranteeService guaranteeService;
	@Autowired private IdmCacheManager cacheManager;
	
	public IdentityContractAddGuaranteeByProjectionProcessor() {
		super(CoreEventType.CREATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmIdentityContractDto> event) {
		if (!super.conditional(event)) {
			return false;
		}
		// check user is logged => from gui
		if (securityService.getCurrentId() == null) {
			LOG.debug("User is not logged in, contract will NOT be processed (e.g. from synchronization).");
			return false;
		}
		// check configured projection is used
		IdmIdentityContractDto content = event.getContent();
		IdmIdentityDto identity = lookupService.lookupEmbeddedDto(content, IdmIdentityContract_.identity);
		UUID formProjectionId = identity.getFormProjection();
		if (formProjectionId == null) {
			LOG.debug("Contract is created without projection, contract will NOT be processer.");
			return false;
		}
		//
		// projection flag is set
		IdmFormProjectionDto formProjection = lookupService.lookupEmbeddedDto(identity, IdmIdentity_.formProjection);
		if (formProjection.getProperties().getBooleanValue(IdentityFormProjectionRoute.PARAMETER_SET_CONTRACT_GUARANTEE)) {
			LOG.debug("Contract is created with projection [{}], add direct guarantee for newly created contract is enabled.",
					formProjection.getId());
			return true;
		}
		//
		LOG.debug("Contract is created with projection [{}], "
				+ "add direct guarantee for newly created contract is NOT enabled, "
				+ "contract will NOT be processed.",
				formProjection.getId());
		return false;
	}
	
	@Override
	public EventResult<IdmIdentityContractDto> process(EntityEvent<IdmIdentityContractDto> event) {
		IdmIdentityContractDto contract = event.getContent();
		//
		// create contract guarantee
		IdmContractGuaranteeDto guarantee = new IdmContractGuaranteeDto();
		guarantee.setIdentityContract(contract.getId());
		guarantee.setGuarantee(securityService.getCurrentId());
		// preserve event chain (and priority)
		ContractGuaranteeEvent guaranteeEvent = new ContractGuaranteeEvent(ContractGuaranteeEventType.CREATE, guarantee);
		guaranteeService.publish(guaranteeEvent, event);
		// evict authorization manager caches for token identity only
		cacheManager.evictValue(AuthorizationManager.PERMISSION_CACHE_NAME, securityService.getCurrentId());
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// after save
		return 150;
	}

}
