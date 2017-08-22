package eu.bcvsolutions.idm.vs.service.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.vs.domain.VirtualSystemGroupPermission;
import eu.bcvsolutions.idm.vs.entity.VsRequest;
import eu.bcvsolutions.idm.vs.entity.VsRequest_;
import eu.bcvsolutions.idm.vs.repository.VsRequestRepository;
import eu.bcvsolutions.idm.vs.repository.filter.RequestFilter;
import eu.bcvsolutions.idm.vs.service.api.VsRequestService;
import eu.bcvsolutions.idm.vs.service.api.dto.VsRequestDto;

/**
 * Service for request in virtual system
 * 
 * @author Svanda
 *
 */
@Service
public class DefaultVsRequestService
		extends AbstractReadWriteDtoService<VsRequestDto, VsRequest, RequestFilter> 
		implements VsRequestService {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultVsRequestService.class);

	private final EntityEventManager entityEventManager;
	
	@Autowired
	public DefaultVsRequestService(
			VsRequestRepository repository,
			EntityEventManager entityEventManager) {
		super(repository);
		//
		Assert.notNull(entityEventManager);
		
		this.entityEventManager = entityEventManager;
	}
	
	
	@Override
	protected List<Predicate> toPredicates(Root<VsRequest> root, CriteriaQuery<?> query, CriteriaBuilder builder, RequestFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// quick - "fulltext"
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.or(
					builder.equal(builder.lower(root.get(VsRequest_.uid)), "%" + filter.getText().toLowerCase() + "%")			
					));
		}
		
		// UID
		if (StringUtils.isNotEmpty(filter.getUid())) {
			predicates.add(builder.equal(root.get(VsRequest_.uid), filter.getUid()));
		}
		
		// System ID
		if (filter.getSystemId() != null) {
			predicates.add(builder.equal(root.get(VsRequest_.systemId), filter.getSystemId()));
		}
		return predicates;
	}


	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(VirtualSystemGroupPermission.VSREQUEST, getEntityClass());
	}

}
