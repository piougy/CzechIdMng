package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.domain.RecursionType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdentityContractFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Operations with working positions
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmIdentityContractService extends
	ReadWriteDtoService<IdmIdentityContractDto, IdentityContractFilter>,
	AuthorizableService<IdmIdentityContractDto>,
	ScriptEnabled {
	
	static final String DEFAULT_POSITION_NAME = "Default"; // TODO: to configuration manager?
	
	/**
	 * Returns working positions for given identity
	 * 
	 * @param identityId
	 * @return
	 */
	List<IdmIdentityContractDto> findAllByIdentity(UUID identityId);
	
	/**
	 * Returns all identity contract, where fits conttract's work position with given work position by recursionType.
	 * 
	 * @param workPositionId
	 * @param recursion
	 * @return
	 */
	List<IdmIdentityContractDto> findAllByWorkPosition(UUID workPositionId, RecursionType recursion);
	
	/**
	 * Returns expired contracts
	 * 
	 * @param expiration date to compare
	 * @param pageable
	 * @return
	 */	
	Page<IdmIdentityContractDto> findExpiredContracts(LocalDate expiration, Pageable pageable);
	
	/**
	 * Constructs default contract for given identity by configuration.
	 * 
	 * @see {@link IdmTreeTypeService#getDefaultTreeType()}
	 * @param identityId
	 * @return
	 */
	IdmIdentityContractDto prepareDefaultContract(UUID identityId);	
	
	/**
	 * Returns given identity's prime contract.
	 * If no main contract is defined, then returns the first contract with working position defined (default tree type has higher priority).
	 * 
	 * @param identityId
	 * @return
	 */
	IdmIdentityContractDto getPrimeContract(UUID identityId);
	
	/**
	 * Method get valid {@link IdmIdentityContract} for date and {@link IdmIdentity} id given in parameter.
	 * Parameter onlyExterne if it's true search only contracts where is {@link IdmIdentity} marked as externe, this param can be null - search all contracts.
	 * 
	 * @param identityId
	 * @param date
	 * @param onlyExterne
	 * @return
	 */
	List<IdmIdentityContractDto> findAllValidForDate(UUID identityId, LocalDate date, Boolean onlyExterne);
}
