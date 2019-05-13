package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.ResolvedIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIncompatibleRoleFilter;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIncompatibleRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIncompatibleRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIncompatibleRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmIncompatibleRoleRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Segregation of Duties
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
public class DefaultIdmIncompatibleRoleService 
		extends AbstractEventableDtoService<IdmIncompatibleRoleDto, IdmIncompatibleRole, IdmIncompatibleRoleFilter> 
		implements IdmIncompatibleRoleService {
	
	private final IdmIncompatibleRoleRepository repository;
	//
	@Autowired private IdmRoleCompositionService roleCompositionService;
	@Autowired private LookupService lookupService;
	
	@Autowired
	public DefaultIdmIncompatibleRoleService(IdmIncompatibleRoleRepository repository, EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
		//
		this.repository = repository;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.INCOMPATIBLEROLE, getEntityClass());
	}
	
	@Override
	public List<IdmIncompatibleRoleDto> findAllByRole(UUID roleId) {
		return toDtos(repository.findAllByRole(roleId), false);
	}
	
	@Override
	public Set<ResolvedIncompatibleRoleDto> resolveIncompatibleRoles(List<Serializable> rolesOrIdentifiers) {
		// search all defined incompatible roles for given roles - business roles can be given
		Set<ResolvedIncompatibleRoleDto> incompatibleRoles = new HashSet<>();
		if(CollectionUtils.isEmpty(rolesOrIdentifiers)) {
			return incompatibleRoles;
		}
		Set<UUID> allRoleIds = new HashSet<>();
		Set<IdmRoleDto> roles = new HashSet<>();
		// search all sub roles
		for (Serializable roleOrIdentifier : rolesOrIdentifiers) {
			if (roleOrIdentifier == null) {
				continue;
			}
			roles.clear();
			//
			IdmRoleDto directRole = null;
			if (roleOrIdentifier instanceof IdmRoleDto) {
				directRole = (IdmRoleDto) roleOrIdentifier;
			} else {
				directRole = (IdmRoleDto) lookupService.lookupDto(IdmRoleDto.class, roleOrIdentifier);
			}
			if (directRole == null) {
				throw new EntityNotFoundException(IdmRole.class, roleOrIdentifier);
			}
			//
			roles.add(directRole);
			if (directRole.getChildrenCount() > 0) {
				roles.addAll(roleCompositionService.resolveDistinctRoles(roleCompositionService.findAllSubRoles(directRole.getId())));
			}
			//
			// resolve incompatible roles 
			for(IdmRoleDto r : roles) {
				// find incompatible roles - we need to know, which from the given role is incompatible => ResolvedIncompatibleRoleDto
				for(IdmIncompatibleRoleDto incompatibleRole : findAllByRole(r.getId())) {
					incompatibleRoles.add(new ResolvedIncompatibleRoleDto(directRole, incompatibleRole));
				}
				allRoleIds.add(r.getId());
			};
		}
		//
		// both sides of incompatible roles should be in the allRoleIds and superior vs. sub role has to be different.
		return incompatibleRoles
				.stream()
				.filter(ir -> { // superior vs. sub role has to be different.
					return !ir.getIncompatibleRole().getSuperior().equals(ir.getIncompatibleRole().getSub());
				})
				.filter(ir -> { // superior and sub role has to be in all roles.
					return allRoleIds.contains(ir.getIncompatibleRole().getSuperior()) && allRoleIds.contains(ir.getIncompatibleRole().getSub());
				})
				.collect(Collectors.toSet());
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmIncompatibleRole> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			IdmIncompatibleRoleFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// superior role
		UUID superior = filter.getSuperiorId();
		if (superior != null) {
			predicates.add(builder.equal(root.get(IdmIncompatibleRole_.superior).get(IdmRole_.id), superior));
		}
		//
		// sub role
		UUID sub = filter.getSubId();
		if (sub != null) {
			predicates.add(builder.equal(root.get(IdmIncompatibleRole_.sub).get(IdmRole_.id), sub));
		}		
		//
		return predicates;
	}
}
