package eu.bcvsolutions.idm.core.model.service.impl;

import java.text.MessageFormat;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.domain.Loggable;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.model.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.filter.ConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;

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

	@Autowired
	public DefaultIdmConceptRoleRequestService(
			AbstractEntityRepository<IdmConceptRoleRequest, ConceptRoleRequestFilter> repository) {
		super(repository);
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
