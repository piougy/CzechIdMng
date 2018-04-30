package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceGuaranteeService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmContractSliceGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmContractSliceGuarantee_;
import eu.bcvsolutions.idm.core.model.repository.IdmContractSliceGuaranteeRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Identity's contract slice guarantee - manually defined  manager (if no tree structure is defined etc.)
 * 
 * @author svandav
 *
 */
@Service("contractSliceGuaranteeService")
public class DefaultIdmContractSliceGuaranteeService 
		extends AbstractEventableDtoService<IdmContractSliceGuaranteeDto, IdmContractSliceGuarantee, IdmContractSliceGuaranteeFilter> 
		implements IdmContractSliceGuaranteeService {
	
	@Autowired
	public DefaultIdmContractSliceGuaranteeService(
			IdmContractSliceGuaranteeRepository repository,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.CONTRACTSLICEGUARANTEE, getEntityClass());
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmContractSliceGuarantee> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmContractSliceGuaranteeFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// contract id
		if (filter.getContractSliceId() != null) {
			predicates.add(builder.equal(
					root.get(IdmContractSliceGuarantee_.contractSlice).get(AbstractEntity_.id), 
					filter.getContractSliceId()));
		}
		// guarante id
		if (filter.getGuaranteeId() != null) {
			predicates.add(builder.equal(
					root.get(IdmContractSliceGuarantee_.guarantee).get(AbstractEntity_.id), 
					filter.getGuaranteeId()));
		}
		return predicates;
	}
}
