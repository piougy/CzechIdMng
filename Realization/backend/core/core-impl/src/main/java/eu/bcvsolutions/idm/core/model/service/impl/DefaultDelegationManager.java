package eu.bcvsolutions.idm.core.model.service.impl;

import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.DelegationTypeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDefinitionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmDelegationFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import eu.bcvsolutions.idm.core.api.service.DelegationManager;
import eu.bcvsolutions.idm.core.api.service.IdmDelegationService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.service.DelegationType;
import eu.bcvsolutions.idm.core.model.delegation.type.DefaultDelegationType;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessDefinitionService;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.activiti.bpmn.model.ValuedDataObject;
import org.apache.commons.collections.CollectionUtils;
import org.modelmapper.internal.util.Assert;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;

/**
 * Delegation manager
 *
 * @author Vít Švanda
 * @since 10.4.0
 *
 */
@Service("delegationManager")
public class DefaultDelegationManager implements DelegationManager {

	@Autowired
	private ApplicationContext context;
	@Lazy
	@Autowired
	private EnabledEvaluator enabledEvaluator;
	@Lazy
	@Autowired
	private WorkflowProcessDefinitionService processDefinitionService;
	@Lazy
	@Autowired
	private IdmDelegationService delegationService;

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultDelegationManager.class);

	@Override
	public List<IdmDelegationDefinitionDto> findDelegation(String type, UUID delegatorId, UUID delegatorContractId, BaseDto owner) {
		Assert.notNull(type, "Delegation type cannot be null!");
		Assert.notNull(delegatorId, "Delegator cannot be null!");

		DelegationType delegateType = this.getDelegateType(type);
		if (delegateType == null) {
			// Delegation type was not found (bad code or implementation
			// of delegatio type for this code missing ) -> throw exception.
			throw new ResultCodeException(CoreResultCode.DELEGATION_UNSUPPORTED_TYPE, ImmutableMap.of("type", type));
		}

		List<IdmDelegationDefinitionDto> definitions = delegateType.findDelegation(delegatorId, delegatorContractId, owner);

		if (CollectionUtils.isEmpty(definitions)) {
			if (DefaultDelegationType.NAME.equals(type)) {
				return null;
			}
			// Try to default delegation.
			DelegationType defaultDelegateType = this.getDelegateType(DefaultDelegationType.NAME);
			definitions = defaultDelegateType.findDelegation(delegatorId, delegatorContractId, owner);
			if (CollectionUtils.isEmpty(definitions)) {
				return null;
			}
		}

		definitions.forEach(definition -> {
			LOG.debug("Delegation definition found [{}] for type [{}] and delegator [{}]",
					definition.getId(), type, delegatorId);
		});

		return definitions;
	}

	@Override
	public IdmDelegationDto delegate(BaseDto owner, IdmDelegationDefinitionDto definition) {
		Assert.notNull(owner, "Delegation goal/owner cannot be null!");
		Assert.notNull(definition, "Delegation definition cannot be null!");

		DelegationType delegateType = this.getDelegateType(definition.getType());
		if (delegateType == null) {
			// Delegation type was not found (bad code or implementation
			// of delegation type for this code missing ) -> throw exception.
			throw new ResultCodeException(CoreResultCode.DELEGATION_UNSUPPORTED_TYPE, ImmutableMap.of("type", definition.getType()));
		}

		return delegateType.delegate(owner, definition);
	}

	@Override
	public List<DelegationType> getSupportedTypes() {
		return context
				.getBeansOfType(DelegationType.class)
				.values()
				.stream()
				.filter(enabledEvaluator::isEnabled)
				.sorted(Comparator.comparing(DelegationType::getOrder))
				.collect(Collectors.toList());
	}

	@Override
	public DelegationTypeDto convertDelegationTypeToDto(DelegationType delegationType) {
		DelegationTypeDto delegationTypeDto = new DelegationTypeDto();
		delegationTypeDto.setId(delegationType.getId());
		delegationTypeDto.setName(delegationType.getId());
		if (delegationType.getOwnerType() != null) {
			delegationTypeDto.setOwnerType(delegationType.getOwnerType().getCanonicalName());
		}
		delegationTypeDto.setModule(delegationType.getModule());
		delegationTypeDto.setSupportsDelegatorContract(delegationType.isSupportsDelegatorContract());
		//
		return delegationTypeDto;
	}

	@Override
	public DelegationType getDelegateType(String id) {
		return this.getSupportedTypes().stream()
				.filter(type -> type.getId().equals(id))
				.findFirst()
				.orElse(null);
	}

	@Override
	public String getProcessDelegationType(String definitionId) {
		Assert.notNull(definitionId, "Workflow definition ID cannot be null!");
		List<ValuedDataObject> dataObjects = processDefinitionService.getDataObjects(definitionId);

		if (dataObjects != null) {
			ValuedDataObject supportVariable = dataObjects.stream()
					.filter(dataObject -> WORKFLOW_DELEGATION_TYPE_KEY.equals(dataObject.getName()))
					.findFirst()
					.orElse(null);
			if (supportVariable != null) {
				Object value = supportVariable.getValue();
				if (value instanceof String) {
					return (String) value;
				}
			}
		}
		return null;
	}

	@Override
	public List<IdmDelegationDto> findDelegationForOwner(BaseDto owner, BasePermission... permission) {
		Assert.notNull(owner, "Owner cannot be null!");

		IdmDelegationFilter delegationFilter = new IdmDelegationFilter();
		delegationFilter.setOwnerId(DtoUtils.toUuid(owner.getId()));
		delegationFilter.setOwnerType(owner.getClass().getCanonicalName());

		return delegationService.find(delegationFilter, null, permission).getContent();
	}
}
