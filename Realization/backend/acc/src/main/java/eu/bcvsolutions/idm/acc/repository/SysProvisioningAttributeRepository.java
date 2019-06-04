package eu.bcvsolutions.idm.acc.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningAttribute;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Schema attributes used in provisioning archive or operation.
 * 
 * @author Radek Tomiška
 * @since 9.6.3
 */
public interface SysProvisioningAttributeRepository extends AbstractEntityRepository<SysProvisioningAttribute> {
	
	/**
	 * Attributes for given operation.
	 * Mainly for test purposes.
	 * 
	 * @param provisioningId
	 * @return
	 */
	List<SysProvisioningAttribute> findAllByProvisioningId(UUID provisioningId);
	
	/**
	 * Delete all atributes for given archive or operation
	 * 
	 * @param provisioningId
	 * @return
	 */
	int deleteByProvisioningId(UUID provisioningId);
	
	/**
	 * Delete attributes for deleted operation or archive.
	 * 
	 * @return
	 */
	@Modifying
	@Query("delete from #{#entityName} e where e.provisioningId not in (select id from SysProvisioningArchive) and e.provisioningId not in (select id from SysProvisioningOperation)")
	int cleanupAttributes();
}
