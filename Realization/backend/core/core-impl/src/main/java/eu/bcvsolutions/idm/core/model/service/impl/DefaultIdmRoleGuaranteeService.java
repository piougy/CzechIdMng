package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.RoleGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleGuaranteeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleGuaranteeService;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Role guarantees
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmRoleGuaranteeService 
		extends AbstractReadWriteDtoService<IdmRoleGuaranteeDto, IdmRoleGuarantee, RoleGuaranteeFilter> 
		implements IdmRoleGuaranteeService {
	
	
	@Autowired
	public DefaultIdmRoleGuaranteeService(IdmRoleGuaranteeRepository repository) {
		super(repository);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.ROLE, getEntityClass());
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmRoleGuarantee> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			RoleGuaranteeFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// role
		if (filter.getRole() != null) {
			predicates.add(builder.equal(
					root.get(IdmRoleGuarantee_.role).get(IdmRole_.id), 
					filter.getRole())
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
		// id
		if (filter.getId() != null) {
			predicates.add(builder.equal(root.get(IdmRoleGuarantee_.id), filter.getId()));
		}
		//
		return predicates;
	}
}
