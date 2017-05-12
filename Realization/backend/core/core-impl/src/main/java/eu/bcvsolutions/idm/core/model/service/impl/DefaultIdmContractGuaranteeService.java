package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.model.dto.filter.ContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee_;
import eu.bcvsolutions.idm.core.model.repository.IdmContractGuaranteeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Identity's contract guarantee - manually defined  manager (if no tree structure is defined etc.)
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmContractGuaranteeService 
		extends AbstractReadWriteDtoService<IdmContractGuaranteeDto, IdmContractGuarantee, ContractGuaranteeFilter> 
		implements IdmContractGuaranteeService {
	
	public DefaultIdmContractGuaranteeService(IdmContractGuaranteeRepository repository) {
		super(repository);
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.CONTRACTGUARANTEE, getEntityClass());
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmContractGuarantee> root, CriteriaQuery<?> query, CriteriaBuilder builder, ContractGuaranteeFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// contract id
		if (filter.getIdentityContractId() != null) {
			predicates.add(builder.equal(
					root.get(IdmContractGuarantee_.identityContract).get(AbstractEntity_.id), 
					filter.getIdentityContractId()));
		}
		// guarante id
		if (filter.getGuaranteeId() != null) {
			predicates.add(builder.equal(
					root.get(IdmContractGuarantee_.guarantee).get(AbstractEntity_.id), 
					filter.getGuaranteeId()));
		}
		return predicates;
	}
}
