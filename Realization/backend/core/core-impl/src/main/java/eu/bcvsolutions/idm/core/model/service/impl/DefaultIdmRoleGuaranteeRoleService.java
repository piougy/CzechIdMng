package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleGuaranteeRoleFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeRoleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuaranteeRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuaranteeRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleGuaranteeRoleRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Role guarantees - by role
 * 
 * @author Radek Tomiška
 * @since 8.2.0
 *
 */
public class DefaultIdmRoleGuaranteeRoleService 
		extends AbstractEventableDtoService<IdmRoleGuaranteeRoleDto, IdmRoleGuaranteeRole, IdmRoleGuaranteeRoleFilter> 
		implements IdmRoleGuaranteeRoleService {
	
	@Autowired
	public DefaultIdmRoleGuaranteeRoleService(IdmRoleGuaranteeRoleRepository repository, EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.ROLEGUARANTEEROLE, getEntityClass());
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmRoleGuaranteeRole> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			IdmRoleGuaranteeRoleFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// role
		UUID role = filter.getRole();
		if (role != null) {
			predicates.add(builder.equal(
					root.get(IdmRoleGuaranteeRole_.role).get(IdmRole_.id), 
					role)
					);
		}
		//
		// guarantee role
		UUID guaranteeRole = filter.getGuaranteeRole();
		if (guaranteeRole != null) {
			predicates.add(builder.equal(root.get(IdmRoleGuaranteeRole_.guaranteeRole).get(IdmRole_.id), guaranteeRole));
		}		
		//
		return predicates;
	}
}
