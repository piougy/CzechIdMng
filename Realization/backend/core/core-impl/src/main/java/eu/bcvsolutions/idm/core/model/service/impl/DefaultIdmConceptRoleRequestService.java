package eu.bcvsolutions.idm.core.model.service.impl;

import java.text.MessageFormat;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.Loggable;
import eu.bcvsolutions.idm.core.api.exception.RoleRequestException;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.model.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.filter.ConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleRequestFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
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
	public IdmConceptRoleRequestDto save(IdmConceptRoleRequestDto concept) {

		// get contract dto from embedded map
		IdmIdentityContractDto contract = (IdmIdentityContractDto) concept.getEmbedded()
				.get(IdmConceptRoleRequestService.IDENTITY_CONTRACT_FIELD);
		// get request dto form embedded map
		IdmRoleRequestDto request = (IdmRoleRequestDto) concept.getEmbedded()
				.get(IdmConceptRoleRequestService.ROLE_REQUEST_FIELD);

		Assert.notNull(contract, "Contract in concept is required!");
		Assert.notNull(request, "Request in concept is required!");

		if (!request.getApplicant().equals(contract.getIdentity())) {
			throw new RoleRequestException(CoreResultCode.ROLE_REQUEST_APPLICANTS_NOT_SAME,
					ImmutableMap.of("request", request, "applicant", request.getApplicant()));
		}
		return super.save(concept);
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
	public void delete(IdmConceptRoleRequestDto dto) {
		
		if(!Strings.isNullOrEmpty(dto.getWfProcessId())){
			workflowProcessInstanceService.delete(dto.getWfProcessId(), "Role concept use this WF, was deleted. This WF was deleted too.");
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
