package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import eu.bcvsolutions.idm.core.api.dto.filter.ContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee;

/**
 * Identity contract's guarantee
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmContractGuaranteeRepository extends AbstractEntityRepository<IdmContractGuarantee, ContractGuaranteeFilter> {

	/**
	 * @deprecated Use IdmContractGuaranteeService (uses criteria api)
	 */
	@Override
	@Deprecated
	@Query(value = "select e from #{#entityName} e")
	default Page<IdmContractGuarantee> find(ContractGuaranteeFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Use IdmContractGuaranteeService (uses criteria api)");
	}
}
