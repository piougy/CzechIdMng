package eu.bcvsolutions.idm.vs.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.entity.SysSystem_;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.vs.domain.VirtualSystemGroupPermission;
import eu.bcvsolutions.idm.vs.dto.VsSystemImplementerDto;
import eu.bcvsolutions.idm.vs.dto.filter.VsSystemImplementerFilter;
import eu.bcvsolutions.idm.vs.entity.VsSystemImplementer;
import eu.bcvsolutions.idm.vs.entity.VsSystemImplementer_;
import eu.bcvsolutions.idm.vs.repository.VsSystemImplementerRepository;
import eu.bcvsolutions.idm.vs.service.api.VsSystemImplementerService;

/**
 * Service for relation between system in virtual system and identity
 * 
 * @author Svanda
 *
 */
@Service
public class DefaultVsSystemImplementerService
		extends AbstractReadWriteDtoService<VsSystemImplementerDto, VsSystemImplementer, VsSystemImplementerFilter>
		implements VsSystemImplementerService {
	
	private final IdmIdentityService identityService;

	@Autowired
	public DefaultVsSystemImplementerService(VsSystemImplementerRepository repository,
			IdmIdentityService identityService) {
		super(repository);

		Assert.notNull(identityService);

		this.identityService = identityService;

	}

	@Override
	protected List<Predicate> toPredicates(Root<VsSystemImplementer> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, VsSystemImplementerFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);

		// System ID
		if (filter.getSystemId() != null) {
			predicates
					.add(builder.equal(root.get(VsSystemImplementer_.system).get(SysSystem_.id), filter.getSystemId()));
		}

		// Identity ID
		if (filter.getIdentityId() != null) {
			predicates.add(builder.equal(root.get(VsSystemImplementer_.identity).get(IdmIdentity_.id),
					filter.getIdentityId()));
		}
		return predicates;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(VirtualSystemGroupPermission.VSREQUEST, getEntityClass());
	}

	@Override
	public List<IdmIdentityDto> findRequestImplementers(UUID vsSystemId) {
		if (vsSystemId == null) {
			return null;
		}
		VsSystemImplementerFilter filter = new VsSystemImplementerFilter();
		filter.setSystemId(vsSystemId);
		List<VsSystemImplementerDto> requestImplementers = this.find(filter, null).getContent();
		Set<IdmIdentityDto> identities = requestImplementers.stream()//
				.filter(sysImp -> sysImp.getIdentity() != null)//
				.map(VsSystemImplementerDto::getIdentity)//
				.map(identityService::get)//
				.collect(Collectors.toSet());

		// Add identities from all roles
		Set<UUID> roles = requestImplementers.stream()//
				.filter(sysImp -> sysImp.getRole() != null)//
				.map(VsSystemImplementerDto::getRole)//
				.collect(Collectors.toSet());

		roles.forEach(role -> {
			identities.addAll(identityService.findAllByRole(role));
		});
		return new ArrayList<>(identities);
	}

}
