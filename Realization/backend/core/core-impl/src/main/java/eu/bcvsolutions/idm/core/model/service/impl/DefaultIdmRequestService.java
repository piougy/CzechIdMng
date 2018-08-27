package eu.bcvsolutions.idm.core.model.service.impl;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RequestState;
import eu.bcvsolutions.idm.core.api.dto.AbstractRequestDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestItemDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestItemFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmRequestItemService;
import eu.bcvsolutions.idm.core.api.service.IdmRequestService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRequest;
import eu.bcvsolutions.idm.core.model.entity.IdmRequest_;
import eu.bcvsolutions.idm.core.model.repository.IdmRequestRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;

/**
 * Default implementation of universal request service
 * 
 * @author svandav
 *
 */
@Service("requestService")
public class DefaultIdmRequestService extends
		AbstractReadWriteDtoService<IdmRequestDto, IdmRequest, IdmRequestFilter>
		implements IdmRequestService {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmRequestService.class);

	@Autowired
	private WorkflowProcessInstanceService workflowProcessInstanceService;
	@Autowired
	private WorkflowHistoricProcessInstanceService workflowHistoricProcessInstanceService;
	@Autowired
	private IdmRequestItemService requestItemService;
	@Autowired
	private LookupService lookupService;

	@Autowired
	public DefaultIdmRequestService(IdmRequestRepository repository) {
		super(repository);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.REQUEST, getEntityClass());
	}

	@Override
	public IdmRequestDto toDto(IdmRequest entity, IdmRequestDto dto) {
		IdmRequestDto requestDto = super.toDto(entity, dto);

		if (requestDto != null && requestDto.getWfProcessId() != null) {
			WorkflowHistoricProcessInstanceDto processDto = workflowHistoricProcessInstanceService.get(requestDto.getWfProcessId());
			// TODO: create trimmed variant in workflow process instance service
			if (processDto != null) {
				processDto.setProcessVariables(null);
			}
			requestDto.getEmbedded().put(AbstractRequestDto.WF_PROCESS_FIELD, processDto);
		}
		
		// Load and add owner DTO to embedded. Prevents of many requests from FE.
		if (requestDto != null && requestDto.getOwnerId() != null && requestDto.getOwnerType() != null) {
			try {
				@SuppressWarnings("unchecked")
				BaseDto lookupDto = lookupService.lookupDto(
						(Class<? extends Identifiable>) Class.forName(requestDto.getOwnerType()),
						requestDto.getOwnerId());
				if (lookupDto == null) {
					// Entity was not found ... maybe is deleted
					LOG.warn(MessageFormat.format("Owner [{0}, {1}] not found for request {2}.",
							requestDto.getOwnerType(), requestDto.getOwnerId(), requestDto.getId()));
				}
				requestDto.getEmbedded().put(IdmRequestDto.OWNER_FIELD, lookupDto);
			} catch (ClassNotFoundException e) {
				// Only print warning
				LOG.warn(MessageFormat.format("Class not found for request {0}.", requestDto.getId()), e);
			}
		}

		return requestDto;
	}
	
	@Override
	public IdmRequestDto saveInternal(IdmRequestDto dto) {
		if(dto != null && RequestState.DISAPPROVED == dto.getState()) {
			dto.setResult(new OperationResultDto(OperationState.NOT_EXECUTED));
		}
		return super.saveInternal(dto);
	}

	@Override
	@Transactional
	public void deleteInternal(IdmRequestDto dto) {
		// Stop connected WF process
		cancelWF(dto);

		// We have to delete all items for this request
		if (dto.getId() != null) {
			IdmRequestItemFilter ruleFilter = new IdmRequestItemFilter();
			ruleFilter.setRequestId(dto.getId());
			List<IdmRequestItemDto> items = requestItemService
					.find(ruleFilter, null).getContent();
			items.forEach(item -> {
				requestItemService.delete(item);
			});
		}
		super.deleteInternal(dto);
	}

	@Override
	protected IdmRequest toEntity(IdmRequestDto dto, IdmRequest entity) {

		if (this.isNew(dto)) { 
			dto.setResult(new OperationResultDto(OperationState.CREATED));
			dto.setState(RequestState.CONCEPT);
		}else if(dto.getResult() == null) {
			IdmRequestDto persistedDto = this.get(dto.getId());
			dto.setResult(persistedDto.getResult());
		}
		IdmRequest requestEntity = super.toEntity(dto, entity);

		return requestEntity;
	}
	

	@Override
	protected List<Predicate> toPredicates(Root<IdmRequest> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, IdmRequestFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);

		// States
		List<RequestState> states = filter.getStates();
		if (!states.isEmpty()) {
			predicates.add(root.get(IdmRequest_.state).in(states));
		}
		return predicates;
	}

	/**
	 * Cancel unfinished workflow process for this automatic role.
	 * 
	 * @param dto
	 */
	private void cancelWF(IdmRequestDto dto) {
		if (!Strings.isNullOrEmpty(dto.getWfProcessId())) {
			WorkflowFilterDto filter = new WorkflowFilterDto();
			filter.setProcessInstanceId(dto.getWfProcessId());

			Collection<WorkflowProcessInstanceDto> resources = workflowProcessInstanceService.find(filter, null)
					.getContent();
			if (resources.isEmpty()) {
				// Process with this ID not exist ... maybe was ended
				return;
			}

			workflowProcessInstanceService.delete(dto.getWfProcessId(),
					"Role request use this WF, was deleted. This WF was deleted too.");
		}
	}

}
