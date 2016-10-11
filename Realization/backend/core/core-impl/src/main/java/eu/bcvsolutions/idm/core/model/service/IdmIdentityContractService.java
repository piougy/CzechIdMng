package eu.bcvsolutions.idm.core.model.service;

import java.util.List;

import eu.bcvsolutions.idm.core.api.dto.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;

/**
 * Operations with working positions
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmIdentityContractService extends ReadWriteEntityService<IdmIdentityContract, EmptyFilter> {
	
	/**
	 * Returns working positions for given identity
	 * 
	 * @param identity
	 * @return
	 */
	List<IdmIdentityContract> getContracts(IdmIdentity identity);
	
}
