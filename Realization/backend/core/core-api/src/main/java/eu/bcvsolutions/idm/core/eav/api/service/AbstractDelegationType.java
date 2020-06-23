package eu.bcvsolutions.idm.core.eav.api.service;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDefinitionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmDelegationDefinitionFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmDelegationFilter;
import eu.bcvsolutions.idm.core.api.service.IdmDelegationDefinitionService;
import eu.bcvsolutions.idm.core.api.service.IdmDelegationService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.modelmapper.internal.util.Assert;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract delegation type.
 *
 * @author Vít Švanda
 * @since 10.4.0
 */
public abstract class AbstractDelegationType implements
		DelegationType,
		BeanNameAware {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractDelegationType.class);
	
	@Autowired
	private IdmDelegationDefinitionService delegationDefinitionService;
	@Autowired
	private IdmDelegationService delegationService;

	private String beanName; // spring bean name - used as id

	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}

	@Override
	public String getId() {
		return beanName;
	}

	@Override
	public boolean isCustomeDelegation() {
		return false;
	}

	@Override
	public List<IdmDelegationDefinitionDto> findDelegation(UUID delegatorId, UUID delegatorContractId, BaseDto owner) {
		IdmDelegationDefinitionFilter definitionFilter = new IdmDelegationDefinitionFilter();
		definitionFilter.setValid(Boolean.TRUE);
		definitionFilter.setType(this.getId());
		definitionFilter.setDelegatorId(delegatorId);
		if (this.isSupportsDelegatorContract()) {
			Assert.notNull(delegatorContractId, "Delegator contract cannot be null for this delegate type!");
			definitionFilter.setDelegatorContractId(delegatorContractId);
		}

		return delegationDefinitionService.find(definitionFilter, null).getContent()
				.stream()
				.sorted(Comparator.comparing(IdmDelegationDefinitionDto::getDelegate))
				.sorted(Comparator.comparing(IdmDelegationDefinitionDto::getValidTill,
						Comparator.nullsFirst(Comparator.naturalOrder()))).collect(Collectors.toList());
	}

	@Override
	public IdmDelegationDto delegate(BaseDto owner, IdmDelegationDefinitionDto definition) {
		IdmDelegationFilter delegationFilter = new IdmDelegationFilter();
		delegationFilter.setOwnerId(DtoUtils.toUuid(owner.getId()));
		delegationFilter.setOwnerType(owner.getClass().getCanonicalName());
		delegationFilter.setDelegationDefinitionId(definition.getId());

		// Check if same delegation already exists.
		IdmDelegationDto delegation = delegationService.find(delegationFilter, null).getContent()
				.stream()
				.findFirst()
				.orElse(null);
		if (delegation != null) {
			LOG.debug("Delegation [{}] for definition [{}] and owner [{}],[{}] already exists.",
					delegation.getId(), definition.getId(), owner.getId(), owner.getClass().getSimpleName());
			return delegation;
		}

		delegation = new IdmDelegationDto();
		delegation.setOwnerState(new OperationResultDto(OperationState.RUNNING));
		delegation.setDefinition(definition.getId());
		delegation.setOwnerId(DtoUtils.toUuid(owner.getId()));
		delegation.setOwnerType(owner.getClass().getCanonicalName());

		return delegationService.save(delegation);
	}

}
