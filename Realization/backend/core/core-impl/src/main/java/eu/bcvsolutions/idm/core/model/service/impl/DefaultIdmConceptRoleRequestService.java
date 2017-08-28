package eu.bcvsolutions.idm.core.model.service.impl;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.google.common.base.Strings;

import eu.bcvsolutions.idm.core.api.domain.Loggable;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.ConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleRequest_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmConceptRoleRequestRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;

/**
 * Default implementation of concept role request service
 * 
 * @author svandav
 * @author Radek Tomiška
 */
@Service("conceptRoleRequestService")
public class DefaultIdmConceptRoleRequestService
		extends AbstractReadWriteDtoService<IdmConceptRoleRequestDto, IdmConceptRoleRequest, ConceptRoleRequestFilter>
		implements IdmConceptRoleRequestService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmConceptRoleRequestService.class);
	private final IdmConceptRoleRequestRepository repository;
	private final WorkflowProcessInstanceService workflowProcessInstanceService;

	@Autowired
	public DefaultIdmConceptRoleRequestService(
			IdmConceptRoleRequestRepository repository, 
			WorkflowProcessInstanceService workflowProcessInstanceService) {
		super(repository);
		//
		Assert.notNull(workflowProcessInstanceService, "Workflow process instance service is required!");
		//
		this.repository = repository;
		this.workflowProcessInstanceService = workflowProcessInstanceService;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		// secured internally by role requests
		return null;
	}
	
	@Override
	public IdmConceptRoleRequest checkAccess(IdmConceptRoleRequest entity, BasePermission... permission) {
		if (entity == null) {
			// nothing to check
			return null;
		}
		if (!ObjectUtils.isEmpty(permission) && !getAuthorizationManager().evaluate(entity.getRoleRequest(), permission)) {
			throw new ForbiddenEntityException(entity.getId());
		}
		return entity;
	}
	
	@Override
	protected IdmConceptRoleRequestDto toDto(IdmConceptRoleRequest entity, IdmConceptRoleRequestDto dto) {
		IdmConceptRoleRequestDto dtoResult = super.toDto(entity, dto);
		// Contract from identity role has higher priority then contract ID in concept role
		if(entity != null && entity.getIdentityRole() != null){
			dtoResult.setIdentityContract(entity.getIdentityRole().getIdentityContract().getId());
		}
		return dtoResult;
	}

	@Override
	@Transactional(readOnly = true)
	public IdmConceptRoleRequest toEntity(IdmConceptRoleRequestDto dto, IdmConceptRoleRequest entity) {
		// Set persisted value to read only properties
		// TODO: Create converter for skip fields mark as read only
		if (dto.getId() != null) {
			IdmConceptRoleRequestDto dtoPersisited = this.get(dto.getId());
			if (dto.getState() == null) {
				dto.setState(dtoPersisited.getState());
			}
			if (dto.getLog() == null) {
				dto.setLog(dtoPersisited.getLog());
			}

			if (dto.getWfProcessId() == null) {
				dto.setWfProcessId(dtoPersisited.getWfProcessId());
			}
		} else {
			dto.setState(RoleRequestState.CONCEPT);
		}
		return super.toEntity(dto, entity);
		 
	}
	
	@Override
	public void delete(IdmConceptRoleRequestDto dto, BasePermission... permission) {
		
		if(!Strings.isNullOrEmpty(dto.getWfProcessId())){
			WorkflowFilterDto filter = new WorkflowFilterDto();
			filter.setProcessInstanceId(dto.getWfProcessId());
			
			Collection<WorkflowProcessInstanceDto> resources = workflowProcessInstanceService.searchInternal(filter, false).getResources();
			if(resources.isEmpty()){
				// Process with this ID not exist ... maybe was ended 
				this.addToLog(dto,
						MessageFormat.format(
								"Workflow process with ID [{0}] was not deleted, because was not found. Maybe was ended before.",
								dto.getWfProcessId()));
				return;
			}
			workflowProcessInstanceService.delete(dto.getWfProcessId(), "Role concept use this WF, was deleted. This WF was deleted too.");
			this.addToLog(dto,
					MessageFormat.format(
							"Workflow process with ID [{0}] was deleted, because this concept is deleted/canceled",
							dto.getWfProcessId()));
		}
			
		super.delete(dto);
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmConceptRoleRequest> root, CriteriaQuery<?> query, CriteriaBuilder builder, ConceptRoleRequestFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		if (filter.getRoleRequestId() != null) {
			predicates.add(builder.equal(root.get(IdmConceptRoleRequest_.roleRequest).get(IdmRoleRequest_.id), filter.getRoleRequestId()));
		}
		if (filter.getIdentityRoleId() != null) {
			predicates.add(builder.equal(root.get(IdmConceptRoleRequest_.identityRole).get(IdmIdentityRole_.id), filter.getIdentityRoleId()));
		}
		if (filter.getRoleId() != null) {
			predicates.add(builder.equal(root.get(IdmConceptRoleRequest_.role).get(IdmRole_.id), filter.getRoleId()));
		}
		if (filter.getIdentityContractId() != null) {
			predicates.add(builder.equal(root.get(IdmConceptRoleRequest_.identityContract).get(IdmIdentityContract_.id), filter.getIdentityContractId()));
		}
		if (filter.getRoleTreeNodeId() != null) {
			predicates.add(builder.equal(root.get(IdmConceptRoleRequest_.roleTreeNode).get(IdmRoleTreeNode_.id), filter.getRoleTreeNodeId()));
		}
		if (filter.getOperation() != null) {
			predicates.add(builder.equal(root.get(IdmConceptRoleRequest_.operation), filter.getOperation()));
		}
		if (filter.getState() != null) {
			predicates.add(builder.equal(root.get(IdmConceptRoleRequest_.state), filter.getState()));
		}
		//
		return predicates;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmConceptRoleRequestDto> findAllByRoleRequest(UUID roleRequestId) {
		return toDtos(repository.findAllByRoleRequest_Id(roleRequestId), false);
	}

	@Override
	public void addToLog(Loggable logItem, String text) {
		StringBuilder sb = new StringBuilder();
		sb.append(DateTime.now());
		sb.append(": ");
		sb.append(text);
		text = sb.toString();
		logItem.addToLog(text);
		LOG.info(text);
	}
}
