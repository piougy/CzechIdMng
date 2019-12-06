package eu.bcvsolutions.idm.acc.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Provisioning log
 * 
 * @author Radek Tomiška
 *
 */
public interface SysProvisioningOperationRepository extends AbstractEntityRepository<SysProvisioningOperation> {
	
	/**
	 * Delete operations by given system id
	 * 
	 * @param systemId @Modifying has to return int or Integer only
	 */
	@Modifying
	@Query("delete from #{#entityName} e where e.system.id = :systemId")
	int deleteBySystem(@Param("systemId") UUID systemId);

	/**
	 * Delete operation directly
	 *
	 * @return
	 */
	@Modifying
	@Query("delete from #{#entityName}")
	void deleteAll();
}
