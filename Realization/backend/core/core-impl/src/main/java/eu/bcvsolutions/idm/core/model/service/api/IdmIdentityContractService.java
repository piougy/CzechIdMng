package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;

import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityContractFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;

/**
 * Operations with working positions
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmIdentityContractService extends ReadWriteEntityService<IdmIdentityContract, IdentityContractFilter> {
	
	/**
	 * Returns working positions for given identity
	 * 
	 * @param identity
	 * @return
	 */
	List<IdmIdentityContract> getContracts(IdmIdentity identity);
	
	/**
	 * Clears guarantee from all contracts, where identity is guarantee (=identity disclaims guarantee).
	 * 
	 * @param identity
	 * @return Returns number of affected contracts
	 */
	int clearGuarantee(@Param("identity") IdmIdentity identity);
	
	/**
	 * Returns expired contracts
	 * 
	 * @param expiration date to compare
	 * @param pageable
	 * @return
	 */	
	Page<IdmIdentityContract> findExpiredContracts(@Param("expiration") LocalDate expiration, Pageable pageable);
	
}
