package eu.bcvsolutions.idm.core.model.service.impl;

import java.text.MessageFormat;
import java.util.Collection;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.base.Strings;

import eu.bcvsolutions.idm.core.api.domain.Loggable;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.model.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.filter.ConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;

/**
 * Default implementation of concept role request service
 * 
 * @author svandav
 *
 */
@Service("conceptRoleRequestService")
public class DefaultIdmConceptRoleRequestService
		extends AbstractReadWriteDtoService<IdmConceptRoleRequestDto, IdmConceptRoleRequest, ConceptRoleRequestFilter>
		implements IdmConceptRoleRequestService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(DefaultIdmConceptRoleRequestService.class);

	private final WorkflowProcessInstanceService workflowProcessInstanceService;

	@Autowired
	public DefaultIdmConceptRoleRequestService(
			AbstractEntityRepository<IdmConceptRoleRequest, ConceptRoleRequestFilter> repository, WorkflowProcessInstanceService workflowProcessInstanceService) {
		super(repository);

		Assert.notNull(workflowProcessInstanceService, "Workflow process instance service is required!");
		
		this.workflowProcessInstanceService = workflowProcessInstanceService;
	}

	@Override
	public IdmConceptRoleRequest toEntity(IdmConceptRoleRequestDto dto, IdmConceptRoleRequest entity) {
		// Set persisted value to read only properties
		// TODO: Create converter for skip fields mark as read only
		if (dto.getId() != null) {
			IdmConceptRoleRequestDto dtoPersisited = this.getDto(dto.getId());
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
