package eu.bcvsolutions.idm.vs.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.vs.domain.VirtualSystemGroupPermission;
import eu.bcvsolutions.idm.vs.entity.VsRequestImplementer;
import eu.bcvsolutions.idm.vs.entity.VsRequestImplementer_;
import eu.bcvsolutions.idm.vs.entity.VsRequest_;
import eu.bcvsolutions.idm.vs.repository.VsRequestImplementerRepository;
import eu.bcvsolutions.idm.vs.repository.filter.VsRequestImplementerFilter;
import eu.bcvsolutions.idm.vs.service.api.VsRequestImplementerService;
import eu.bcvsolutions.idm.vs.service.api.dto.VsRequestDto;
import eu.bcvsolutions.idm.vs.service.api.dto.VsRequestImplementerDto;

/**
 * Service for relation between request in virtual system and identity
 * 
 * @author Svanda
 *
 */
@Service
public class DefaultVsRequestImplementerService
		extends AbstractReadWriteDtoService<VsRequestImplementerDto, VsRequestImplementer, VsRequestImplementerFilter>
		implements VsRequestImplementerService {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultVsRequestImplementerService.class);
	private final IdmIdentityService identityService;

	@Autowired
	public DefaultVsRequestImplementerService(VsRequestImplementerRepository repository,
			IdmIdentityService identityService) {
		super(repository);

		Assert.notNull(identityService);

		this.identityService = identityService;

	}

	@Override
	protected List<Predicate> toPredicates(Root<VsRequestImplementer> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, VsRequestImplementerFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);

		// Request ID
		if (filter.getRequestId() != null) {
			predicates.add(builder.equal(root.get(VsRequestImplementer_.request).get(VsRequest_.id), filter.getRequestId()));
		}

		// Identity ID
		if (filter.getIdentityId() != null) {
			predicates.add(builder.equal(root.get(VsRequestImplementer_.identity).get(IdmIdentity_.id), filter.getIdentityId()));
		}
		return predicates;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(VirtualSystemGroupPermission.VSREQUEST, getEntityClass());
	}

	@Override
	public List<IdmIdentityDto> findRequestImplementers(VsRequestDto request) {
		if (request == null) {
			return null;
		}
		VsRequestImplementerFilter filter = new VsRequestImplementerFilter();
		filter.setRequestId(request.getId());
		List<VsRequestImplementerDto> requestImplementers = this.find(filter, null).getContent();
		return requestImplementers.stream()//
				.map(VsRequestImplementerDto::getIdentity)//
				.map(identityService::get)//
				.collect(Collectors.toList());
	}

}
