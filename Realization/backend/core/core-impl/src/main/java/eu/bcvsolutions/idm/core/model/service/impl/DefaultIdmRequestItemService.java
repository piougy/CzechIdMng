package eu.bcvsolutions.idm.core.model.service.impl;

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

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestItemDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestItemFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.IdmRequestItemService;
import eu.bcvsolutions.idm.core.api.service.RequestManager;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRequestItem;
import eu.bcvsolutions.idm.core.model.entity.IdmRequestItem_;
import eu.bcvsolutions.idm.core.model.repository.IdmRequestItemRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;

/**
 * Default implementation of request's item service
 * 
 * @author svandav
 *
 */
@Service("requestItemService")
public class DefaultIdmRequestItemService extends
		AbstractReadWriteDtoService<IdmRequestItemDto, IdmRequestItem, IdmRequestItemFilter>
		implements IdmRequestItemService {

	@Autowired
	private ConfidentialStorage confidentialStorage;
	@Autowired
	private WorkflowProcessInstanceService workflowProcessInstanceService;
	
	@Autowired
	public DefaultIdmRequestItemService(IdmRequestItemRepository repository) {
		super(repository);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.REQUESTITEM, getEntityClass());
	}

	@Override
	public IdmRequestItemDto toDto(IdmRequestItem entity, IdmRequestItemDto dto) {
		IdmRequestItemDto requestDto = super.toDto(entity, dto);

		return requestDto;
	}

	@Override
	@Transactional
	public void deleteInternal(IdmRequestItemDto dto) {

		// We try to find value in the confidential storage and delete it
		if (dto.getId() != null) {
			String storageKey = RequestManager.getConfidentialStorageKey(dto.getId());
			confidentialStorage.delete(dto, storageKey);
		}
		super.deleteInternal(dto);
	}
	
	@Override
	@Transactional
	public IdmRequestItemDto cancel(IdmRequestItemDto dto) {
		cancelWF(dto);
		dto.setState(RequestState.CANCELED);
		dto.setResult(new OperationResultDto(OperationState.CANCELED));
		return this.save(dto);
	}

	@Override
	protected IdmRequestItem toEntity(IdmRequestItemDto dto, IdmRequestItem entity) {

		if (this.isNew(dto)) { 
			dto.setResult(new OperationResultDto(OperationState.CREATED));
		}
		IdmRequestItem requestEntity = super.toEntity(dto, entity);

		return requestEntity;
	}
	

	@Override
	protected List<Predicate> toPredicates(Root<IdmRequestItem> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, IdmRequestItemFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		
		if (filter.getRequestId() != null) {
			predicates.add(builder.equal(
					root.get(IdmRequestItem_.request).get(IdmRequestItem_.id),
					filter.getRequestId()));
		}
		if (filter.getOwnerId() != null) {
			predicates.add(builder.equal(
					root.get(IdmRequestItem_.ownerId),
					filter.getOwnerId()));
		}
		if (filter.getOwnerType() != null) {
			predicates.add(builder.equal(
					root.get(IdmRequestItem_.ownerType),
					filter.getOwnerType()));
		}
		if (filter.getOperationType() != null) {
			predicates.add(builder.equal(
					root.get(IdmRequestItem_.operation),
					filter.getOperationType()));
		}

		return predicates;
	}
	
	/**
	 * Cancel unfinished workflow process for this automatic role.
	 *
	 * @param dto
	 */
	private void cancelWF(IdmRequestItemDto dto) {
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
					"Request item was canceled.");
		}
	}


}
