package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.domain.ProvisioningOperation;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningAttributeRepository;

/**
 * Helper service only. For simple CRUD use {@link SysProvisioningAttributeRepository}.
 * 
 * @see SysProvisioningAttributeRepository
 * @author Radek Tomiška
 * @since 9.6.3
 */
public interface SysProvisioningAttributeService {
   
	/**
	 * Saves connector attributes in provisioning context of given operation.
	 * 
	 * @param operation
	 */
	void saveAttributes(ProvisioningOperation operation);
	
	/**
	 * Deletes all attributes associated to given operation.
	 * 
	 * @param operation
	 * @return count of deleted attributes
	 */
	int deleteAttributes(ProvisioningOperation operation);
	
	
	/**
	 * Delete attributes for deleted operation or archive.
	 * 
	 * @return count of deleted attributes
	 */
	int cleanupAttributes();
}
