package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleGuaranteeRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Role guarantees
 * 
 * TODO: eventable - when role guarantee will be removed from IdmRole list and detail.
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmRoleGuaranteeService 
		extends AbstractReadWriteDtoService<IdmRoleGuaranteeDto, IdmRoleGuarantee, IdmRoleGuaranteeFilter> 
		implements IdmRoleGuaranteeService {
	
	@Autowired
	public DefaultIdmRoleGuaranteeService(IdmRoleGuaranteeRepository repository) {
		super(repository);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.ROLEGUARANTEE, getEntityClass());
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmRoleGuarantee> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			IdmRoleGuaranteeFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// role
		UUID role = filter.getRole();
		if (role != null) {
			predicates.add(builder.equal(
					root.get(IdmRoleGuarantee_.role).get(IdmRole_.id), 
					role)
					);
		}
		//
		// guarantee
		if (filter.getGuarantee() != null) {
			predicates.add(builder.equal(
					root.get(IdmRoleGuarantee_.guarantee).get(IdmIdentity_.id), 
					filter.getGuarantee())
					);
		}	
		//
		return predicates;
	}
}
