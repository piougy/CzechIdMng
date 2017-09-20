package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleGuaranteeRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Role guarantees
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
		// not secured
		return null;
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmRoleGuarantee> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			IdmRoleGuaranteeFilter filter) {
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
		//
		return predicates;
	}
}
