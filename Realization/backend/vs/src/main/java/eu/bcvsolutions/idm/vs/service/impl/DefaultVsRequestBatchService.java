package eu.bcvsolutions.idm.vs.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.vs.domain.VirtualSystemGroupPermission;
import eu.bcvsolutions.idm.vs.entity.VsRequestBatch;
import eu.bcvsolutions.idm.vs.repository.VsRequestBatchRepository;
import eu.bcvsolutions.idm.vs.repository.filter.VsRequestBatchFilter;
import eu.bcvsolutions.idm.vs.service.api.VsRequestBatchService;
import eu.bcvsolutions.idm.vs.service.api.dto.VsRequestBatchDto;

/**
 * Service for request batch in virtual system
 * 
 * @author Svanda
 *
 */
@Service
public class DefaultVsRequestBatchService
		extends AbstractReadWriteDtoService<VsRequestBatchDto, VsRequestBatch, VsRequestBatchFilter> 
		implements VsRequestBatchService {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultVsRequestBatchService.class);

	private final EntityEventManager entityEventManager;
	
	@Autowired
	public DefaultVsRequestBatchService(
			VsRequestBatchRepository repository,
			EntityEventManager entityEventManager) {
		super(repository);
		//
		Assert.notNull(entityEventManager);
		
		this.entityEventManager = entityEventManager;
	}


	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(VirtualSystemGroupPermission.VSREQUEST, getEntityClass());
	}

}
