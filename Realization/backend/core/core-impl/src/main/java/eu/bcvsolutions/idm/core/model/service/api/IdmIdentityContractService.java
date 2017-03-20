package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.domain.RecursionType;
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
	
	static final String DEFAULT_POSITION_NAME = "Default"; // TODO: to configuration manager?
	
	/**
	 * Returns working positions for given identity
	 * 
	 * @param identity
	 * @return
	 */
	List<IdmIdentityContract> getContracts(IdmIdentity identity);
	
	/**
	 * Returns all identity contract, where fits conttract's work position with given work position by recursionType.
	 * 
	 * @param workPositionId
	 * @param recursion
	 * @return
	 */
	List<IdmIdentityContract> getContractsByWorkPosition(UUID workPositionId, RecursionType recursion);
	
	/**
	 * Clears guarantee from all contracts, where identity is guarantee (=identity disclaims guarantee).
	 * 
	 * @param identity
	 * @return Returns number of affected contracts
	 */
	int clearGuarantee(IdmIdentity identity);
	
	/**
	 * Returns expired contracts
	 * 
	 * @param expiration date to compare
	 * @param disabled find disabled contracts or not
	 * @param pageable
	 * @return
	 */	
	Page<IdmIdentityContract> findExpiredContracts(LocalDate expiration, boolean disabled, Pageable pageable);
	
	/**
	 * Constructs default contract for given identity by configuration.
	 * 
	 * @see {@link IdmTreeTypeService#getDefaultTreeType()}
	 * @param identity
	 * @return
	 */
	IdmIdentityContract prepareDefaultContract(IdmIdentity identity);	
	
	/**
	 * Returns given identity's prime contract.
	 * If no main contract is defined, then returns the first contract with working position defined (default tree type has higher priority).
	 * 
	 * @param identity
	 * @return
	 */
	IdmIdentityContract getPrimeContract(IdmIdentity identity);
}
