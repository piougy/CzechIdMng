package eu.bcvsolutions.idm.core.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.filter.ConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;

/**
 * Default implementation of concept role request service
 * @author svandav
 *
 */
@Service("conceptRoleRequestService")
public class DefaultIdmConceptRoleRequestService extends AbstractReadWriteDtoService<IdmConceptRoleRequestDto, IdmConceptRoleRequest, ConceptRoleRequestFilter>
		implements IdmConceptRoleRequestService {
	
	// private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmConceptRoleRequestService.class);

	@Autowired
	public DefaultIdmConceptRoleRequestService(AbstractEntityRepository<IdmConceptRoleRequest, ConceptRoleRequestFilter> repository) {
		super(repository);
	}



}
